package com.testSpring.store.controllers;

import com.testSpring.store.entities.Sample;
import com.testSpring.store.dto.SampleRequest;
import com.testSpring.store.dto.SampleUpdateRequest;
import com.testSpring.store.dto.SampleResponse;
import com.testSpring.store.dto.ActivationRequest;
import com.testSpring.store.services.SampleServices;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/** REST endpoints for the sample workflow. */
@RestController
@RequestMapping("/api/samples")
public class SampleRestController {

    private final SampleServices sampleServices;

    public SampleRestController(SampleServices sampleServices) {
        this.sampleServices = sampleServices;
    }

    @GetMapping
    public Page<SampleResponse> getAllSamples(Pageable pageable) {
        return sampleServices.getAllSamples(pageable).map(SampleResponse::from);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SampleResponse> getSample(@PathVariable int id) {
        if (id <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El id debe ser mayor que cero");
        Sample sample = sampleServices.getSample(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la muestra " + id));
        return ResponseEntity.ok(SampleResponse.from(sample));
    }

    @PostMapping
    public ResponseEntity<SampleResponse> addSample(@Valid @RequestBody SampleRequest request,
                                                    @AuthenticationPrincipal Jwt jwt) {
        Sample sample = new Sample();
        sample.setSampleCode(request.sampleCode());
        sample.setType(request.type());
        sample.setDescription(request.description());
        sample.setDateReception(request.dateReception());
        sampleServices.addSample(sample, jwt == null ? null : jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(SampleResponse.from(sample));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateSample(@PathVariable int id, @Valid @RequestBody SampleUpdateRequest request) {
        if (id <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El id debe ser mayor que cero");
        Sample sample = new Sample();
        sample.setSampleCode(request.sampleCode());
        sample.setType(request.type());
        sample.setDescription(request.description());
        sample.setVersion(request.version());
        sampleServices.updateSample(sample, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/technician/{technicianId}")
    public ResponseEntity<?> assignTechnician(@PathVariable int id, @PathVariable int technicianId) {
        if (id <= 0 || technicianId <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los ids deben ser mayores que cero");
        return ResponseEntity.ok(SampleResponse.from(sampleServices.assignTechnician(id, technicianId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateSample(@PathVariable int id) {
        if (id <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El id debe ser mayor que cero");
        if (sampleServices.getSample(id).isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la muestra " + id);
        sampleServices.deleteSample(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activation")
    public ResponseEntity<SampleResponse> setActivation(@PathVariable int id,
                                                        @Valid @RequestBody ActivationRequest request) {
        if (id <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El id debe ser mayor que cero");
        if (sampleServices.getSample(id).isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la muestra " + id);
        if (request.active()) sampleServices.activateSample(id);
        else sampleServices.deleteSample(id);
        return ResponseEntity.ok(sampleServices.getSample(id).map(SampleResponse::from).orElseThrow());
    }

    @PostMapping("/{id}/status/advance")
    public ResponseEntity<?> advanceSampleStatus(@PathVariable int id, @AuthenticationPrincipal Jwt jwt) {
        if (id <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El id debe ser mayor que cero");
        return ResponseEntity.ok(SampleResponse.from(sampleServices.advanceSampleStatus(id,
                jwt == null ? null : jwt.getSubject())));
    }

    @GetMapping("/{id}/status/history")
    public ResponseEntity<?> getSampleStatusHistory(@PathVariable int id) {
        if (id <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El id debe ser mayor que cero");
        List<SampleStatusHistoryResponse> history = sampleServices.getSampleStatusHistory(id).stream()
                .map(entry -> new SampleStatusHistoryResponse(
                        entry.getPreviousStatusCode(), entry.getNewStatusCode(), entry.getChangedAt(), entry.getChangedBy()))
                .toList();
        return ResponseEntity.ok(history);
    }

}
