package ru.ifmo.coworkly.user.dto;

public record AuthResponse(
        String token,
        Long userId,
        String email,
        String fullName,
        String role
) {
}
