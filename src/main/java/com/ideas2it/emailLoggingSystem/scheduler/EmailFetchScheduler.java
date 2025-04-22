package com.ideas2it.emailLoggingSystem.scheduler;

import com.ideas2it.emailLoggingSystem.dto.ResponseResult;
import com.ideas2it.emailLoggingSystem.model.enums.RoleName;
import com.ideas2it.emailLoggingSystem.service.EmailService;
import com.ideas2it.emailLoggingSystem.repository.UsersRepository;
import com.ideas2it.emailLoggingSystem.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class EmailFetchScheduler {
    private static final Logger logger = LoggerFactory.getLogger(EmailFetchScheduler.class);

    private final EmailService emailService;
    private final UsersRepository usersRepository;
    private final UserService userService;

    public EmailFetchScheduler(EmailService emailService, UsersRepository usersRepository, UserService userService) {
        this.emailService = emailService;
        this.usersRepository = usersRepository;
        this.userService = userService;
    }

    @Scheduled(cron = "0 0 * * * ?") // every 1 hour for testing
    public void fetchEmailsScheduled() {
        var schedulerUsers = usersRepository.findByRole_Name("SCHEDULER");

        if (schedulerUsers.isEmpty()) {
            logger.info("No users found with role: " + "SCHEDULER");
            return;
        }

        for (var user : schedulerUsers) {
            try {
                Long userId = user.getId();
                    ResponseResult result = emailService.syncUnreadEmails(userId);
                    logger.info("userId {}", userId);
                    if (result.isSuccess()) {
                        logger.info("User {}: {}", userId, result.getMessage());
                    } else {
                        logger.warn("User {}", userId);
                    }

            } catch (Exception e) {
                logger.error("User {}: Error during scheduled fetch - {}", user.getId(), e.getMessage());
            }
        }
    }

}
