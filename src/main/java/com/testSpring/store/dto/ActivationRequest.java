package com.testSpring.store.dto;

import jakarta.validation.constraints.NotNull;

public record ActivationRequest(@NotNull Boolean active) { }
