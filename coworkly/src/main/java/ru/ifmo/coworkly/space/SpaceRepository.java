package ru.ifmo.coworkly.space;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpaceRepository extends JpaRepository<Space, Long> {

    @EntityGraph(attributePaths = "location")
    List<Space> findByActiveTrueOrderByNameAsc();

    @EntityGraph(attributePaths = "location")
    List<Space> findByLocation_IdAndActiveTrueOrderByNameAsc(Long locationId);
}
