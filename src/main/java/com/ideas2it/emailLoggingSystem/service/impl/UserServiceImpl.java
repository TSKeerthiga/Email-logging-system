package com.ideas2it.emailLoggingSystem.service.impl;

import com.ideas2it.emailLoggingSystem.security.CustomAuthDetails;
import com.ideas2it.emailLoggingSystem.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    public Long getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getDetails() instanceof CustomAuthDetails) {
            CustomAuthDetails details = (CustomAuthDetails) auth.getDetails();
            String userIdStr = details.getUserId();

            if (userIdStr != null) {
                try {
                    Long userId = Long.valueOf(userIdStr);
                    logger.info("UserId: {}", userId);
                    return userId;
                } catch (NumberFormatException e) {
                    logger.error("Failed to parse userId: {}", userIdStr, e);
                    return null; // Or handle the error accordingly
                }
            } else {
                logger.warn("UserId is null");
                return null; // Or handle the error accordingly
            }
        } else {
            logger.warn("Authentication or CustomAuthDetails is missing");
            return null; // Or handle the error accordingly
        }
    }

}
