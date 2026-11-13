package com.testSpring.store.dto;

import jakarta.validation.constraints.Pattern;

public record UserUpdateRequest(String name,
                                @Pattern(regexp = "(?i)(ROLE_)?(ADMIN|TECHNICIAN|TECNICO)") String role,
                                String department, String location, String password) { }
