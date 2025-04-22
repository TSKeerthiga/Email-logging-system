package com.ideas2it.emailLoggingSystem.scheduler;

import com.ideas2it.emailLoggingSystem.context.UserContextHolder;
import com.ideas2it.emailLoggingSystem.dto.ResponseResult;
import com.ideas2it.emailLoggingSystem.service.EmailService;
import com.ideas2it.emailLoggingSystem.repository.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EmailFetchScheduler {
    private static final Logger logger = LoggerFactory.getLogger(EmailFetchScheduler.class);

    private final EmailService emailService;
    private final UsersRepository usersRepository;

    public EmailFetchScheduler(EmailService emailService, UsersRepository usersRepository) {
        this.emailService = emailService;
        this.usersRepository = usersRepository;
    }

    @Scheduled(cron = "*/10 * * * * ?") // every 1 hour for testing
    public void fetchEmailsScheduled() {

        var schedulerUsers = usersRepository.findByRole_Name("SCHEDULER");

        if (schedulerUsers.isEmpty()) {
            logger.info("No users found with role: " + "SCHEDULER");
            return;
        }

        logger.info("user context holder getUser: fetch Email Scheduler: {}", UserContextHolder.getUserId());

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
