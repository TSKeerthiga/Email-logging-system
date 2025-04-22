package com.ideas2it.emailLoggingSystem.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class CustomAuthDetails extends WebAuthenticationDetails {
    private final String userId;

    public CustomAuthDetails(HttpServletRequest request, String userId) {
        super(request);
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}

