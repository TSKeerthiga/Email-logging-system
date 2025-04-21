package com.ideas2it.emailLoggingSystem.service.impl;

import com.ideas2it.emailLoggingSystem.constants.MessageConstants;
import com.ideas2it.emailLoggingSystem.dto.LoginRequest;
import com.ideas2it.emailLoggingSystem.dto.RegisterRequest;
import com.ideas2it.emailLoggingSystem.dto.ResponseResult;
import com.ideas2it.emailLoggingSystem.entity.Role;
import com.ideas2it.emailLoggingSystem.entity.Users;
import com.ideas2it.emailLoggingSystem.repository.RoleRepository;
import com.ideas2it.emailLoggingSystem.repository.UsersRepository;
import com.ideas2it.emailLoggingSystem.security.JwtUtil;
import com.ideas2it.emailLoggingSystem.security.TokenBlacklist;
import com.ideas2it.emailLoggingSystem.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl  implements AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UsersRepository usersRepository;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private TokenBlacklist tokenBlacklist;

    public AuthServiceImpl(UsersRepository usersRepository, RoleRepository roleRepository,AuthenticationManager authenticationManager,JwtUtil jwtUtil, PasswordEncoder passwordEncoder, TokenBlacklist tokenBlacklist) {
        this.usersRepository = usersRepository;
        this.roleRepository = roleRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.tokenBlacklist = tokenBlacklist;
    }

    @Override
    public ResponseResult register(RegisterRequest registerRequest, BindingResult bindingResult) {
        try {
            logger.info("User data received: email={}, username={}, password={}, isActive={}, phone={}",
                    registerRequest.getEmail(),
                    registerRequest.getUsername(),
                    registerRequest.getPassword(),
                    registerRequest.isActive(),
                    registerRequest.getPhoneNumber()
            );

            String validationErrors = fieldValidation(bindingResult);
            if (validationErrors != null) {
                return new ResponseResult(false, validationErrors);
            }

            if (usersRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
                return new ResponseResult(false, MessageConstants.USER_TAKEN);
            }

            Users newUser = new Users();
            newUser.setUsername(registerRequest.getUsername());
            newUser.setPassword(registerRequest.getPassword());
            newUser.setEmail(registerRequest.getEmail());  // Ensure email is mapped
            newUser.setPhoneNumber(registerRequest.getPhoneNumber());
            newUser.setIsActive(registerRequest.isActive());  // Using the updated "isActive" field

            String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());
            newUser.setPassword(encodedPassword);

            Set<Role> roles = new HashSet<>();
            if (registerRequest.getRole() == null || registerRequest.getRole().isEmpty()) {
                return new ResponseResult(false, MessageConstants.ROLE_EMPTY);
            }

            for (String roleName : registerRequest.getRole()) {
                Optional<Role> role = roleRepository.findByName(roleName);
                if (!role.isPresent()) {
                    return new ResponseResult(false, MessageConstants.ROLE_NOT_FOUND);
                }
                roles.add(role.get());
            }

            newUser.setRole(roles);
            usersRepository.save(newUser);

            return new ResponseResult(false, MessageConstants.USER_REGISTER_SUCCESS);
        } catch (Exception e) {
            logger.error("Error during registration", e);
            return new ResponseResult(false, MessageConstants.USER_REGISTER_FAILED);
        }
//        return new ResponseResult(false, MessageConstants.USER_REGISTER_FAILED);
    }

    @Override
    public ResponseResult login(LoginRequest loginRequest, BindingResult bindingResult) {
        try {
            logger.info("loginRequest.getUsername()", loginRequest.getUsername());
            String validationErrors = fieldValidation(bindingResult);
            if (validationErrors != null) {
                return new ResponseResult(false, validationErrors);
            }

            Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            logger.info("Authentication", authenticate.isAuthenticated());
            String token = jwtUtil.generateToken(loginRequest.getUsername());
            logger.info("token", token);
            return new ResponseResult(true, token);
        } catch (BadCredentialsException e) {
            logger.error("Login failed for user {}: Invalid credentials at {}", loginRequest.getUsername(), LocalDateTime.now(), e);
            return new ResponseResult(false, MessageConstants.INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            logger.error("Login failed for user {}: {} at {}", loginRequest.getUsername(), e.getMessage(), LocalDateTime.now(), e);
            return new ResponseResult(false, MessageConstants.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklist.blacklistToken(token);
            return ResponseEntity.ok("Logged out and token invalidated.");
        } else {
            return ResponseEntity.badRequest().body("Token not provided");
        }
    }

    // Helper method to validate email format
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";  // Simple email validation regex
        return email != null && email.matches(emailRegex);
    }

    public String fieldValidation(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
        }
        return null;
    }
}
