package com.ideas2it.emailLoggingSystem.service;

import com.ideas2it.emailLoggingSystem.dto.RoleRequest;
import com.ideas2it.emailLoggingSystem.entity.Role;
import org.springframework.http.ResponseEntity;

public interface RoleService {

    Boolean createRole(RoleRequest roleRequest);

}
