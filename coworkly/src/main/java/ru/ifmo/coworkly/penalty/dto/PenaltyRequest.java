package ru.ifmo.coworkly.penalty.dto;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import ru.ifmo.coworkly.penalty.PenaltyType;

public record PenaltyRequest(
        @NotNull Long userId,
        @NotNull PenaltyType type,
        String reason,
        Integer limitMinutes,
        Long amountCents,
        OffsetDateTime expiresAt
) {
}
