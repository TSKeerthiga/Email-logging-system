package com.ideas2it.emailLoggingSystem.filter;

import com.ideas2it.emailLoggingSystem.context.UserContext;
import com.ideas2it.emailLoggingSystem.context.UserContextHolder;
import com.ideas2it.emailLoggingSystem.security.CustomAuthDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class UserContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getDetails() instanceof CustomAuthDetails details) {
                UserContext context = new UserContext();
                context.setUserId(Long.valueOf(details.getUserId()));
                context.setUsername(details.getUsername());
                context.setRole(details.getRole());

                UserContextHolder.setUser(context);
            }

            filterChain.doFilter(request, response);

        } finally {
            UserContextHolder.clear(); // important to prevent thread leakage
        }
    }
}
