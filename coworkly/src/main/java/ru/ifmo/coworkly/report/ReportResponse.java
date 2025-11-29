package ru.ifmo.coworkly.report;

import java.util.List;

public record ReportResponse(
        SummaryBlock summary,
        List<ByTypeBlock> byType,
        List<DailyBlock> daily,
        List<TopSpaceBlock> topSpaces
) {

    public record SummaryBlock(
            long totalBookings,
            long confirmed,
            long pending,
            long canceled,
            long completed,
            double avgDurationMinutes,
            long totalRevenueCents
    ) {
    }

    public record ByTypeBlock(
            String type,
            long bookings,
            double durationMinutes
    ) {
    }

    public record DailyBlock(
            String day,
            long bookings
    ) {
    }

    public record TopSpaceBlock(
            long spaceId,
            String spaceName,
            long bookings
    ) {
    }
}
