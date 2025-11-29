package ru.ifmo.coworkly.visit.dto;

import java.time.OffsetDateTime;
import ru.ifmo.coworkly.visit.VisitStatus;

public record VisitResponse(
        Long id,
        Long bookingId,
        Long userId,
        Long spaceId,
        OffsetDateTime checkIn,
        OffsetDateTime plannedEnd,
        OffsetDateTime checkOut,
        VisitStatus status
) {
}
