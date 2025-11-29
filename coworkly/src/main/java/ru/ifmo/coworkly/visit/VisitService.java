package ru.ifmo.coworkly.visit;

import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import ru.ifmo.coworkly.booking.Booking;
import ru.ifmo.coworkly.booking.BookingRepository;
import ru.ifmo.coworkly.visit.dto.ExtendVisitRequest;
import ru.ifmo.coworkly.visit.dto.VisitResponse;

@Service
@Transactional
public class VisitService {

    private final VisitRepository visitRepository;
    private final BookingRepository bookingRepository;

    public VisitService(VisitRepository visitRepository, BookingRepository bookingRepository) {
        this.visitRepository = visitRepository;
        this.bookingRepository = bookingRepository;
    }

    public VisitResponse checkIn(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Бронь не найдена"));

        if (visitRepository.findByBookingIdAndStatus(bookingId, VisitStatus.ACTIVE).isPresent()) {
            throw new IllegalStateException("Есть активный визит по этой брони");
        }
        OffsetDateTime now = OffsetDateTime.now();
        Visit visit = new Visit();
        visit.setBookingId(booking.getId());
        visit.setUserId(booking.getUserId());
        visit.setSpaceId(booking.getSpace().getId());
        visit.setCheckIn(now);
        visit.setPlannedEnd(booking.getEndsAt());
        visit.setStatus(VisitStatus.ACTIVE);
        return toDto(visitRepository.save(visit));
    }

    public VisitResponse checkOut(Long visitId) {
        Visit visit = getActive(visitId);
        visit.setCheckOut(OffsetDateTime.now());
        visit.setStatus(VisitStatus.COMPLETED);
        return toDto(visitRepository.save(visit));
    }

    public VisitResponse extend(Long visitId, ExtendVisitRequest request) {
        Visit visit = getActive(visitId);
        if (!request.newPlannedEnd().isAfter(visit.getPlannedEnd())) {
            throw new IllegalArgumentException("Новая дата окончания должна быть позже текущей");
        }
        visit.setPlannedEnd(request.newPlannedEnd());
        return toDto(visitRepository.save(visit));
    }

    public List<VisitResponse> active() {
        return visitRepository.findActive().stream().map(this::toDto).toList();
    }

    public List<VisitResponse> expiring(int minutes) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime to = now.plusMinutes(minutes);
        return visitRepository.findExpiring(now, to).stream().map(this::toDto).toList();
    }

    public List<VisitResponse> overdue() {
        OffsetDateTime now = OffsetDateTime.now();
        List<Visit> visits = visitRepository.findOverdue(now);
        visits.forEach(v -> v.setStatus(VisitStatus.OVERDUE));
        visitRepository.saveAll(visits);
        return visits.stream().map(this::toDto).toList();
    }

    private Visit getActive(Long id) {
        Visit visit = visitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Визит не найден"));
        if (visit.getStatus() != VisitStatus.ACTIVE) {
            throw new IllegalStateException("Визит не активен");
        }
        return visit;
    }

    private VisitResponse toDto(Visit visit) {
        return new VisitResponse(
                visit.getId(),
                visit.getBookingId(),
                visit.getUserId(),
                visit.getSpaceId(),
                visit.getCheckIn(),
                visit.getPlannedEnd(),
                visit.getCheckOut(),
                visit.getStatus()
        );
    }
}
