package com.testSpring.store.services;

import com.testSpring.store.entities.Status;
import com.testSpring.store.repositories.StatusRepository;
import com.testSpring.store.repositories.SampleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatusServicesTest {
    @Mock StatusRepository repository;
    @Mock SampleRepository samples;
    @InjectMocks StatusServices service;

    @Test
    void updatePersistsWithRequestedId() {
        when(repository.existsById(3)).thenReturn(true);
        Status status = new Status();
        service.updateStatus(status, 3);
        assertEquals(3, status.getId());
        verify(repository).save(status);
    }

    @Test
    void updateOfMissingStatusFails() {
        when(repository.existsById(3)).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> service.updateStatus(new Status(), 3));
        verify(repository, never()).save(any());
    }

    @Test
    void cannotDeleteStatusThatIsStillAssignedToASample() {
        when(samples.existsByStatus_Id(3)).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> service.deleteStatus(3));
        verify(repository, never()).deleteById(any());
    }
}
