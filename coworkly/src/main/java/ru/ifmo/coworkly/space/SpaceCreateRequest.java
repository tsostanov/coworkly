package ru.ifmo.coworkly.space;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SpaceCreateRequest(
        @NotNull Long locationId,
        @NotBlank String name,
        @NotNull @Min(1) Integer capacity,
        @NotNull SpaceType type,
        Long tariffPlanId,
        Boolean active
) {
}
