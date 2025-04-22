package com.ideas2it.emailLoggingSystem.service;

import com.ideas2it.emailLoggingSystem.dto.LoginRequest;
import com.ideas2it.emailLoggingSystem.dto.RegisterRequest;

import com.ideas2it.emailLoggingSystem.dto.ResponseResult;
import com.ideas2it.emailLoggingSystem.entity.Users;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

public interface AuthService {

    /**
     * Registers a new user, validates input, and saves to the database.
     *
     * @param registerRequest the user registration details
     * @param bindingResult validation results
     * @return response with success or error message
     */
    ResponseResult register(RegisterRequest registerRequest, BindingResult bindingResult);

    /**
     * Authenticates user and generates a JWT token if credentials are valid.
     *
     * @param loginRequest the login credentials (username, password)
     * @param bindingResult validation results
     * @return response with JWT token or error message
     */
    ResponseResult login(LoginRequest loginRequest, BindingResult bindingResult);

    /**
     * Logs out user by invalidating the JWT token and clearing the security context.
     *
     * @param authHeader the JWT token in the "Bearer <token>" format
     * @return response indicating success or failure
     */
    ResponseEntity<String> logout(String authHeader);

}
