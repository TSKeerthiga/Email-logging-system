package com.ideas2it.emailLoggingSystem.controller;

import com.ideas2it.emailLoggingSystem.config.ImapConfig;
import com.ideas2it.emailLoggingSystem.dto.LoginRequest;
import com.ideas2it.emailLoggingSystem.dto.RegisterRequest;
import com.ideas2it.emailLoggingSystem.dto.ResponseResult;
import com.ideas2it.emailLoggingSystem.entity.Users;
import com.ideas2it.emailLoggingSystem.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
//import org.springframework.validation.DefaultMessageSourceResolvable;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private final AuthService authService;

    @Autowired
    public AuthController( AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user.
     *
     * Endpoint: POST /register
     *
     * @param registerRequest The request body containing user registration info
     * @param bindingResult   Binding result for validation errors
     * @return HTTP 200 with status message
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest registerRequest, BindingResult bindingResult) {
        ResponseResult registerResponse = authService.register(registerRequest, bindingResult);

        if (registerResponse.isSuccess()) {
            return ResponseEntity.ok(registerResponse.getMessage());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(registerResponse.getMessage()); // Returning failure message as a String
        }
    }

    /**
     * Login user.
     *
     * Endpoint: POST /login
     *
     * @param loginRequest The request body containing user registration info
     * @param bindingResult   Binding result for validation errors
     * @return HTTP 200 with status message
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest loginRequest, BindingResult bindingResult) {
        ResponseResult loginResponse =  authService.login(loginRequest, bindingResult);

        if (loginResponse.isSuccess()) {
            return ResponseEntity.ok(loginResponse.getMessage());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(loginResponse.getMessage()); // Returning failure message as a String
        }
    }

    /**
     * Logout user.
     *
     * Endpoint: POST /login
     *
     * @param authHeader header The request body containing token info
     * @return HTTP 200 with status message
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        return authService.logout(authHeader);
    }
}
