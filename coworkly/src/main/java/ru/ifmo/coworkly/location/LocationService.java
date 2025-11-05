package ru.ifmo.coworkly.location;

import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class LocationService {

    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<LocationResponse> getAll() {
        return locationRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(location -> new LocationResponse(location.getId(), location.getName(), location.getAddress()))
                .toList();
    }
}
