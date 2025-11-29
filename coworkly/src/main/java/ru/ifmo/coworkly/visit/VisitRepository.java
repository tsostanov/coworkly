package ru.ifmo.coworkly.visit;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VisitRepository extends JpaRepository<Visit, Long> {

    Optional<Visit> findByBookingIdAndStatus(Long bookingId, VisitStatus status);

    @Query("""
            select v from Visit v
            where v.status = 'ACTIVE'
            order by v.plannedEnd asc
            """)
    List<Visit> findActive();

    @Query("""
            select v from Visit v
            where v.status = 'ACTIVE'
              and v.plannedEnd between :from and :to
            order by v.plannedEnd asc
            """)
    List<Visit> findExpiring(OffsetDateTime from, OffsetDateTime to);

    @Query("""
            select v from Visit v
            where v.status = 'ACTIVE'
              and v.plannedEnd < :now
            order by v.plannedEnd asc
            """)
    List<Visit> findOverdue(OffsetDateTime now);
}
