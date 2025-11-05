package ru.ifmo.coworkly.booking;

import jakarta.transaction.Transactional;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Service;
import ru.ifmo.coworkly.space.Space;
import ru.ifmo.coworkly.space.SpaceType;

@Service
@Transactional
public class BookingService {

    private static final String CREATE_BOOKING_SQL = "select s367550.api_create_booking(?, ?, ?, ?)";
    private static final String CONFIRM_BOOKING_SQL = "select s367550.api_confirm_booking(?)";

    private final JdbcTemplate jdbcTemplate;
    private final BookingRepository bookingRepository;

    public BookingService(JdbcTemplate jdbcTemplate, BookingRepository bookingRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.bookingRepository = bookingRepository;
    }

    public Long createBooking(CreateBookingRequest request) {
        OffsetDateTime startsAt = request.startsAt();
        OffsetDateTime endsAt = request.endsAt();
        if (!endsAt.isAfter(startsAt)) {
            throw new IllegalArgumentException("endsAt must be after startsAt");
        }

        Long bookingId = jdbcTemplate.queryForObject(
                CREATE_BOOKING_SQL,
                Long.class,
                request.userId(),
                request.spaceId(),
                asTimestamp(startsAt),
                asTimestamp(endsAt)
        );
        // if (bookingId == null) {
        //     throw new IllegalStateException("Failed to create booking");
        // }
        return bookingId;
    }

    public void confirmBooking(Long bookingId) {
        jdbcTemplate.execute(CONFIRM_BOOKING_SQL, (PreparedStatementCallback<Void>) ps -> {
            ps.setLong(1, bookingId);
            ps.execute();
            return null;
        });
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<BookingResponse> getBookingsForUser(Long userId) {
        return bookingRepository.findByUserIdOrderByStartsAtAsc(userId)
                .stream()
                .map(this::mapBooking)
                .toList();
    }

    private BookingResponse mapBooking(Booking booking) {
        Space space = booking.getSpace();
        Long spaceId = space != null ? space.getId() : null;
        String spaceName = space != null ? space.getName() : null;
        SpaceType spaceType = space != null ? space.getType() : null;
        Long locationId = null;
        String locationName = null;
        if (space != null && space.getLocation() != null) {
            locationId = space.getLocation().getId();
            locationName = space.getLocation().getName();
        }

        return new BookingResponse(
                booking.getId(),
                booking.getUserId(),
                spaceId,
                spaceName,
                spaceType,
                locationId,
                locationName,
                booking.getStartsAt(),
                booking.getEndsAt(),
                booking.getStatus(),
                booking.getTotalCents(),
                booking.getCreatedAt()
        );
    }

    private Timestamp asTimestamp(OffsetDateTime value) {
        return Timestamp.from(value.toInstant());
    }
}
