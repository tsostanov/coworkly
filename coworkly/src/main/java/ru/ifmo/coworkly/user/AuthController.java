package ru.ifmo.coworkly.user;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.ifmo.coworkly.security.UserPrincipal;
import ru.ifmo.coworkly.user.dto.AuthResponse;
import ru.ifmo.coworkly.user.dto.LoginRequest;
import ru.ifmo.coworkly.user.dto.RegisterRequest;
import ru.ifmo.coworkly.user.dto.UserResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        var user = userService.getById(principal.id());
        return userService.toDto(user);
    }
}
