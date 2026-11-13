package com.testSpring.store.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record SampleRequest(
        @NotBlank String sampleCode,
        @NotBlank String type,
        String description,
        LocalDate dateReception) { }
