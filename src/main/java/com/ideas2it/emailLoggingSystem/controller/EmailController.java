package com.ideas2it.emailLoggingSystem.controller;

import com.ideas2it.emailLoggingSystem.dto.EmailLogWithAttachmentsDTO;
import com.ideas2it.emailLoggingSystem.dto.ResponseResult;
import com.ideas2it.emailLoggingSystem.entity.EmailLog;
import com.ideas2it.emailLoggingSystem.repository.EmailLogRepository;
import com.ideas2it.emailLoggingSystem.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/emails")
public class EmailController {
    @Autowired
    private EmailLogRepository emailLogRepository;

    private final EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService, EmailLogRepository emailLogRepository) {
        this.emailService = emailService;
        this.emailLogRepository = emailLogRepository;
    }

    @GetMapping("/fetch")
    public ResponseEntity<String> fetchEmails(@RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            // If no date is provided, use today's date
            if (date == null) {
                date = LocalDate.now();
            }
            ResponseResult responseResult  = emailService.fetchEmails(date);
            if (!responseResult.isSuccess()) {
                return ResponseEntity.ok("✅ " + responseResult.getMessage());
            }

            return ResponseEntity.ok("✅ " + responseResult.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching emails: " + e.getMessage());
        }
    }

    @GetMapping("/list/logs")
    public ResponseEntity<List<EmailLogWithAttachmentsDTO>> getAllEmailLogs() {
        return ResponseEntity.ok(emailService.getAllEmailLogsWithAttachments());
    }
}
