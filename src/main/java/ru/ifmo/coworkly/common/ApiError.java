package ru.ifmo.coworkly.common;

import java.time.OffsetDateTime;

public record ApiError(String message, OffsetDateTime timestamp) {
    public ApiError(String message) {
        this(message, OffsetDateTime.now());
    }
}
