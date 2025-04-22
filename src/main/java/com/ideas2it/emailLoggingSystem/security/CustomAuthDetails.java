package com.ideas2it.emailLoggingSystem.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

@Getter
@Setter
public class CustomAuthDetails extends WebAuthenticationDetails {
    private final String userId;
    private String username;
    private String role;

    public CustomAuthDetails(HttpServletRequest request, String userId) {
        super(request);
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}

