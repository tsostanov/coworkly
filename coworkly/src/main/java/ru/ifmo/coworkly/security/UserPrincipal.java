package ru.ifmo.coworkly.security;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import ru.ifmo.coworkly.user.UserRole;

public record UserPrincipal(Long id, String email, UserRole role) {

    public Collection<? extends GrantedAuthority> authorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    public Authentication toAuthentication() {
        return new UsernamePasswordAuthenticationToken(this, null, authorities());
    }

    public static UserPrincipal current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return null;
        }
        return principal;
    }
}
