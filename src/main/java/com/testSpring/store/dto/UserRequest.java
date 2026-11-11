package com.testSpring.store.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserRequest(@NotBlank String name, @NotBlank @Email String email,
                          @NotBlank @Pattern(regexp = "(?i)(ROLE_)?(ADMIN|TECHNICIAN|TECNICO)") String role,
                          String department, String location,
                          @NotBlank String password) { }
