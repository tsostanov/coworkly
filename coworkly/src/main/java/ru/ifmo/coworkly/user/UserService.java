package ru.ifmo.coworkly.user;

import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.ifmo.coworkly.user.dto.UserResponse;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public UserResponse toDto(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                user.getStatus().name()
        );
    }

    public User register(String email, String password, String fullName, UserRole role) {
        User user = new User();
        user.setEmail(email.toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(OffsetDateTime.now());
        return userRepository.save(user);
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    }

    public User ensureActive(User user) {
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new IllegalStateException("Пользователь заблокирован");
        }
        return user;
    }
}
