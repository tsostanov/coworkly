package ru.ifmo.coworkly.space;

import jakarta.transaction.Transactional;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Service;
import ru.ifmo.coworkly.location.Location;

@Service
@Transactional
public class SpaceService {

    private static final String SEARCH_FREE_SPACES_SQL =
            "select * from s367550.search_free_spaces(?, ?, ?, ?)";

    private final SpaceRepository spaceRepository;
    private final JdbcTemplate jdbcTemplate;

    public SpaceService(SpaceRepository spaceRepository, JdbcTemplate jdbcTemplate) {
        this.spaceRepository = spaceRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<SpaceResponse> getAllActiveSpaces() {
        return spaceRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::mapSpace)
                .toList();
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<SpaceResponse> getActiveSpacesByLocation(Long locationId) {
        Objects.requireNonNull(locationId, "locationId must not be null");
        return spaceRepository.findByLocation_IdAndActiveTrueOrderByNameAsc(locationId)
                .stream()
                .map(this::mapSpace)
                .toList();
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<FreeSpaceResponse> findFreeSpaces(Long locationId,
                                                  OffsetDateTime from,
                                                  OffsetDateTime to,
                                                  Integer capacity) {
        Objects.requireNonNull(locationId, "locationId must not be null");
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");

        if (!to.isAfter(from)) {
            throw new IllegalArgumentException("Parameter 'to' must be after 'from'");
        }
        if (capacity != null && capacity < 1) {
            throw new IllegalArgumentException("Parameter 'capacity' must be positive");
        }

        int requestedCapacity = capacity != null ? capacity : 1;
        PreparedStatementSetter pss = ps -> {
            ps.setLong(1, locationId);
            ps.setTimestamp(2, asTimestamp(from));
            ps.setTimestamp(3, asTimestamp(to));
            ps.setInt(4, requestedCapacity);
        };

        return jdbcTemplate.query(SEARCH_FREE_SPACES_SQL, pss, (rs, rowNum) -> new FreeSpaceResponse(
                rs.getLong("space_id"),
                rs.getString("space_name"),
                rs.getInt("capacity")
        ));
    }

    private SpaceResponse mapSpace(Space space) {
        Location location = space.getLocation();
        return new SpaceResponse(
                space.getId(),
                location != null ? location.getId() : null,
                location != null ? location.getName() : null,
                location != null ? location.getAddress() : null,
                space.getName(),
                space.getCapacity(),
                space.getType(),
                space.getTariffPlanId(),
                space.getActive()
        );
    }

    private Timestamp asTimestamp(OffsetDateTime value) {
        return Timestamp.from(value.toInstant());
    }
}
