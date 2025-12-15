package ru.ifmo.coworkly.penalty;

import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import ru.ifmo.coworkly.penalty.dto.PenaltyRequest;
import ru.ifmo.coworkly.penalty.dto.PenaltyResponse;
import ru.ifmo.coworkly.security.UserPrincipal;
import ru.ifmo.coworkly.user.UserService;

@Service
@Transactional
public class PenaltyService {

    private final PenaltyRepository penaltyRepository;
    private final UserService userService;

    public PenaltyService(PenaltyRepository penaltyRepository, UserService userService) {
        this.penaltyRepository = penaltyRepository;
        this.userService = userService;
    }

    public PenaltyResponse create(PenaltyRequest request, UserPrincipal admin) {
        userService.ensureActive(userService.getById(request.userId()));
        validatePayload(request);

        Penalty penalty = new Penalty();
        penalty.setUserId(request.userId());
        penalty.setType(request.type());
        penalty.setReason(request.reason());
        penalty.setLimitMinutes(request.limitMinutes());
        penalty.setAmountCents(request.amountCents());
        penalty.setExpiresAt(request.expiresAt());
        penalty.setCreatedAt(OffsetDateTime.now());
        if (admin != null) {
            penalty.setCreatedByAdminId(admin.id());
        }
        return toDto(penaltyRepository.save(penalty));
    }

    public List<PenaltyResponse> list(Long userId, boolean activeOnly) {
        OffsetDateTime now = OffsetDateTime.now();
        return penaltyRepository.findByFilter(userId, activeOnly, now)
                .stream()
                .map(p -> toDto(p, now))
                .toList();
    }

    public List<PenaltyResponse> activeForUser(Long userId) {
        OffsetDateTime now = OffsetDateTime.now();
        return penaltyRepository.findActiveForUser(userId, now)
                .stream()
                .map(p -> toDto(p, now))
                .toList();
    }

    public void revoke(Long id) {
        Penalty penalty = penaltyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Штраф не найден"));
        penalty.setRevokedAt(OffsetDateTime.now());
        penaltyRepository.save(penalty);
    }

    public void validateBooking(Long userId, Duration duration) {
        OffsetDateTime now = OffsetDateTime.now();
        List<Penalty> active = penaltyRepository.findActiveForUser(userId, now);
        for (Penalty p : active) {
            if (p.getType() == PenaltyType.TIMEOUT) {
                throw new IllegalStateException("У пользователя действует тайм-аут до " +
                        (p.getExpiresAt() != null ? p.getExpiresAt() : "отмены"));
            }
            if (p.getType() == PenaltyType.MAX_DURATION_LIMIT && p.getLimitMinutes() != null) {
                long minutes = duration.toMinutes();
                if (minutes > p.getLimitMinutes()) {
                    throw new IllegalStateException("Максимальная длительность брони: " + p.getLimitMinutes() + " минут");
                }
            }
        }
    }

    private void validatePayload(PenaltyRequest request) {
        if (request.type() == PenaltyType.MAX_DURATION_LIMIT && (request.limitMinutes() == null || request.limitMinutes() < 1)) {
            throw new IllegalArgumentException("Для ограничения длительности нужно указать limitMinutes > 0");
        }
        if (request.type() == PenaltyType.TIMEOUT && request.expiresAt() == null) {
            throw new IllegalArgumentException("Для тайм-аута нужно указать expiresAt");
        }
    }

    private PenaltyResponse toDto(Penalty penalty) {
        return toDto(penalty, OffsetDateTime.now());
    }

    private PenaltyResponse toDto(Penalty penalty, OffsetDateTime now) {
        boolean active = penalty.getRevokedAt() == null && (penalty.getExpiresAt() == null || penalty.getExpiresAt().isAfter(now));
        ru.ifmo.coworkly.user.User user = userService.getById(penalty.getUserId());
        return new PenaltyResponse(
                penalty.getId(),
                penalty.getUserId(),
                penalty.getType(),
                penalty.getReason(),
                penalty.getLimitMinutes(),
                penalty.getAmountCents(),
                penalty.getExpiresAt(),
                penalty.getCreatedAt(),
                penalty.getRevokedAt(),
                penalty.getCreatedByAdminId(),
                user.getEmail(),
                user.getFullName(),
                active
        );
    }
}
