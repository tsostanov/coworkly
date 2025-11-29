package ru.ifmo.coworkly.booking;

import jakarta.validation.Valid;
import java.security.SecureRandom;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ifmo.coworkly.user.User;
import ru.ifmo.coworkly.user.UserRole;
import ru.ifmo.coworkly.user.UserService;

@RestController
@RequestMapping("/api/admin/walkin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminWalkInController {

    private final BookingService bookingService;
    private final UserService userService;
    private final SecureRandom random = new SecureRandom();
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";

    public AdminWalkInController(BookingService bookingService, UserService userService) {
        this.bookingService = bookingService;
        this.userService = userService;
    }

    @PostMapping
    public WalkInBookingResponse createWalkIn(@Valid @RequestBody WalkInBookingRequest request) {
        String normalizedEmail = request.email().toLowerCase();
        User user = userService.findByEmail(normalizedEmail)
                .map(userService::ensureActive)
                .orElse(null);

        String tempPassword = null;
        boolean existing = user != null;
        if (user == null) {
            tempPassword = generatePassword(12);
            user = userService.register(normalizedEmail, tempPassword, request.fullName(), UserRole.RESIDENT);
        }

        Long bookingId = bookingService.createBooking(
                new CreateBookingRequest(user.getId(), request.spaceId(), request.startsAt(), request.endsAt())
        );
        return new WalkInBookingResponse(user.getId(), bookingId, tempPassword, existing);
    }

    private String generatePassword(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int idx = random.nextInt(ALPHABET.length());
            sb.append(ALPHABET.charAt(idx));
        }
        return sb.toString();
    }
}
