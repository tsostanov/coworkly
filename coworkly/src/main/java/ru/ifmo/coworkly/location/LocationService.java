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

    public LocationResponse create(LocationCreateRequest request) {
        Location location = new Location();
        location.setName(request.name());
        location.setAddress(request.address());
        Location saved = locationRepository.save(location);
        return new LocationResponse(saved.getId(), saved.getName(), saved.getAddress());
    }

    public LocationResponse update(Long id, LocationCreateRequest request) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Локация не найдена"));
        location.setName(request.name());
        location.setAddress(request.address());
        Location saved = locationRepository.save(location);
        return new LocationResponse(saved.getId(), saved.getName(), saved.getAddress());
    }

    public void delete(Long id) {
        locationRepository.deleteById(id);
    }
}
