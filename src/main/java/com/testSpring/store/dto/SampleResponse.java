package com.testSpring.store.dto;

import com.testSpring.store.entities.Sample;
import java.time.LocalDate;

public record SampleResponse(
        int id, String sampleCode, String type, String description, String state,
        Integer createdBy, LocalDate dateReception, LocalDate dateCreation,
        LocalDate dateModified, boolean active, Long version,
        StatusSummary status, TechnicianSummary technician) {

    public static SampleResponse from(Sample sample) {
        var status = sample.getStatus() == null ? null
                : new StatusSummary(sample.getStatus().getId(), sample.getStatus().getName(), sample.getStatus().getCode());
        var technician = sample.getTechnician() == null ? null
                : new TechnicianSummary(sample.getTechnician().getID(), sample.getTechnician().getName(),
                sample.getTechnician().getEmail(), sample.getTechnician().getRole());
        return new SampleResponse(sample.getId(), sample.getSampleCode(), sample.getType(), sample.getDescription(),
                sample.getState(), sample.getCreatedBy(), sample.getDateReception(), sample.getDateCreation(),
                sample.getDateModified(), sample.isActive(), sample.getVersion(), status, technician);
    }

    public record StatusSummary(int id, String name, String code) { }
    public record TechnicianSummary(int id, String name, String email, String role) { }
}
