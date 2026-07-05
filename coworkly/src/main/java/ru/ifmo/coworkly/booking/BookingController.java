package ru.ifmo.coworkly.booking;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
@Validated
@Tag(name = "Bookings", description = "Resident and admin booking management")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RESIDENT','ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a booking for a resident")
    public CreateBookingResponse createBooking(@Valid @RequestBody CreateBookingRequest request,
                                               Authentication authentication) {
        ensureOwnOrAdmin(authentication, request.userId());
        Long bookingId = bookingService.createBooking(request);
        return new CreateBookingResponse(bookingId);
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Confirm a pending booking")
    public void confirmBooking(@PathVariable Long id) {
        bookingService.confirmBooking(id);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('RESIDENT','ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cancel a booking")
    public void cancelBooking(@PathVariable Long id, Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof ru.ifmo.coworkly.security.UserPrincipal userPrincipal) {
            boolean isAdmin = userPrincipal.role().name().equals("ADMIN");
            bookingService.cancelBooking(id, userPrincipal.id(), isAdmin);
            return;
        }
        throw new AccessDeniedException("Authentication required");
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('RESIDENT','ADMIN')")
    @Operation(summary = "Get bookings for a specific user")
    public List<BookingResponse> getUserBookings(@PathVariable Long userId,
                                                 Authentication authentication) {
        ensureOwnOrAdmin(authentication, userId);
        return bookingService.getBookingsForUser(userId);
    }

    private void ensureOwnOrAdmin(Authentication authentication, Long userId) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof ru.ifmo.coworkly.security.UserPrincipal userPrincipal) {
            if (!userPrincipal.role().name().equals("ADMIN") && !userPrincipal.id().equals(userId)) {
                throw new AccessDeniedException("Доступ только к своим данным");
            }
        }
    }
}
