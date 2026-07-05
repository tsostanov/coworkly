package ru.ifmo.coworkly.visit;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.ifmo.coworkly.visit.dto.ExtendVisitRequest;
import ru.ifmo.coworkly.visit.dto.VisitResponse;

@RestController
@RequestMapping("/api/admin/visits")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Visits", description = "Visit lifecycle management for checked-in residents")
@SecurityRequirement(name = "bearerAuth")
public class VisitController {

    private final VisitService visitService;

    public VisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @PostMapping("/checkin")
    @Operation(summary = "Check in a resident by booking ID")
    public VisitResponse checkIn(@RequestParam Long bookingId) {
        return visitService.checkIn(bookingId);
    }

    @PostMapping("/{id}/checkout")
    @Operation(summary = "Check out an active visit")
    public VisitResponse checkout(@PathVariable Long id) {
        return visitService.checkOut(id);
    }

    @PostMapping("/{id}/extend")
    @Operation(summary = "Extend an active visit")
    public VisitResponse extend(@PathVariable Long id, @Valid @RequestBody ExtendVisitRequest request) {
        return visitService.extend(id, request);
    }

    @GetMapping("/active")
    @Operation(summary = "List active visits")
    public List<VisitResponse> active() {
        return visitService.active();
    }

    @GetMapping("/expiring")
    @Operation(summary = "List visits that will expire soon")
    public List<VisitResponse> expiring(@RequestParam(defaultValue = "15") int minutes) {
        return visitService.expiring(minutes);
    }

    @GetMapping("/overdue")
    @Operation(summary = "List overdue visits")
    public List<VisitResponse> overdue() {
        return visitService.overdue();
    }
}
