package com.testSpring.store.controllers;

import java.time.LocalDateTime;

public record SampleStatusHistoryResponse(
        String previousStatusCode,
        String newStatusCode,
        LocalDateTime changedAt,
        String changedBy) {
}
