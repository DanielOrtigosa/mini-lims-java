package com.testSpring.store.controllers;

import com.testSpring.store.entities.Result;
import com.testSpring.store.entities.Sample;
import com.testSpring.store.dto.ResultRequest;
import com.testSpring.store.dto.ResultResponse;
import com.testSpring.store.services.ResultServices;
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
    public Page<ResultResponse> getAllResults(Pageable pageable) {
        return resultServices.getAllResults(pageable).map(ResultResponse::from);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResultResponse> getResult(@PathVariable int id) {
        if (id <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El id debe ser mayor que cero");
        Result result = resultServices.getResult(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el resultado " + id));
        return ResponseEntity.ok(ResultResponse.from(result));
    }

    @PostMapping
    public ResponseEntity<ResultResponse> addResult(@Valid @RequestBody ResultRequest request) {
        Result result = new Result();
        Sample sample = new Sample();
        sample.setId(request.sample().id());
        result.setSample(sample);
        result.setResultValue(request.resultValue());
        result.setResultDate(request.resultDate());
        resultServices.addResult(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResultResponse.from(result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateResult(@PathVariable int id, @Valid @RequestBody ResultRequest request) {
        if (id <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El id debe ser mayor que cero");
        Result result = new Result();
        Sample sample = new Sample();
        sample.setId(request.sample().id());
        result.setSample(sample);
        result.setResultValue(request.resultValue());
        result.setResultDate(request.resultDate());
        if (resultServices.getResult(id).isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el resultado " + id);
        resultServices.updateResult(result, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResult(@PathVariable int id) {
        if (id <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El id debe ser mayor que cero");
        Optional<Result> result = resultServices.getResult(id);
        if (result.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el resultado " + id);
        resultServices.deleteResult(id);
        return ResponseEntity.noContent().build();
    }

}
