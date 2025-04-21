package com.ideas2it.emailLoggingSystem.service;

import com.ideas2it.emailLoggingSystem.dto.EmailLogWithAttachmentsDTO;
import com.ideas2it.emailLoggingSystem.dto.ResponseResult;
import com.ideas2it.emailLoggingSystem.entity.EmailLog;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public interface EmailService {

    // Method to fetch emails for a specific date
    ResponseResult fetchEmails(LocalDate date);

    // Method to list all email logs
    List<EmailLogWithAttachmentsDTO> getAllEmailLogsWithAttachments();

}
