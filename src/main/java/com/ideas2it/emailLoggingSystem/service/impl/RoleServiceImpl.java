package com.ideas2it.emailLoggingSystem.service.impl;

import com.ideas2it.emailLoggingSystem.dto.RoleRequest;
import com.ideas2it.emailLoggingSystem.entity.Role;
import com.ideas2it.emailLoggingSystem.repository.RoleRepository;
import com.ideas2it.emailLoggingSystem.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
//@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private static final Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Created role based on logged user,
     *
     * @param roleRequest containing role info
     * @param userId
     * @return ture if role created.
     */
    @Override
    public Boolean createRole(RoleRequest roleRequest, Long userId) {
        logger.info("createRole userId: {}",userId);
        if (roleRepository.existsByName(roleRequest.getName())) {
            throw new RuntimeException("Role already exists");
        }
        Role role =  new Role();
        role.setName(roleRequest.getName());
        role.setCreatedBy(userId);

        Role saveRole = roleRepository.save(role);

        return saveRole.getId() != null;
    }
}
