package com.ideas2it.emailLoggingSystem.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ideas2it.emailLoggingSystem.config.ImapConfig;
import com.ideas2it.emailLoggingSystem.dto.EmailLogWithAttachmentsDTO;
import com.ideas2it.emailLoggingSystem.dto.ResponseResult;
import com.ideas2it.emailLoggingSystem.entity.Attachment;
import com.ideas2it.emailLoggingSystem.entity.EmailLog;
import com.ideas2it.emailLoggingSystem.repository.AttachmentRepository;
import com.ideas2it.emailLoggingSystem.repository.EmailLogRepository;
import com.ideas2it.emailLoggingSystem.repository.UsersRepository;
import com.ideas2it.emailLoggingSystem.security.JwtUtil;
import com.ideas2it.emailLoggingSystem.service.EmailService;
import com.ideas2it.emailLoggingSystem.service.S3Service;
import jakarta.annotation.PostConstruct;
import jakarta.mail.*;
import jakarta.mail.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@DependsOn("awsConfig")
public class EmailServiceImpl implements EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final ImapConfig imapConfig;
    private Store imapStore;
    private final S3Service s3service;

    @Autowired
    private EmailLogRepository emailLogRepository;

    private UsersRepository usersRepository;
    private JwtUtil jwtUtil;
    private AttachmentRepository attachmentRepository;

    @Autowired
    public EmailServiceImpl(ImapConfig imapConfig, S3Service s3service, UsersRepository usersRepository, JwtUtil jwtUtil, AttachmentRepository attachmentRepository) {
        this.imapConfig = imapConfig;
        this.s3service = s3service;
        this.usersRepository = usersRepository;
        this.jwtUtil = jwtUtil;
        this.attachmentRepository = attachmentRepository;
    }

    @PostConstruct
    public void initializeImapStore() {
        try {
            // Wait for credentials to be available
            if (imapConfig.getUsername() == null || imapConfig.getPassword() == null) {
                logger.error("IMAP credentials not available yet.");
                throw new IllegalStateException("IMAP credentials not loaded.");
            }

            // Log configuration values to debug
            logger.info("IMAP Configuration - Host: " + imapConfig.getHost());
            logger.info("IMAP Configuration - Username: " + imapConfig.getUsername());
            logger.info("IMAP Configuration - Password: " + imapConfig.getPassword());

            // Set up IMAP properties based on the configuration
            Properties properties = new Properties();
            properties.put("mail.imap.host", imapConfig.getHost());
            properties.put("mail.imap.port", imapConfig.getPort());
            properties.put("mail.imap.username", imapConfig.getUsername());
            properties.put("mail.imap.password", imapConfig.getPassword());
            properties.put("mail.imap.starttls.enable", "true");
            properties.put("mail.imap.ssl.enable", "true");

            // Create a session with the provided properties
            Session session = Session.getInstance(properties);

            // Create the Store object for IMAP
            imapStore = session.getStore("imap");

            // Connect to the IMAP server using the credentials from the configuration
            imapStore.connect(imapConfig.getHost(), imapConfig.getUsername(), imapConfig.getPassword());
            logger.info("Successfully connected to the IMAP server!");
        } catch (Exception e) {
            logger.error("Error initializing IMAP store: ", e);
        }
    }

    @Override
    public ResponseResult fetchEmails(LocalDate date) {
        try {
            if (!imapStore.isConnected()) {
                imapStore.connect(imapConfig.getHost(), imapConfig.getUsername(), imapConfig.getPassword());
            }

            Folder inbox = imapStore.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            Calendar startCal = Calendar.getInstance();
            if (date != null) {
                startCal.setTime(java.sql.Date.valueOf(date));
            }
            startCal.set(Calendar.HOUR_OF_DAY, 0);
            startCal.set(Calendar.MINUTE, 0);
            startCal.set(Calendar.SECOND, 0);
            startCal.set(Calendar.MILLISECOND, 0);
            Date startOfDay = startCal.getTime();

            Calendar endCal = (Calendar) startCal.clone();
            endCal.add(Calendar.DATE, 1);
            Date endOfDay = endCal.getTime();

            SearchTerm todayTerm = new AndTerm(
                    new ReceivedDateTerm(ComparisonTerm.GE, startOfDay),
                    new ReceivedDateTerm(ComparisonTerm.LT, endOfDay)
            );
            FlagTerm unReadInboxMessage = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
            SearchTerm unReadTodayInboxMessage = new AndTerm(todayTerm, unReadInboxMessage);

            Message[] messages = inbox.search(unReadTodayInboxMessage);

            if (messages.length == 0) {
                inbox.close(false);
                return new ResponseResult(false, "No new (unseen) emails to process.");
            }

            logger.info("Total Messages: " + messages.length);
            List<EmailLog> emailLogs = new ArrayList<>();
            Map<Message, List<String>> attachmentUrlMap = new HashMap<>(); // Hold URLs mapped to message
            ObjectMapper objectMapper = new ObjectMapper();

            // Step 1: Process email content & collect attachment URLs
            for (Message message : messages) {
                try {
                    logger.debug("Processing email: " + message.getSubject());

                    String fromEmail = message.getFrom()[0].toString();
                    String toEmail = message.getRecipients(Message.RecipientType.TO)[0].toString();
                    String subject = message.getSubject();
                    String body = getTextFromMessage(message);
                    Date sentDate = message.getSentDate();

                    List<String> attachmentUrls = handleAttachmentUrls(message);
                    logger.debug("Attachment URLs: " + objectMapper.writeValueAsString(attachmentUrls));

                    EmailLog emailLog = new EmailLog(fromEmail, toEmail, subject, body, sentDate);
                    emailLogs.add(emailLog);

                    attachmentUrlMap.put(message, attachmentUrls);

                    logger.info("Email processed: From: " + fromEmail + ", To: " + toEmail + ", Subject: " + subject);
                } catch (Exception e) {
                    logger.error("Error processing email: " + e.getMessage(), e);
                }
            }

            logger.info("emailLogs: {}", emailLogs);
            // Step 2: Save EmailLogs
            List<EmailLog> savedEmailLogs = saveAllEmailLogs(emailLogs);
            logger.info("savedEmailLogs {}", savedEmailLogs );
            if (savedEmailLogs == null || savedEmailLogs.isEmpty()) {
                logger.warn("Emails not saved, skipping attachment saving.");
                inbox.close(false);
                return new ResponseResult(false, "❌ Emails not saved. Attachments skipped.");
            } else {
                // Step 3: Prepare and save attachments
                List<Attachment> allAttachments = new ArrayList<>();
                for (int i = 0; i < savedEmailLogs.size(); i++) {
                    EmailLog savedLog = savedEmailLogs.get(i);
                    Message correspondingMessage = messages[i];
                    List<String> attachmentUrls = attachmentUrlMap.get(correspondingMessage);

                    List<Attachment> attachments = new ArrayList<>();
                    for (String url : attachmentUrls) {
                        Attachment attachment = new Attachment();
                        attachment.setAttachmentUrl(url);
                        attachment.setEmailLog(savedLog);
                        attachments.add(attachment);
                    }

                    allAttachments.addAll(attachments);
                    savedLog.setAttachments(new HashSet<>(attachments)); // maintain bidirectional link
                }

                List<Attachment> savedAttachments = attachmentRepository.saveAll(allAttachments);
                if (!savedAttachments.isEmpty()) {
                    logger.info("Attachments saved successfully. Marking emails as SEEN.");
                    inbox.setFlags(messages, new Flags(Flags.Flag.SEEN), true);
                } else {
                    logger.warn("Attachments not saved. Emails will not be marked as SEEN.");
                }
            }

            inbox.close(false);
            return new ResponseResult(true, "✅ Emails and attachments saved successfully.");
        } catch (Exception e) {
            logger.error("Error fetching emails: " + e.getMessage(), e);
            return new ResponseResult(false, "❌ Error: " + e.getMessage());
        }
    }


    private String getTextFromMessage(Message message) throws Exception {
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            return getTextFromMultipart((Multipart) message.getContent());
        }
        return "";
    }

    private String getTextFromMultipart(Multipart multipart) throws Exception {
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart part = multipart.getBodyPart(i);

            if (part.isMimeType("text/plain")) {
                return part.getContent().toString();
            } else if (part.isMimeType("text/html")) {
                String html = (String) part.getContent();
                return org.jsoup.Jsoup.parse(html).text();
            } else if (part.getContent() instanceof Multipart) {
                // Recursive call for nested multiparts
                return getTextFromMultipart((Multipart) part.getContent());
            }
        }
        return "";
    }

    private List<String> handleAttachmentUrls(Part part) throws Exception {
        List<String> attachmentUrls = new ArrayList<>();
        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                String disposition = bodyPart.getDisposition();

                // Check if the part is an attachment
                if (disposition != null &&
                        (disposition.equalsIgnoreCase(Part.ATTACHMENT) || disposition.equalsIgnoreCase(Part.INLINE))) {
                    String fileName = bodyPart.getFileName();
                    InputStream is = bodyPart.getInputStream();

                    // Create unique filename
                    String uniqueId = String.valueOf(System.nanoTime());

                    // Save to a temp file
                    File tempFile = File.createTempFile("attachment-" + uniqueId + "-", "-" + fileName);
                    Files.copy(is, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    // Upload to S3
                    String fileUrl = s3service.uploadFile(tempFile);
                    attachmentUrls.add(fileUrl);

                    // Clean up
                    tempFile.delete();
                } else if (bodyPart.isMimeType("multipart/*")) {
                    // Recursively check for attachments in nested multiparts
                    attachmentUrls.addAll(handleAttachmentUrls(bodyPart));
                }
            }
        }
        return attachmentUrls;
    }

    private List<EmailLog> saveAllEmailLogs(List<EmailLog> emailLogs) {
        logger.info("saveAllEmailLogs emailLogs: {}", emailLogs);
        if (emailLogs == null || emailLogs.isEmpty()) {
            logger.warn("No emails to save.");
            return null;
        }

        try {
            List<EmailLog> savedEmailLogs = emailLogRepository.saveAll(emailLogs);
            logger.info(savedEmailLogs.size() + " email(s) saved to the database in bulk.");
            return savedEmailLogs;
        } catch (Exception e) {
            logger.error("Error during bulk save of email details: " + e.getMessage(), e);
            return null;
        }
    }

    public List<EmailLogWithAttachmentsDTO> getAllEmailLogsWithAttachments() {

        boolean isAdmin = getRole();
        logger.info("Check is admin: {} ", isAdmin);

        return emailLogRepository.findAllWithAttachments().stream()
                .map(log -> new EmailLogWithAttachmentsDTO(
                        log.getFromEmail(),
                        log.getToEmail(),
                        log.getSubject(),
                        log.getBody(),
                        log.getMailReceiveDate(),
                        isAdmin ?
                            log.getAttachments()
                                    .stream()
                                    .map(Attachment::getAttachmentUrl)
                                    .toList()
                                : List.of()
                        )
                ).toList();
    }


    public boolean getRole() {
        // Get current user's roles
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            logger.info("Current user roles: {}", roles);
            return roles.contains("ROLE_ADMIN");
        }
        logger.warn("Authentication object is null.");
        return false;
    }
}
