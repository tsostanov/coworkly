package ru.ifmo.coworkly.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import ru.ifmo.coworkly.penalty.PenaltyService;
import ru.ifmo.coworkly.user.User;
import ru.ifmo.coworkly.user.UserService;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserService userService;

    @Mock
    private PenaltyService penaltyService;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(jdbcTemplate, bookingRepository, userService, penaltyService);
    }

    @Test
    void createBookingRejectsEndBeforeOrEqualStart() {
        OffsetDateTime startsAt = OffsetDateTime.parse("2026-07-05T10:00:00Z");
        CreateBookingRequest request = new CreateBookingRequest(1L, 2L, startsAt, startsAt);

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("endsAt must be after startsAt");

        verify(userService, never()).getById(anyLong());
        verify(penaltyService, never()).validateBooking(anyLong(), any(Duration.class));
        verify(jdbcTemplate, never()).queryForObject(any(String.class), eq(Long.class), any(), any(), any(), any());
    }

    @Test
    void createBookingValidatesDependenciesAndReturnsBookingId() {
        OffsetDateTime startsAt = OffsetDateTime.parse("2026-07-05T10:00:00Z");
        OffsetDateTime endsAt = OffsetDateTime.parse("2026-07-05T12:00:00Z");
        CreateBookingRequest request = new CreateBookingRequest(1L, 2L, startsAt, endsAt);
        User user = new User();
        user.setId(1L);

        when(userService.getById(1L)).thenReturn(user);
        when(jdbcTemplate.queryForObject(
                any(String.class),
                eq(Long.class),
                eq(1L),
                eq(2L),
                any(Timestamp.class),
                any(Timestamp.class)))
                .thenReturn(42L);

        Long bookingId = bookingService.createBooking(request);

        assertThat(bookingId).isEqualTo(42L);
        verify(userService).getById(1L);
        verify(userService).ensureActive(user);
        verify(penaltyService).validateBooking(1L, Duration.ofHours(2));
    }

    @Test
    void cancelBookingRejectsAnotherUsersBookingForResident() {
        Booking booking = new Booking();
        booking.setId(10L);
        booking.setUserId(99L);
        booking.setStatus(BookingStatus.PENDING);
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(10L, 1L, false))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Cannot cancel another user's booking");
    }

    @Test
    void cancelBookingRejectsNonPendingBookingForResident() {
        Booking booking = new Booking();
        booking.setId(10L);
        booking.setUserId(1L);
        booking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(10L, 1L, false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only pending booking can be canceled");
    }
}
