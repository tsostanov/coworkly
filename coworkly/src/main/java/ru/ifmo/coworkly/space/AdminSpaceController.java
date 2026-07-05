package ru.ifmo.coworkly.space;

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
@RequestMapping("/api/admin/spaces")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Spaces", description = "Administrative space management")
@SecurityRequirement(name = "bearerAuth")
public class AdminSpaceController {

    private final SpaceService spaceService;

    public AdminSpaceController(SpaceService spaceService) {
        this.spaceService = spaceService;
    }

    @GetMapping
    @Operation(summary = "List active spaces")
    public List<SpaceResponse> list() {
        return spaceService.getAllActiveSpaces();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new space")
    public SpaceResponse create(@Valid @RequestBody SpaceCreateRequest request) {
        return spaceService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing space")
    public SpaceResponse update(@PathVariable Long id, @Valid @RequestBody SpaceCreateRequest request) {
        return spaceService.update(id, request);
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate a space")
    public SpaceResponse activate(@PathVariable Long id) {
        return spaceService.toggleActive(id, true);
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a space")
    public SpaceResponse deactivate(@PathVariable Long id) {
        return spaceService.toggleActive(id, false);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a space")
    public void delete(@PathVariable Long id) {
        spaceService.delete(id);
    }
}
