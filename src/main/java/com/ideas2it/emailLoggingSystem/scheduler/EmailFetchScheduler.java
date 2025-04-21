package com.ideas2it.emailLoggingSystem.scheduler;

import com.ideas2it.emailLoggingSystem.dto.ResponseResult;
import com.ideas2it.emailLoggingSystem.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;

public class EmailFetchScheduler {
    private final EmailService emailService;

    public EmailFetchScheduler(EmailService emailService) {
        this.emailService = emailService;
    }

    // Scheduled to every one hour for day
    @Scheduled(cron = "0 0 * * * ?")  //  every hour
    public ResponseEntity<String> fetchEmailsScheduled() {
        try {
            LocalDate today = LocalDate.now();
            System.out.println("Scheduled fetch for emails on: " + today);
            ResponseResult result = emailService.fetchEmails(today);
            if (!result.isSuccess()) {
                return ResponseEntity.ok("✅ " + result.getMessage());
            }

            return ResponseEntity.ok("✅ " + result.getMessage());
        } catch (Exception e) {
            System.err.println("Error during scheduled email fetch: " + e.getMessage());
        }
        return null;
    }
}
