package com.testSpring.store.services;

import com.testSpring.store.entities.Status;
import com.testSpring.store.repositories.StatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StatusServices {

    @Autowired
    private StatusRepository statusRepository;

    public List<Status> getAllStatuses() { return (List<Status>) this.statusRepository.findAll(); }
    public Optional<Status> getStatus(int id) {
        return this.statusRepository.findById(id);
    }
    public Optional<Status> getStatusByCode(String code) {
        return Optional.ofNullable(this.statusRepository.findStatusByCode(code));
    }
    public void addStatus(Status status) { this.statusRepository.save(status); }
    public void updateStatus(Status status, int id) {
        if (!statusRepository.existsById(id)) throw new IllegalArgumentException("No existe el estado " + id);
        status.setId(id);
        this.statusRepository.save(status);
    }
    public void deleteStatus(int id) { this.statusRepository.deleteById(id); }
}
