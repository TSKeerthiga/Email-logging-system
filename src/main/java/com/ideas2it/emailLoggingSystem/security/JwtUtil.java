package com.ideas2it.emailLoggingSystem.security;

import com.ideas2it.emailLoggingSystem.entity.Role;
import com.ideas2it.emailLoggingSystem.entity.Users;
import com.ideas2it.emailLoggingSystem.repository.UsersRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    // secret key
    private static final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    // expiration time
    private final int jwtExpirationMs = 86400000;  // 1 day

    private UsersRepository usersRepository;

    public JwtUtil(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    // Generate Token
    public String generateToken(String username) {
        Optional<Users> user = usersRepository.findByUsername(username);
        Set<Role> roles = user.get().getRole();
        Long userId = user.get().getId();  // Assuming User entity has getId() method to fetch the userId

        // Add roles to the token
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles.stream()
                        .map(role -> role.getName()).collect(Collectors.joining(",")))
                .claim("userId", userId)  // Add userId to the claims
                .setIssuedAt(new Date()).setExpiration(new Date(new Date().getTime() + jwtExpirationMs))
                .signWith(secretKey).compact();
    }

    // Add this method to extract all claims
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Extract Username
    public String extractUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getSubject();
    }

    // Extract roles
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        String rolesString = claims.get("roles", String.class);

        logger.info("Raw roles string from token: '{}'", rolesString);

        if (rolesString == null || rolesString.isBlank()) {
            return List.of("Role not available");
        }

        List<String> roles = Arrays.stream(rolesString.split(","))
                .map(String:: trim)
                .filter(role -> !role.isEmpty())
                .toList();

        if (roles.isEmpty()) {
            logger.warn("No valid roles found in token.");
            return List.of("Role not available");
        }
        return roles;
    }

    // Token Validation
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e){
            return false;
        }
    }

    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        Object userIdObj = claims.get("userId");

        if (userIdObj == null) {
            throw new IllegalArgumentException("userId not found in token claims");
        }

        return Long.parseLong(userIdObj.toString());
    }

}
