package com.ideas2it.emailLoggingSystem.controller;

import com.ideas2it.emailLoggingSystem.context.UserContextHolder;
import com.ideas2it.emailLoggingSystem.dto.RoleRequest;
import com.ideas2it.emailLoggingSystem.service.RoleService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RoleController {
    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
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
        Long userId = UserContextHolder.getUserId();

        logger.info("UserId before id: {}", userId);

        // Check if the user is authenticated before running the task
        if (SecurityContextHolder.getContext().getAuthentication() == null ||
                !SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            logger.warn("No authenticated user found. Scheduled task skipped.");
            return ResponseEntity.status(HttpStatus.CREATED).body("Role created Successfully"); // Exit if the user is not authenticated
        } else {
            return ResponseEntity.status(HttpStatus.CREATED).body("workingggg fine"); // Exit if the user is not authenticated

        }

//        boolean isRoleCreated =  roleService.createRole(roleRequest, userId);
//        if (isRoleCreated) {
//            return ResponseEntity.status(HttpStatus.CREATED).body("Role created Successfully");
//        } else {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to create role");
//        }
    }
}
