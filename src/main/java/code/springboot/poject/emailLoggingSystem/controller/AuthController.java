package code.springboot.poject.emailLoggingSystem.controller;

import code.springboot.poject.emailLoggingSystem.dto.RegisterRequest;
import code.springboot.poject.emailLoggingSystem.entity.Role;
import code.springboot.poject.emailLoggingSystem.entity.User;
import code.springboot.poject.emailLoggingSystem.repository.RoleRepository;
import code.springboot.poject.emailLoggingSystem.repository.UserRepository;
import code.springboot.poject.emailLoggingSystem.security.JwtUtil;
import code.springboot.poject.emailLoggingSystem.security.TokenBlacklist;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklist tokenBlacklist;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, TokenBlacklist tokenBlacklist) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenBlacklist = tokenBlacklist;
    }

    // Register user API
    @PostMapping("/register") // Register endpoint http://localhost/auth/register
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest) {

        // Check if username already exists
        if(userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username is already token");
        }

        User newUser = new User();
        newUser.setUsername(registerRequest.getUsername());

        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());
        newUser.setPassword(encodedPassword);
        System.out.println("Encoded Password" + encodedPassword);
        // convert role names to roles entites and assign to user
        Set<Role> roles = new HashSet<>();
        for(String roleName: registerRequest.getRoles()) {
            Role role = roleRepository.findByName(roleName).orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
            roles.add(role);
        }

        newUser.setRole(roles);
        userRepository.save(newUser);
        return ResponseEntity.ok("User Registered Successfully");
    }

    @PostMapping("/login") // Login endpoint http://localhost/auth/login
    public ResponseEntity<String> login(@RequestBody User loginRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        } catch (Exception e) {
            System.out.println("Exception: "+ e);
        }

        String token = jwtUtil.generateToken(loginRequest.getUsername());
        return ResponseEntity.ok(token);
    }

    @PostMapping("/logout")
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

}
