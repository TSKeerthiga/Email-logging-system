package com.ideas2it.emailLoggingSystem.security;

import com.ideas2it.emailLoggingSystem.service.CustomUserDetailService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailService customUserDetailService;;
    private final TokenBlacklist tokenBlacklist;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailService customUserDetailService, TokenBlacklist tokenBlacklist) {
        this.jwtUtil = jwtUtil;
        this.customUserDetailService = customUserDetailService;
        this.tokenBlacklist = tokenBlacklist;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");
        String path = request.getRequestURI();
        System.out.println("path" + path);

        // Skip token validation for login or public endpoints
        if (path.equals("/auth/login") || path.startsWith("/auth/register") || path.equals("/api/emails/fetch")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);

            // Check if the token is blacklisted
            if (tokenBlacklist.isTokenBlacklisted(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token is expired. Please login again.");
                return;
            }

            try {
                String username = jwtUtil.extractUsername(token);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    List<String> roles = jwtUtil.extractRoles(token);
                    String userId = String.valueOf(jwtUtil.extractUserId(token));  // Extract user_id from JWT

                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .toList();

                    UserDetails userDetails = customUserDetailService.loadUserByUsername(username);

                    if (jwtUtil.isTokenValid(token)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
                                null, authorities);

                        // Create the custom authentication details with both WebAuthenticationDetails and userId
                        CustomAuthDetails customDetails = new CustomAuthDetails(request, userId);
                        authToken.setDetails(customDetails);

                        // Set the authentication in the SecurityContextHolder
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        request.setAttribute("userId", userId);  // You can still store in request for convenience
                    }
                }
            } catch (Exception e) {
                return;
            }
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Authorization header missing or invalid");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
