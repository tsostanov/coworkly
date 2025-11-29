package ru.ifmo.coworkly.penalty;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.ifmo.coworkly.penalty.dto.PenaltyRequest;
import ru.ifmo.coworkly.penalty.dto.PenaltyResponse;
import ru.ifmo.coworkly.security.UserPrincipal;

@RestController
@RequestMapping("/api/admin/penalties")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPenaltyController {

    private final PenaltyService penaltyService;

    public AdminPenaltyController(PenaltyService penaltyService) {
        this.penaltyService = penaltyService;
    }

    @PostMapping
    public PenaltyResponse create(@Valid @RequestBody PenaltyRequest request, Authentication authentication) {
        UserPrincipal admin = (UserPrincipal) authentication.getPrincipal();
        return penaltyService.create(request, admin);
    }

    @GetMapping
    public List<PenaltyResponse> list(@RequestParam(value = "userId", required = false) Long userId,
                                      @RequestParam(value = "activeOnly", defaultValue = "false") boolean activeOnly) {
        return penaltyService.list(userId, activeOnly);
    }

    @DeleteMapping("/{id}")
    public void revoke(@PathVariable Long id) {
        penaltyService.revoke(id);
    }
}
