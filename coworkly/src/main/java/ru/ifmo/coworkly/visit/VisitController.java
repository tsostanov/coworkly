package ru.ifmo.coworkly.visit;

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
public class VisitController {

    private final VisitService visitService;

    public VisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @PostMapping("/checkin")
    public VisitResponse checkIn(@RequestParam Long bookingId) {
        return visitService.checkIn(bookingId);
    }

    @PostMapping("/{id}/checkout")
    public VisitResponse checkout(@PathVariable Long id) {
        return visitService.checkOut(id);
    }

    @PostMapping("/{id}/extend")
    public VisitResponse extend(@PathVariable Long id, @Valid @RequestBody ExtendVisitRequest request) {
        return visitService.extend(id, request);
    }

    @GetMapping("/active")
    public List<VisitResponse> active() {
        return visitService.active();
    }

    @GetMapping("/expiring")
    public List<VisitResponse> expiring(@RequestParam(defaultValue = "15") int minutes) {
        return visitService.expiring(minutes);
    }

    @GetMapping("/overdue")
    public List<VisitResponse> overdue() {
        return visitService.overdue();
    }
}
