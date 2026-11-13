package com.testSpring.store.services;

import com.testSpring.store.entities.Status;
import com.testSpring.store.repositories.StatusRepository;
import com.testSpring.store.repositories.SampleRepository;
import com.testSpring.store.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class StatusServices {

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private SampleRepository sampleRepository;

    public List<Status> getAllStatuses() { return (List<Status>) this.statusRepository.findAll(); }
    public Page<Status> getAllStatuses(Pageable pageable) { return statusRepository.findAll(pageable); }
    public Optional<Status> getStatus(int id) {
        return this.statusRepository.findById(id);
    }
    public Optional<Status> getStatusByCode(String code) {
        return Optional.ofNullable(this.statusRepository.findStatusByCode(code));
    }
    public void addStatus(Status status) { this.statusRepository.save(status); }
    public void updateStatus(Status status, int id) {
        if (!statusRepository.existsById(id)) throw new ResourceNotFoundException("No existe el estado " + id);
        status.setId(id);
        this.statusRepository.save(status);
    }
    public void deleteStatus(int id) {
        if (sampleRepository.existsByStatus_Id(id)) {
            throw new IllegalStateException("No se puede borrar un estado que está asignado a muestras");
        }
        this.statusRepository.deleteById(id);
    }
}
