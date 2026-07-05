package ru.ifmo.coworkly.location;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/locations")
@PreAuthorize("hasAnyRole('RESIDENT','ADMIN')")
@Tag(name = "Locations", description = "Resident-facing location catalog")
@SecurityRequirement(name = "bearerAuth")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    @Operation(summary = "List all available coworking locations")
    public List<LocationResponse> getLocations() {
        return locationService.getAll();
    }
}
