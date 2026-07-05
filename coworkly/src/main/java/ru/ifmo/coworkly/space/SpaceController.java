package ru.ifmo.coworkly.space;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/spaces")
@Validated
@PreAuthorize("hasAnyRole('RESIDENT','ADMIN')")
@Tag(name = "Spaces", description = "Resident-facing workspace search and catalog")
@SecurityRequirement(name = "bearerAuth")
public class SpaceController {

    private final SpaceService spaceService;

    public SpaceController(SpaceService spaceService) {
        this.spaceService = spaceService;
    }

    @GetMapping
    @Operation(summary = "List active spaces across all locations")
    public List<SpaceResponse> getSpaces() {
        return spaceService.getAllActiveSpaces();
    }

    @GetMapping("/location/{locationId}")
    @Operation(summary = "List active spaces for a location")
    public List<SpaceResponse> getSpacesByLocation(@PathVariable Long locationId) {
        return spaceService.getActiveSpacesByLocation(locationId);
    }

    @GetMapping("/free")
    @Operation(summary = "Search for free spaces in a time range")
    public List<FreeSpaceResponse> getFreeSpaces(@RequestParam Long locationId,
                                                 @RequestParam("from")
                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                 OffsetDateTime from,
                                                 @RequestParam("to")
                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                 OffsetDateTime to,
                                                 @RequestParam(value = "capacity", required = false)
                                                 Integer capacity) {
        return spaceService.findFreeSpaces(locationId, from, to, capacity);
    }
}
