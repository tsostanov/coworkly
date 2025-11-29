package ru.ifmo.coworkly.location;

import jakarta.validation.constraints.NotBlank;

public record LocationCreateRequest(
        @NotBlank String name,
        @NotBlank String address
) {
}
