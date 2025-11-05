package ru.ifmo.coworkly.booking;

import java.time.OffsetDateTime;
import ru.ifmo.coworkly.space.SpaceType;

public record BookingResponse(
        Long id,
        Long userId,
        Long spaceId,
        String spaceName,
        SpaceType spaceType,
        Long locationId,
        String locationName,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        BookingStatus status,
        Long totalCents,
        OffsetDateTime createdAt
) {
}
