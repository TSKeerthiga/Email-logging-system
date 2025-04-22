package com.ideas2it.emailLoggingSystem.service;

import com.ideas2it.emailLoggingSystem.dto.EmailLogWithAttachmentsDTO;
import com.ideas2it.emailLoggingSystem.dto.ResponseResult;
import com.ideas2it.emailLoggingSystem.entity.EmailLog;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public interface EmailService {

    /** Fetch unread emails for a specific date */
    ResponseResult syncUnreadEmails(Long userId);

    /** Retrieve all email logs with attachments */
    List<EmailLogWithAttachmentsDTO> getAllEmailLogsWithAttachments();

}
