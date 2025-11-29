package ru.ifmo.coworkly.penalty;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ifmo.coworkly.penalty.dto.PenaltyResponse;
import ru.ifmo.coworkly.security.UserPrincipal;

@RestController
@RequestMapping("/api/penalties")
@PreAuthorize("hasAnyRole('RESIDENT','ADMIN')")
public class UserPenaltyController {

    private final PenaltyService penaltyService;

    public UserPenaltyController(PenaltyService penaltyService) {
        this.penaltyService = penaltyService;
    }

    @GetMapping("/me")
    public List<PenaltyResponse> myPenalties(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return penaltyService.activeForUser(principal.id());
    }
}
