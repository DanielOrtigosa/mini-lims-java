package com.testSpring.store.services;

import com.testSpring.store.entities.Sample;
import com.testSpring.store.entities.SampleStatusHistory;
import com.testSpring.store.entities.Status;
import com.testSpring.store.entities.Role;
import com.testSpring.store.repositories.SampleRepository;
import com.testSpring.store.repositories.SampleStatusHistoryRepository;
import com.testSpring.store.repositories.StatusRepository;
import com.testSpring.store.repositories.LimsUserRepository;
import com.testSpring.store.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class SampleServices {

    @Autowired
    private SampleRepository sampleRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private SampleStatusHistoryRepository historyRepository;

    @Autowired
    private LimsUserRepository limsUserRepository;

    public List<Sample> getAllSamples() { return (List<Sample>) this.sampleRepository.findAll(); }
    public Page<Sample> getAllSamples(Pageable pageable) { return sampleRepository.findAll(pageable); }
    public Optional<Sample> getSample(int id) {
        return this.sampleRepository.findById(id);
    }
    public Optional<Sample> getSampleByCode(String sampleCode) {
        return Optional.ofNullable(this.sampleRepository.findSampleBySampleCode(sampleCode));
    }
    @Transactional
    public void addSample(Sample sample) {
        addSample(sample, null);
    }

    @Transactional
    public void addSample(Sample sample, String creatorEmail) {
        if (sample.getDateCreation() == null) sample.setDateCreation(LocalDate.now());
        if (creatorEmail != null) {
            sample.setCreatedBy(limsUserRepository.findByEmail(creatorEmail).map(user -> user.getID()).orElse(null));
        }
        else sample.setCreatedBy(null);
        sample.setActive(true);
        sample.setTechnician(null);
        Status pending = getOrCreateWorkflowStatus("PENDING", "Pendiente");
        sample.setStatus(pending);
        sample.setState(pending.getName());
        Sample savedSample = sampleRepository.save(sample);
        historyRepository.save(new SampleStatusHistory(savedSample, null, "PENDING", LocalDateTime.now(), creatorEmail));
    }

    @Transactional
    public Sample advanceSampleStatus(int id) {
        return advanceSampleStatus(id, null);
    }

    @Transactional
    public Sample advanceSampleStatus(int id, String changedBy) {
        Sample sample = sampleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la muestra " + id));
        if (sample.getStatus() == null || sample.getStatus().getCode() == null) {
            throw new IllegalStateException("La muestra no tiene un estado válido");
        }

        String currentCode = sample.getStatus().getCode().trim().toUpperCase();
        String nextCode;
        String nextName;
        switch (currentCode) {
            case "PENDING":
                nextCode = "IN_ANALYSIS";
                nextName = "En análisis";
                break;
            case "IN_ANALYSIS":
                nextCode = "COMPLETED";
                nextName = "Completado";
                break;
            case "COMPLETED":
                throw new IllegalStateException("La muestra ya está completada");
            default:
                throw new IllegalStateException("No se puede avanzar desde el estado " + currentCode);
        }

        Status nextStatus = getOrCreateWorkflowStatus(nextCode, nextName);
        sample.setStatus(nextStatus);
        sample.setState(nextStatus.getName());
        sample.setDateModified(LocalDate.now());
        Sample savedSample = sampleRepository.save(sample);
        historyRepository.save(new SampleStatusHistory(savedSample, currentCode, nextCode, LocalDateTime.now(), changedBy));
        return savedSample;
    }

    public List<SampleStatusHistory> getSampleStatusHistory(int sampleId) {
        if (!sampleRepository.existsById(sampleId)) {
            throw new ResourceNotFoundException("No existe la muestra " + sampleId);
        }
        return historyRepository.findBySample_IdOrderByChangedAtAsc(sampleId);
    }

    private Status getOrCreateWorkflowStatus(String code, String name) {
        Status status = statusRepository.findFirstByCodeIgnoreCase(code);
        if (status != null) return status;
        Status newStatus = new Status();
        newStatus.setCode(code);
        newStatus.setName(name);
        return statusRepository.save(newStatus);
    }

    public void updateSample(Sample sample, int id) {
        Sample current = sampleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la muestra " + id));
        if (sample.getVersion() == null) {
            throw new IllegalArgumentException("La versión de la muestra es obligatoria");
        }
        if (!sample.getVersion().equals(current.getVersion())) {
            throw new org.springframework.orm.ObjectOptimisticLockingFailureException(Sample.class, id);
        }
        if (sample.getSampleCode() != null) current.setSampleCode(sample.getSampleCode());
        if (sample.getType() != null) current.setType(sample.getType());
        if (sample.getDescription() != null) current.setDescription(sample.getDescription());
        // Preserve lifecycle, reception and ownership fields during ordinary updates.
        current.setDateModified(LocalDate.now());
        this.sampleRepository.save(current);
    }

    @Transactional
    public Sample assignTechnician(int sampleId, int technicianId) {
        Sample sample = sampleRepository.findById(sampleId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la muestra " + sampleId));
        com.testSpring.store.entities.LimsUser technician = limsUserRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el técnico " + technicianId));
        if (!technician.isActive()) throw new IllegalStateException("El técnico está inactivo");
        if (technician.getRole() != Role.TECHNICIAN && technician.getRole() != Role.TECNICO) {
            throw new IllegalStateException("El usuario indicado no tiene rol de técnico");
        }
        sample.setTechnician(technician);
        sample.setDateModified(LocalDate.now());
        return sampleRepository.save(sample);
    }
    public void deleteSample(int id) {
        this.sampleRepository.findById(id).ifPresent(sample -> {
            sample.setActive(false);
            this.sampleRepository.save(sample);
        });
    }
    public void activateSample(int id) {
        this.sampleRepository.findById(id).ifPresent(sample -> {
            sample.setActive(true);
            this.sampleRepository.save(sample);
        });
    }
}
