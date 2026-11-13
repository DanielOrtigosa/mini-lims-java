package com.testSpring.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SampleUpdateRequest(
        @NotBlank String sampleCode,
        @NotBlank String type,
        String description,
        @NotNull @PositiveOrZero Long version) { }
