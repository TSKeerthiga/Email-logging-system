package com.ideas2it.emailLoggingSystem.security;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

@Component
public class TokenBlacklist {
    // Using a thread-safe set to store blacklisted tokens
    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();

    // Method to blacklist a token
    public void blacklistToken(String token) {
        blacklist.add(token);
    }

    // Check if a token is blacklisted
    public boolean isTokenBlacklisted(String token) {
        return blacklist.contains(token);
    }
}
