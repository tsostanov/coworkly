package ru.ifmo.coworkly.booking;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BookingLifecycleScheduler {

    private final BookingService bookingService;

    public BookingLifecycleScheduler(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Scheduled(fixedDelayString = "${app.booking.finalize-interval-ms:60000}")
    public void finalizeExpiredBookings() {
        bookingService.finalizeExpiredBookings();
    }
}
