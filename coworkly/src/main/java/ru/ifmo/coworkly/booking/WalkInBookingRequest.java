package ru.ifmo.coworkly.booking;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record WalkInBookingRequest(
        @NotBlank @Email String email,
        @NotBlank String fullName,
        @NotNull Long spaceId,
        @NotNull OffsetDateTime startsAt,
        @NotNull OffsetDateTime endsAt
) {
}
