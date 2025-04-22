package com.ideas2it.emailLoggingSystem.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface CustomUserDetailService extends UserDetailsService{
    /**
     * Loads a user by their username for authentication.
     *
     * @param username the username of the user
     * @return UserDetails containing user information for authentication
     */
    public UserDetails loadUserByUsername(String username);

}
