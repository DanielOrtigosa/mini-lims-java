package com.testSpring.store.controllers;

import com.testSpring.store.entities.Status;
import com.testSpring.store.services.StatusServices;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/statuses")
public class StatusRestController {

    private final StatusServices statusServices;

    public StatusRestController(StatusServices statusServices) {
        this.statusServices = statusServices;
    }

    @GetMapping
    public List<Status> getAllStatuses() {
        return statusServices.getAllStatuses();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Status> getStatus(@PathVariable int id) {
        Optional<Status> status = statusServices.getStatus(id);
        return status.isEmpty()
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(status.get());
    }

    @PostMapping
    public ResponseEntity<Status> addStatus(@RequestBody Status status) {
        statusServices.addStatus(status);
        return ResponseEntity.status(HttpStatus.CREATED).body(status);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateStatus(@PathVariable int id, @RequestBody Status status) {
        if (statusServices.getStatus(id).isEmpty()) return ResponseEntity.notFound().build();
        statusServices.updateStatus(status, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStatus(@PathVariable int id) {
        Optional<Status> status = statusServices.getStatus(id);
        if (status.isEmpty()) return ResponseEntity.notFound().build();
        statusServices.deleteStatus(id);
        return ResponseEntity.noContent().build();
    }
}
