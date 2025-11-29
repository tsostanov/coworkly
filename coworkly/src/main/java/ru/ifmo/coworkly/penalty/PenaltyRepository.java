package ru.ifmo.coworkly.penalty;

import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PenaltyRepository extends JpaRepository<Penalty, Long> {

    @Query("""
            select p from Penalty p
            where p.userId = :userId
              and (p.revokedAt is null)
              and (p.expiresAt is null or p.expiresAt > :now)
            """)
    List<Penalty> findActiveForUser(Long userId, OffsetDateTime now);

    @Query("""
            select p from Penalty p
            where (:userId is null or p.userId = :userId)
              and (:activeOnly = false or (p.revokedAt is null and (p.expiresAt is null or p.expiresAt > :now)))
            order by p.createdAt desc
            """)
    List<Penalty> findByFilter(Long userId, boolean activeOnly, OffsetDateTime now);
}
