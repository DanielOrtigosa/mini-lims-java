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
import java.util.Map;

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
        if (id <= 0) return ResponseEntity.badRequest().build();
        Optional<Status> status = statusServices.getStatus(id);
        return status.isEmpty()
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(status.get());
    }

    @PostMapping
    public ResponseEntity<?> addStatus(@RequestBody(required = false) Status status) {
        String validationError = validateStatus(status);
        if (validationError != null) return ResponseEntity.badRequest().body(Map.of("error", validationError));
        statusServices.addStatus(status);
        return ResponseEntity.status(HttpStatus.CREATED).body(status);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable int id, @RequestBody(required = false) Status status) {
        if (id <= 0) return ResponseEntity.badRequest().body(Map.of("error", "El id debe ser mayor que cero"));
        String validationError = validateStatus(status);
        if (validationError != null) return ResponseEntity.badRequest().body(Map.of("error", validationError));
        if (statusServices.getStatus(id).isEmpty()) return ResponseEntity.notFound().build();
        statusServices.updateStatus(status, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStatus(@PathVariable int id) {
        if (id <= 0) return ResponseEntity.badRequest().build();
        Optional<Status> status = statusServices.getStatus(id);
        if (status.isEmpty()) return ResponseEntity.notFound().build();
        try {
            statusServices.deleteStatus(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    private String validateStatus(Status status) {
        if (status == null) return "El cuerpo del estado es obligatorio";
        if (status.getName() == null || status.getName().isBlank()) return "El nombre del estado es obligatorio";
        if (status.getCode() == null || status.getCode().isBlank()) return "El código del estado es obligatorio";
        return null;
    }
}
