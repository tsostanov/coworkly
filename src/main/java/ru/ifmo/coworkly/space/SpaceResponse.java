package ru.ifmo.coworkly.space;

public record SpaceResponse(
        Long id,
        Long locationId,
        String locationName,
        String locationAddress,
        String name,
        Integer capacity,
        SpaceType type,
        Long tariffPlanId,
        Boolean active
) {
}
