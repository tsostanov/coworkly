package ru.ifmo.coworkly.booking;

public record WalkInBookingResponse(
        Long userId,
        Long bookingId,
        String tempPassword,
        boolean existingUser,
        String userEmail,
        String userFullName
) {
}
