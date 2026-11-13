package com.testSpring.store.entities;

import java.util.Locale;

public enum Role {
    ADMIN,
    TECHNICIAN,
    TECNICO;

    public static Role from(String value) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("ROLE_")) normalized = normalized.substring("ROLE_".length());
        return Role.valueOf(normalized);
    }
}
