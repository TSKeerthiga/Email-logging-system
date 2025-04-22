package com.ideas2it.emailLoggingSystem.service;

import com.ideas2it.emailLoggingSystem.dto.RoleRequest;

public interface RoleService {

    /** Creating role based on logger user */
    Boolean createRole(RoleRequest roleRequest, Long userId);

}
