package com.testSpring.store.services;

import com.testSpring.store.entities.Sample;
import com.testSpring.store.entities.Status;
import com.testSpring.store.entities.LimsUser;
import com.testSpring.store.entities.Role;
import com.testSpring.store.repositories.LimsUserRepository;
import com.testSpring.store.repositories.SampleRepository;
import com.testSpring.store.repositories.SampleStatusHistoryRepository;
import com.testSpring.store.repositories.StatusRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SampleServicesTest {
    @Mock SampleRepository samples;
    @Mock StatusRepository statuses;
    @Mock SampleStatusHistoryRepository history;
    @Mock LimsUserRepository users;
    @InjectMocks SampleServices service;

    @Test
    void addingSampleStartsWorkflowAndRecordsHistory() {
        Status pending = status(1, "PENDING", "Pendiente");
        when(statuses.findFirstByCodeIgnoreCase("PENDING")).thenReturn(null);
        when(statuses.save(any(Status.class))).thenReturn(pending);
        when(samples.save(any(Sample.class))).thenAnswer(call -> call.getArgument(0));
        Sample sample = new Sample();
        sample.setSampleCode("S-1");

        service.addSample(sample);

        assertEquals("PENDING", sample.getStatus().getCode());
        assertEquals("Pendiente", sample.getState());
        assertEquals(LocalDate.now(), sample.getDateCreation());
        assertNull(sample.getTechnician());
        verify(history).save(any());
    }

    @Test
    void updatingSamplePreservesActivationOwnershipReceptionAndWorkflow() {
        Sample existing = new Sample();
        existing.setSampleCode("S-keep");
        existing.setType("water");
        existing.setActive(false);
        existing.setCreatedBy(42);
        existing.setDateReception(LocalDate.of(2026, 1, 2));
        existing.setVersion(3L);
        existing.setStatus(status(1, "PENDING", "Pendiente"));
        existing.setState("Pendiente");
        when(samples.findById(7)).thenReturn(Optional.of(existing));

        Sample changes = new Sample();
        changes.setSampleCode("S-updated");
        changes.setType("blood");
        changes.setActive(true);
        changes.setCreatedBy(999);
        changes.setDateReception(LocalDate.of(2030, 1, 1));
        changes.setState("COMPLETED");
        changes.setVersion(3L);
        service.updateSample(changes, 7);

        assertEquals("S-updated", existing.getSampleCode());
        assertFalse(existing.isActive());
        assertEquals(42, existing.getCreatedBy());
        assertEquals(LocalDate.of(2026, 1, 2), existing.getDateReception());
        assertEquals("PENDING", existing.getStatus().getCode());
        assertEquals("Pendiente", existing.getState());
        verify(samples).save(existing);
    }

    @Test
    void statusCanAdvanceTwiceAndCannotAdvanceAfterCompletion() {
        Sample sample = new Sample();
        sample.setStatus(status(1, "PENDING", "Pendiente"));
        when(samples.findById(7)).thenReturn(Optional.of(sample));
        when(statuses.findFirstByCodeIgnoreCase("IN_ANALYSIS"))
                .thenReturn(status(2, "IN_ANALYSIS", "En análisis"));
        when(statuses.findFirstByCodeIgnoreCase("COMPLETED"))
                .thenReturn(status(3, "COMPLETED", "Completado"));
        when(samples.save(any(Sample.class))).thenAnswer(call -> call.getArgument(0));

        service.advanceSampleStatus(7);
        assertEquals("IN_ANALYSIS", sample.getStatus().getCode());
        service.advanceSampleStatus(7);
        assertEquals("COMPLETED", sample.getStatus().getCode());
        assertThrows(IllegalStateException.class, () -> service.advanceSampleStatus(7));
        verify(history, times(2)).save(any());
    }

    @Test
    void advancingMissingSampleFails() {
        when(samples.findById(99)).thenReturn(Optional.empty());
        assertThrows(com.testSpring.store.exceptions.ResourceNotFoundException.class,
                () -> service.advanceSampleStatus(99));
    }

    @Test
    void assignmentRequiresAnActiveTechnician() {
        Sample sample = new Sample();
        LimsUser technician = new LimsUser();
        technician.setRole(Role.TECHNICIAN);
        technician.setActive(true);
        when(samples.findById(7)).thenReturn(Optional.of(sample));
        when(users.findById(12)).thenReturn(Optional.of(technician));
        when(samples.save(any(Sample.class))).thenAnswer(call -> call.getArgument(0));

        assertSame(sample, service.assignTechnician(7, 12));
        assertSame(technician, sample.getTechnician());

        technician.setActive(false);
        assertThrows(IllegalStateException.class, () -> service.assignTechnician(7, 12));
        verify(samples, times(1)).save(sample);
    }

    @Test
    void oneTechnicianCanBeAssignedToMoreThanOneSample() {
        LimsUser technician = new LimsUser();
        technician.setRole(Role.TECNICO);
        technician.setActive(true);
        Sample first = new Sample();
        Sample second = new Sample();
        when(samples.findById(7)).thenReturn(Optional.of(first));
        when(samples.findById(8)).thenReturn(Optional.of(second));
        when(users.findById(12)).thenReturn(Optional.of(technician));
        when(samples.save(any(Sample.class))).thenAnswer(call -> call.getArgument(0));

        service.assignTechnician(7, 12);
        service.assignTechnician(8, 12);

        assertSame(technician, first.getTechnician());
        assertSame(technician, second.getTechnician());
    }

    private Status status(int id, String code, String name) {
        Status status = new Status();
        status.setId(id);
        status.setCode(code);
        status.setName(name);
        return status;
    }
}
