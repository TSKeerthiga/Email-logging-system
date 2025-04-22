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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private Long createdBy;

    public AuthServiceImpl(UsersRepository usersRepository, RoleRepository roleRepository, AuthenticationManager authenticationManager, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, TokenBlacklist tokenBlacklist) {
        this.usersRepository = usersRepository;
        this.roleRepository = roleRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.tokenBlacklist = tokenBlacklist;
    }

    /**
     * Registers a new user in the system.
     *
     * This method validates the input data, checks if the username already exists,
     * hashes the password, and assigns roles to the new user. It returns a response
     * with a success or failure message.
     *
     * @param registerRequest The user registration data.
     * @param bindingResult The result of the validation of the request.
     * @return A ResponseResult object containing success or failure message.
     */
    @Override
    public ResponseResult register(RegisterRequest registerRequest, BindingResult bindingResult) {
        try {
//            getCurrentUserId();
            logger.info("User data received: email={}, username={}, password={}, isActive={}, phone={}, user_id={}",
                    registerRequest.getEmail(),
                    registerRequest.getUsername(),
                    registerRequest.getPassword(),
                    registerRequest.isActive(),
                    registerRequest.getPhoneNumber()
//                    UserContext.getCurrentUserId()
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
            newUser.setEmail(registerRequest.getEmail());
            newUser.setPhoneNumber(registerRequest.getPhoneNumber());
            newUser.setIsActive(registerRequest.isActive());
//            newUser.setCreatedBy(UserContext.getCurrentUserId());

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
            try {
                usersRepository.save(newUser);
            } catch (Exception e) {
                logger.error("Error saving user to the database", e);
                return new ResponseResult(false, MessageConstants.USER_REGISTER_FAILED);
            }

            return new ResponseResult(false, MessageConstants.USER_REGISTER_SUCCESS);
        } catch (Exception e) {
            logger.error("Error during registration", e);
            return new ResponseResult(false, MessageConstants.USER_REGISTER_FAILED);
        }
    }

    /**
     * Logs a user into the system by authenticating the provided credentials.
     *
     * This method validates the login request, authenticates the user using the
     * provided credentials, and generates a JWT token if authentication is successful.
     * If authentication fails, an error message is returned.
     *
     * @param loginRequest The login credentials provided by the user.
     * @param bindingResult The result of the validation of the request.
     * @return A ResponseResult object containing the login token or an error message.
     */
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
            return new ResponseResult(false, MessageConstants.INVALID_USER_PASSWORD);

        } catch (Exception e) {
            logger.error("Login failed for user {}: {} at {}", loginRequest.getUsername(), e.getMessage(), LocalDateTime.now(), e);
            return new ResponseResult(false, MessageConstants.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Logs out a user by invalidating the JWT token and clearing the security context.
     *
     * This method takes the Authorization header, extracts the token, adds it to
     * the blacklist, and clears the current user's authentication context.
     *
     * @param authHeader The Authorization header containing the JWT token.
     * @return A ResponseEntity with a success or error message.
     */
    @Override
    public ResponseEntity<String> logout(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklist.blacklistToken(token);
            SecurityContextHolder.clearContext();
            return ResponseEntity.ok("Logged out and token validated");
        } else {
            return ResponseEntity.badRequest().body("Token not provided");
        }
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
