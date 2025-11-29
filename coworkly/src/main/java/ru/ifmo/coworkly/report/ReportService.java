package ru.ifmo.coworkly.report;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class ReportService {

    private final JdbcTemplate jdbcTemplate;

    private static final String COMMON_WHERE = """
            where b.starts_at >= ? and b.ends_at <= ?
              and ( ? is null or s.location_id = ? )
            """;

    public ReportService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ReportResponse getReport(ReportRequest request) {
        validateRange(request.from(), request.to());
        ReportResponse.SummaryBlock summary = fetchSummary(request);
        List<ReportResponse.ByTypeBlock> byType = fetchByType(request);
        List<ReportResponse.DailyBlock> daily = fetchDaily(request);
        List<ReportResponse.TopSpaceBlock> topSpaces = fetchTopSpaces(request);
        return new ReportResponse(summary, byType, daily, topSpaces);
    }

    private void validateRange(OffsetDateTime from, OffsetDateTime to) {
        Assert.notNull(from, "from must not be null");
        Assert.notNull(to, "to must not be null");
        if (!to.isAfter(from)) {
            throw new IllegalArgumentException("'to' must be after 'from'");
        }
    }

    private ReportResponse.SummaryBlock fetchSummary(ReportRequest request) {
        String sql = """
                select
                    count(*) as total_bookings,
                    sum(case when b.status = 'CONFIRMED' then 1 else 0 end) as confirmed,
                    sum(case when b.status = 'PENDING' then 1 else 0 end) as pending,
                    sum(case when b.status = 'CANCELED' then 1 else 0 end) as canceled,
                    sum(case when b.status = 'COMPLETED' then 1 else 0 end) as completed,
                    avg(extract(epoch from (b.ends_at - b.starts_at))/60) as avg_minutes,
                    coalesce(sum(b.total_cents),0) as revenue_cents
                from booking b
                join space s on b.space_id = s.id
                """ + COMMON_WHERE;

        return jdbcTemplate.query(sql, pss(request), rs -> {
            if (!rs.next()) {
                return new ReportResponse.SummaryBlock(0, 0, 0, 0, 0, 0, 0);
            }
            return new ReportResponse.SummaryBlock(
                    rs.getLong("total_bookings"),
                    rs.getLong("confirmed"),
                    rs.getLong("pending"),
                    rs.getLong("canceled"),
                    rs.getLong("completed"),
                    safeDouble(rs, "avg_minutes"),
                    rs.getLong("revenue_cents")
            );
        });
    }

    private List<ReportResponse.ByTypeBlock> fetchByType(ReportRequest request) {
        String sql = """
                select s.type, count(*) as bookings,
                       sum(extract(epoch from (b.ends_at - b.starts_at))/60) as duration_minutes
                from booking b
                join space s on b.space_id = s.id
                """ + COMMON_WHERE + """
                group by s.type
                order by bookings desc
                """;

        return jdbcTemplate.query(sql, pss(request), (rs, rowNum) ->
                new ReportResponse.ByTypeBlock(
                        rs.getString("type"),
                        rs.getLong("bookings"),
                        safeDouble(rs, "duration_minutes")
                ));
    }

    private List<ReportResponse.DailyBlock> fetchDaily(ReportRequest request) {
        String sql = """
                select cast(b.starts_at as date) as day, count(*) as bookings
                from booking b
                join space s on b.space_id = s.id
                """ + COMMON_WHERE + """
                group by day
                order by day
                """;
        return jdbcTemplate.query(sql, pss(request), (rs, rowNum) ->
                new ReportResponse.DailyBlock(
                        rs.getString("day"),
                        rs.getLong("bookings")
                ));
    }

    private List<ReportResponse.TopSpaceBlock> fetchTopSpaces(ReportRequest request) {
        String sql = """
                select s.id as space_id, s.name as space_name, count(*) as bookings
                from booking b
                join space s on b.space_id = s.id
                """ + COMMON_WHERE + """
                group by s.id, s.name
                order by bookings desc
                limit 5
                """;
        return jdbcTemplate.query(sql, pss(request), (rs, rowNum) ->
                new ReportResponse.TopSpaceBlock(
                        rs.getLong("space_id"),
                        rs.getString("space_name"),
                        rs.getLong("bookings")
                ));
    }

    private PreparedStatementSetter pss(ReportRequest request) {
        return ps -> {
            ps.setObject(1, request.from());
            ps.setObject(2, request.to());
            if (request.locationId() != null) {
                ps.setLong(3, request.locationId());
                ps.setLong(4, request.locationId());
            } else {
                ps.setObject(3, null);
                ps.setObject(4, null);
            }
        };
    }

    private double safeDouble(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        if (rs.wasNull()) {
            return 0.0;
        }
        return value;
    }
}
