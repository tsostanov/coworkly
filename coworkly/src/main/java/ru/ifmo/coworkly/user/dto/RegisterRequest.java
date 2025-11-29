package ru.ifmo.coworkly.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email(message = "Введите корректный email")
        @NotBlank(message = "Email обязателен")
        String email,

        @NotBlank(message = "Пароль обязателен")
        @Size(min = 6, message = "Пароль должен быть не короче 6 символов")
        String password,

        @NotBlank(message = "Имя обязательно")
        String fullName
) {
}
