package ru.ifmo.coworkly.booking;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @EntityGraph(attributePaths = {"space", "space.location"})
    List<Booking> findByUserIdOrderByStartsAtAsc(Long userId);
}
