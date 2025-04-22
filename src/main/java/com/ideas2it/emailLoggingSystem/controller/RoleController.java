package com.ideas2it.emailLoggingSystem.controller;

import com.ideas2it.emailLoggingSystem.dto.RoleRequest;
import com.ideas2it.emailLoggingSystem.security.CustomAuthDetails;
import com.ideas2it.emailLoggingSystem.service.RoleService;
import com.ideas2it.emailLoggingSystem.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RoleController {
    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

    private final RoleService roleService;
    private final UserService userService;

    public RoleController(RoleService roleService, UserService userService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    /**
     * Create role api
     *
     * @param roleRequest has user role information
     * @return a ResponseEntity containing if created role it return true
     */
    @PostMapping("/create/role")
    public ResponseEntity<String> createRole(@Valid @RequestBody RoleRequest roleRequest) {
        Long userId = userService.getUserId();
        logger.info("UserId: {}", userId);

        boolean isRoleCreated =  roleService.createRole(roleRequest, userId);
        if (isRoleCreated) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Role created Successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to create role");
        }
    }
}
