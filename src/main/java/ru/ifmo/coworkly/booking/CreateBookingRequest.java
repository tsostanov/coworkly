package ru.ifmo.coworkly.booking;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record CreateBookingRequest(
        @NotNull Long userId,
        @NotNull Long spaceId,
        @NotNull OffsetDateTime startsAt,
        @NotNull OffsetDateTime endsAt
) {
}
