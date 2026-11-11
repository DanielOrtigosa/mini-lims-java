package com.testSpring.store.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record ResultRequest(@Valid @NotNull SampleReference sample,
                            @NotBlank String resultValue,
                            @NotNull LocalDate resultDate) {
    public record SampleReference(@NotNull @Positive Integer id) { }
}
