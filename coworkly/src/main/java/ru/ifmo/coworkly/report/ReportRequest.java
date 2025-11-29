package ru.ifmo.coworkly.report;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record ReportRequest(
        @NotNull OffsetDateTime from,
        @NotNull OffsetDateTime to,
        Long locationId
) {
}
