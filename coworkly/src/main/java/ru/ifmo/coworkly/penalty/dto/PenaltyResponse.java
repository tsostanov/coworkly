package ru.ifmo.coworkly.penalty.dto;

import java.time.OffsetDateTime;
import ru.ifmo.coworkly.penalty.PenaltyType;

public record PenaltyResponse(
        Long id,
        Long userId,
        PenaltyType type,
        String reason,
        Integer limitMinutes,
        Long amountCents,
        OffsetDateTime expiresAt,
        OffsetDateTime createdAt,
        OffsetDateTime revokedAt,
        Long createdByAdminId,
        boolean active
) {
}
