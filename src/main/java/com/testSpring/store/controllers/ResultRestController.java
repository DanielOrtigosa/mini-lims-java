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
        Optional<Result> result = resultServices.getResult(id);
        return result.isEmpty()
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(result.get());
    }

    @PostMapping
    public ResponseEntity<Result> addResult(@RequestBody Result result) {
        resultServices.addResult(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateResult(@PathVariable int id, @RequestBody Result result) {
        if (resultServices.getResult(id).isEmpty()) return ResponseEntity.notFound().build();
        resultServices.updateResult(result, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResult(@PathVariable int id) {
        Optional<Result> result = resultServices.getResult(id);
        if (result.isEmpty()) return ResponseEntity.notFound().build();
        resultServices.deleteResult(id);
        return ResponseEntity.noContent().build();
    }
}
