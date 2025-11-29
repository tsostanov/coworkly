package ru.ifmo.coworkly.user.dto;

public record UserResponse(
        Long id,
        String email,
        String fullName,
        String role,
        String status
) {
}
