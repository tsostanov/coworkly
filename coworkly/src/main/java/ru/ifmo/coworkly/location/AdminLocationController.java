package ru.ifmo.coworkly.location;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/locations")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Locations", description = "Administrative location management")
@SecurityRequirement(name = "bearerAuth")
public class AdminLocationController {

    private final LocationService locationService;

    public AdminLocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    @Operation(summary = "List all locations")
    public List<LocationResponse> list() {
        return locationService.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new location")
    public LocationResponse create(@Valid @RequestBody LocationCreateRequest request) {
        return locationService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing location")
    public LocationResponse update(@PathVariable Long id, @Valid @RequestBody LocationCreateRequest request) {
        return locationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a location")
    public void delete(@PathVariable Long id) {
        locationService.delete(id);
    }
}
