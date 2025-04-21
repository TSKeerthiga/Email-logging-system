package com.ideas2it.emailLoggingSystem.service.impl;

import com.ideas2it.emailLoggingSystem.dto.RoleRequest;
import com.ideas2it.emailLoggingSystem.entity.Role;
import com.ideas2it.emailLoggingSystem.repository.RoleRepository;
import com.ideas2it.emailLoggingSystem.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
//@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Boolean createRole(RoleRequest roleRequest) {
        if (roleRepository.existsByName(roleRequest.getName())) {
            throw new RuntimeException("Role already exists");
        }
        Role role =  new Role();
        role.setName(roleRequest.getName());

        Role saveRole = roleRepository.save(role);

        return saveRole.getId() != null;

    }
}
