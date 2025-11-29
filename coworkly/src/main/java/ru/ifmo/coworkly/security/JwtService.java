package ru.ifmo.coworkly.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.ifmo.coworkly.user.User;

@Component
public class JwtService {

    private final Key key;
    private final long expirationHours;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-hours:24}") long expirationHours) {
        byte[] secretBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.expirationHours = expirationHours;
    }

    public String generate(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("uid", user.getId())
                .claim("role", user.getRole().name())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(expirationHours, ChronoUnit.HOURS)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public UserPrincipal parse(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Long userId = claims.get("uid", Number.class).longValue();
        String email = claims.getSubject();
        String role = claims.get("role", String.class);
        return new UserPrincipal(userId, email, ru.ifmo.coworkly.user.UserRole.valueOf(role));
    }
}
