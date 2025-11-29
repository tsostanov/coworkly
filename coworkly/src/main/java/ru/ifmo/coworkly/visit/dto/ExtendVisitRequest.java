package ru.ifmo.coworkly.visit.dto;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record ExtendVisitRequest(
        @NotNull OffsetDateTime newPlannedEnd
) {
}
