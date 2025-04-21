package com.ideas2it.emailLoggingSystem.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface CustomUserDetailService extends UserDetailsService{

    public UserDetails loadUserByUsername(String username);

}
