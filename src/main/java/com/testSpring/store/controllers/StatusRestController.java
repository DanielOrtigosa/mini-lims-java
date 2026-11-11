package com.testSpring.store.controllers;

import com.testSpring.store.entities.Status;
import com.testSpring.store.services.StatusServices;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/api/statuses")
public class StatusRestController {

    private final StatusServices statusServices;

    public StatusRestController(StatusServices statusServices) {
        this.statusServices = statusServices;
    }

    @GetMapping
    public Page<Status> getAllStatuses(Pageable pageable) {
        return statusServices.getAllStatuses(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Status> getStatus(@PathVariable int id) {
        if (id <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El id debe ser mayor que cero");
        Status status = statusServices.getStatus(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el estado " + id));
        return ResponseEntity.ok(status);
    }

    @PostMapping
    public ResponseEntity<?> addStatus(@Valid @RequestBody Status status) {
        statusServices.addStatus(status);
        return ResponseEntity.status(HttpStatus.CREATED).body(status);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable int id, @Valid @RequestBody Status status) {
        if (id <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El id debe ser mayor que cero");
        if (statusServices.getStatus(id).isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el estado " + id);
        statusServices.updateStatus(status, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStatus(@PathVariable int id) {
        if (id <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El id debe ser mayor que cero");
        Optional<Status> status = statusServices.getStatus(id);
        if (status.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el estado " + id);
        statusServices.deleteStatus(id);
        return ResponseEntity.noContent().build();
    }

}
