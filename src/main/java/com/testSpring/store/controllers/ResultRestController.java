package com.testSpring.store.controllers;

import com.testSpring.store.entities.Result;
import com.testSpring.store.services.ResultServices;
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
@RequestMapping("/api/results")
public class ResultRestController {

    private final ResultServices resultServices;

    public ResultRestController(ResultServices resultServices) {
        this.resultServices = resultServices;
    }

    @GetMapping
    public List<Result> getAllResults() {
        return resultServices.getAllResults();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result> getResult(@PathVariable int id) {
        if (id <= 0) return ResponseEntity.badRequest().build();
        Optional<Result> result = resultServices.getResult(id);
        return result.isEmpty()
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(result.get());
    }

    @PostMapping
    public ResponseEntity<?> addResult(@RequestBody(required = false) Result result) {
        String validationError = validateResult(result);
        if (validationError != null) return ResponseEntity.badRequest().body(Map.of("error", validationError));
        try {
            resultServices.addResult(result);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateResult(@PathVariable int id, @RequestBody(required = false) Result result) {
        if (id <= 0) return ResponseEntity.badRequest().body(Map.of("error", "El id debe ser mayor que cero"));
        String validationError = validateResult(result);
        if (validationError != null) return ResponseEntity.badRequest().body(Map.of("error", validationError));
        if (resultServices.getResult(id).isEmpty()) return ResponseEntity.notFound().build();
        try {
            resultServices.updateResult(result, id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResult(@PathVariable int id) {
        if (id <= 0) return ResponseEntity.badRequest().build();
        Optional<Result> result = resultServices.getResult(id);
        if (result.isEmpty()) return ResponseEntity.notFound().build();
        resultServices.deleteResult(id);
        return ResponseEntity.noContent().build();
    }

    private String validateResult(Result result) {
        if (result == null) return "El cuerpo del resultado es obligatorio";
        if (result.getSample() == null || result.getSample().getId() <= 0) {
            return "Debe indicarse una muestra existente";
        }
        if (result.getResultValue() == null || result.getResultValue().isBlank()) {
            return "El valor del resultado es obligatorio";
        }
        if (result.getResultDate() == null) return "La fecha del resultado es obligatoria";
        return null;
    }
}
