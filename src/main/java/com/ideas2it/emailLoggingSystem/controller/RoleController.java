package com.ideas2it.emailLoggingSystem.controller;

import com.ideas2it.emailLoggingSystem.dto.RegisterRequest;
import com.ideas2it.emailLoggingSystem.dto.RoleRequest;
import com.ideas2it.emailLoggingSystem.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    // Register user API
    @PostMapping("/create/role")
    public ResponseEntity<String> createRole(@Valid @RequestBody RoleRequest roleRequest) {
        boolean isRoleCreated =  roleService.createRole(roleRequest);
        if (isRoleCreated) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Role created Successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to create role");
        }
    }
}
