package ru.ifmo.coworkly.space;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.PreparedStatement;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.ifmo.coworkly.location.Location;
import ru.ifmo.coworkly.location.LocationRepository;

@ExtendWith(MockitoExtension.class)
class SpaceServiceTest {

    @Mock
    private SpaceRepository spaceRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    private SpaceService spaceService;

    @BeforeEach
    void setUp() {
        spaceService = new SpaceService(spaceRepository, locationRepository, jdbcTemplate);
    }

    @Test
    void createDefaultsSpaceToActiveWhenFlagIsMissing() {
        Location location = new Location();
        location.setId(3L);
        location.setName("Nevsky Hub");
        location.setAddress("Nevsky 1");
        SpaceCreateRequest request = new SpaceCreateRequest(3L, "Focus Desk", 2, SpaceType.OPEN_DESK, 7L, null);

        when(locationRepository.findById(3L)).thenReturn(Optional.of(location));
        when(spaceRepository.save(any(Space.class))).thenAnswer(invocation -> {
            Space saved = invocation.getArgument(0);
            saved.setId(11L);
            return saved;
        });

        SpaceResponse response = spaceService.create(request);

        assertThat(response.id()).isEqualTo(11L);
        assertThat(response.active()).isTrue();
        assertThat(response.locationId()).isEqualTo(3L);
        assertThat(response.name()).isEqualTo("Focus Desk");
    }

    @Test
    void findFreeSpacesRejectsNonPositiveCapacity() {
        OffsetDateTime from = OffsetDateTime.parse("2026-07-05T10:00:00Z");
        OffsetDateTime to = OffsetDateTime.parse("2026-07-05T11:00:00Z");

        assertThatThrownBy(() -> spaceService.findFreeSpaces(1L, from, to, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Parameter 'capacity' must be positive");
    }

    @Test
    void findFreeSpacesUsesCapacityOneWhenRequestDoesNotSpecifyIt() throws Exception {
        OffsetDateTime from = OffsetDateTime.parse("2026-07-05T10:00:00Z");
        OffsetDateTime to = OffsetDateTime.parse("2026-07-05T11:00:00Z");
        List<FreeSpaceResponse> expected = List.of(new FreeSpaceResponse(5L, "Meeting Room", 6));

        when(jdbcTemplate.query(
                eq("select * from s367550.search_free_spaces(?, ?, ?, ?)"),
                any(org.springframework.jdbc.core.PreparedStatementSetter.class),
                any(org.springframework.jdbc.core.RowMapper.class)))
                .thenAnswer(invocation -> {
                    PreparedStatement statement = org.mockito.Mockito.mock(PreparedStatement.class);
                    ((org.springframework.jdbc.core.PreparedStatementSetter) invocation.getArgument(1))
                            .setValues(statement);
                    verify(statement).setLong(1, 1L);
                    verify(statement).setInt(4, 1);
                    return expected;
                });

        List<FreeSpaceResponse> actual = spaceService.findFreeSpaces(1L, from, to, null);

        assertThat(actual).isEqualTo(expected);
    }
}
