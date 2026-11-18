package ru.ifmo.coworkly.visit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ifmo.coworkly.booking.Booking;
import ru.ifmo.coworkly.booking.BookingRepository;
import ru.ifmo.coworkly.booking.BookingStatus;
import ru.ifmo.coworkly.space.Space;

@ExtendWith(MockitoExtension.class)
class VisitServiceTest {
    @Mock VisitRepository visits;
    @Mock BookingRepository bookings;
    VisitService service;
    Booking booking;

    @BeforeEach
    void setUp() {
        service = new VisitService(visits, bookings);
        booking = new Booking();
        booking.setId(7L);
        booking.setUserId(2L);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setEndsAt(OffsetDateTime.now().plusHours(2));
        Space space = new Space();
        space.setId(3L);
        booking.setSpace(space);
        when(bookings.findById(7L)).thenReturn(Optional.of(booking));
    }

    @Test
    void checkInLinksVisitToResidentBooking() {
        when(visits.save(any())).thenAnswer(invocation -> {
            Visit visit = invocation.getArgument(0);
            visit.setId(11L);
            return visit;
        });
        var response = service.checkIn(7L);
        assertThat(response.bookingId()).isEqualTo(7L);
        assertThat(response.userId()).isEqualTo(2L);
        assertThat(response.spaceId()).isEqualTo(3L);
        assertThat(response.status()).isEqualTo(VisitStatus.ACTIVE);
        assertThat(response.checkIn()).isNotNull();
    }

    @Test
    void checkInRejectsUnconfirmedBooking() {
        booking.setStatus(BookingStatus.PENDING);
        assertThatThrownBy(() -> service.checkIn(7L)).isInstanceOf(IllegalStateException.class);
        verify(visits, never()).save(any());
    }

    @Test
    void checkInRejectsExpiredBooking() {
        booking.setEndsAt(OffsetDateTime.now().minusMinutes(1));
        assertThatThrownBy(() -> service.checkIn(7L)).isInstanceOf(IllegalStateException.class);
        verify(visits, never()).save(any());
    }

    @Test
    void checkInRejectsDuplicateActiveVisit() {
        when(visits.findByBookingIdAndStatus(7L, VisitStatus.ACTIVE)).thenReturn(Optional.of(new Visit()));
        assertThatThrownBy(() -> service.checkIn(7L)).isInstanceOf(IllegalStateException.class);
        verify(visits, never()).save(any());
    }
}
