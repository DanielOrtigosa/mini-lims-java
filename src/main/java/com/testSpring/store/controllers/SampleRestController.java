package com.testSpring.store.controllers;

import com.testSpring.store.entities.Sample;
import com.testSpring.store.services.SampleServices;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.Map;

/** REST endpoints for working with samples independently of the form-based controllers. */
@RestController
@RequestMapping("/api/samples")
public class SampleRestController {

    private final SampleServices sampleServices;

    public SampleRestController(SampleServices sampleServices) {
        this.sampleServices = sampleServices;
    }

    @GetMapping
    public List<Sample> getAllSamples() {
        return sampleServices.getAllSamples();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sample> getSample(@PathVariable int id) {
        if (id <= 0) return ResponseEntity.badRequest().build();
        Optional<Sample> sample = sampleServices.getSample(id);
        return sample.isEmpty()
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(sample.get());
    }

    @PostMapping
    public ResponseEntity<?> addSample(@RequestBody(required = false) Sample sample) {
        String validationError = validateSample(sample);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(Map.of("error", validationError));
        }
        sampleServices.addSample(sample);
        return ResponseEntity.status(HttpStatus.CREATED).body(sample);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSample(@PathVariable int id, @RequestBody(required = false) Sample sample) {
        if (id <= 0) return ResponseEntity.badRequest().body(Map.of("error", "El id debe ser mayor que cero"));
        String validationError = validateSample(sample);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(Map.of("error", validationError));
        }
        if (sampleServices.getSample(id).isEmpty()) return ResponseEntity.notFound().build();
        sampleServices.updateSample(sample, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/technician/{technicianId}")
    public ResponseEntity<?> assignTechnician(@PathVariable int id, @PathVariable int technicianId) {
        if (id <= 0 || technicianId <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Los ids deben ser mayores que cero"));
        }
        try {
            return ResponseEntity.ok(sampleServices.assignTechnician(id, technicianId));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateSample(@PathVariable int id) {
        if (id <= 0) return ResponseEntity.badRequest().build();
        Optional<Sample> sample = sampleServices.getSample(id);
        if (sample.isEmpty()) return ResponseEntity.notFound().build();
        sampleServices.deleteSample(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/status/advance")
    public ResponseEntity<?> advanceSampleStatus(@PathVariable int id) {
        if (id <= 0) return ResponseEntity.badRequest().body(Map.of("error", "El id debe ser mayor que cero"));
        try {
            return ResponseEntity.ok(sampleServices.advanceSampleStatus(id));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/{id}/status/history")
    public ResponseEntity<?> getSampleStatusHistory(@PathVariable int id) {
        if (id <= 0) return ResponseEntity.badRequest().body(Map.of("error", "El id debe ser mayor que cero"));
        try {
            List<SampleStatusHistoryResponse> history = sampleServices.getSampleStatusHistory(id).stream()
                    .map(entry -> new SampleStatusHistoryResponse(
                            entry.getPreviousStatusCode(), entry.getNewStatusCode(), entry.getChangedAt()))
                    .toList();
            return ResponseEntity.ok(history);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    private String validateSample(Sample sample) {
        if (sample == null) return "El cuerpo de la muestra es obligatorio";
        if (sample.getSampleCode() == null || sample.getSampleCode().isBlank()) {
            return "El código de la muestra es obligatorio";
        }
        if (sample.getType() == null || sample.getType().isBlank()) {
            return "El tipo de muestra es obligatorio";
        }
        return null;
    }
}
