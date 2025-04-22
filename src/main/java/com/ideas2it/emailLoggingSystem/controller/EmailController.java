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

    /**
     * Retrieves all email logs along with their associated attachments from the database.
     *
     * @return a ResponseEntity containing a list of EmailLogWithAttachmentsDTO objects
     */
    @GetMapping("/list/logs")
    public ResponseEntity<?> getAllEmailLogsWithAttachments() {
        try {
            List<EmailLogWithAttachmentsDTO> emailLogs = emailService.getAllEmailLogsWithAttachments();

            return ResponseEntity.ok(emailLogs);

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("An error occurred while fetching email logs with attachments: " + e.getMessage());
        }
    }

}
