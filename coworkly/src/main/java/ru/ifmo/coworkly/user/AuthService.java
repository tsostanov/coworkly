package ru.ifmo.coworkly.user;

import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.ifmo.coworkly.security.JwtService;
import ru.ifmo.coworkly.user.dto.AuthResponse;
import ru.ifmo.coworkly.user.dto.LoginRequest;
import ru.ifmo.coworkly.user.dto.RegisterRequest;
import ru.ifmo.coworkly.user.dto.UserResponse;
import ru.ifmo.coworkly.user.UserRole;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       UserService userService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email().toLowerCase())) {
            throw new IllegalArgumentException("Email уже зарегистрирован");
        }
        long existing = userRepository.count();
        UserRole role = existing == 0 ? UserRole.ADMIN : UserRole.RESIDENT;
        User user = userService.register(request.email(), request.password(), request.fullName(), role);
        return toAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .map(userService::ensureActive)
                .orElseThrow(() -> new IllegalArgumentException("Неверный email или пароль"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Неверный email или пароль");
        }
        return toAuthResponse(user);
    }

    public UserResponse me(User current) {
        return userService.toDto(current);
    }

    private AuthResponse toAuthResponse(User user) {
        String token = jwtService.generate(user);
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getFullName(), user.getRole().name());
    }
}
