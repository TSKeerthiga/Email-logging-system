package com.ideas2it.emailLoggingSystem.service.impl;

import com.ideas2it.emailLoggingSystem.config.ImapConfig;
import com.ideas2it.emailLoggingSystem.constants.MessageConstants;
import com.ideas2it.emailLoggingSystem.context.UserContextHolder;
import com.ideas2it.emailLoggingSystem.dto.EmailLogWithAttachmentsDTO;
import com.ideas2it.emailLoggingSystem.dto.ResponseResult;
import com.ideas2it.emailLoggingSystem.entity.Attachment;
import com.ideas2it.emailLoggingSystem.entity.EmailLog;
import com.ideas2it.emailLoggingSystem.repository.AttachmentRepository;
import com.ideas2it.emailLoggingSystem.repository.EmailLogRepository;
import com.ideas2it.emailLoggingSystem.service.EmailService;
import com.ideas2it.emailLoggingSystem.service.S3Service;
import jakarta.annotation.PostConstruct;
import jakarta.mail.*;
import jakarta.mail.search.*;
import org.jsoup.Jsoup;
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
import java.util.*;
import java.util.stream.Collectors;
import com.ideas2it.emailLoggingSystem.util.CollectionUtils;

@Service
@DependsOn("awsConfig")
public class EmailServiceImpl implements EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final ImapConfig imapConfig;
    private Store imapStore;
    private final S3Service s3service;

    @Autowired
    private EmailLogRepository emailLogRepository;

    private final AttachmentRepository attachmentRepository;

    private Long createdBy;

    @Autowired
    public EmailServiceImpl(ImapConfig imapConfig, S3Service s3service, AttachmentRepository attachmentRepository) {
        this.imapConfig = imapConfig;
        this.s3service = s3service;
        this.attachmentRepository = attachmentRepository;
    }

    public void setCreatedBy() {
        this.createdBy = UserContextHolder.getUserId();
    }
    /**
     * initializeImapStore is checking imap config connected or not and data fetched from AWS Security Manager
     */
    @PostConstruct
    public void initializeImapStore() {
        try {
            if (imapConfig.getUsername() == null || imapConfig.getPassword() == null) {
                logger.error("IMAP credentials not available yet.");
                throw new IllegalStateException("IMAP credentials not loaded.");
            }

            logger.info("IMAP Configuration - Host: " + imapConfig.getHost());
            logger.info("IMAP Configuration - Username: " + imapConfig.getUsername());
            logger.info("IMAP Configuration - Password: " + imapConfig.getPassword());

            Properties properties = new Properties();
            properties.put("mail.imap.host", imapConfig.getHost());
            properties.put("mail.imap.port", imapConfig.getPort());
            properties.put("mail.imap.username", imapConfig.getUsername());
            properties.put("mail.imap.password", imapConfig.getPassword());
            properties.put("mail.imap.starttls.enable", "true");
            properties.put("mail.imap.ssl.enable", "true");

            Session session = Session.getInstance(properties);

            imapStore = session.getStore("imap");

            imapStore.connect(imapConfig.getHost(), imapConfig.getUsername(), imapConfig.getPassword());
            logger.info("Successfully connected to the IMAP server!");
        } catch (Exception e) {
            logger.error("Error initializing IMAP store: ", e);
        }
    }

    /**
     * Fetches all unread emails from the mail server for the specified date,
     * saves their details and attachments to the database, and marks them as read.
     *
     * @return a ResponseResult indicating success or failure with a message
     */
    @Override
    public ResponseResult syncUnreadEmails(Long userId) {
        logger.info("user context holder getUser: {}", UserContextHolder.getUserId());
        try {
            if (!imapStore.isConnected()) {
                imapStore.connect(imapConfig.getHost(), imapConfig.getUsername(), imapConfig.getPassword());
            }
            logger.info("createdBy: {}: {}", createdBy, userId);

            String folderPath = "INBOX";
            Folder inbox = imapStore.getFolder(folderPath);
            inbox.open(Folder.READ_WRITE);

            Calendar startDate = Calendar.getInstance();
            startDate.set(Calendar.HOUR_OF_DAY, 0);
            startDate.set(Calendar.MINUTE, 0);
            startDate.set(Calendar.SECOND, 0);
            startDate.set(Calendar.MILLISECOND, 0);
            Date startOfDay = startDate.getTime();

            Calendar endDate = (Calendar) startDate.clone();
            endDate.add(Calendar.DATE, 1);
            Date endOfDay = endDate.getTime();

            SearchTerm todayTerm = new AndTerm(
                    new ReceivedDateTerm(ComparisonTerm.GE, startOfDay),
                    new ReceivedDateTerm(ComparisonTerm.LT, endOfDay)
            );
            FlagTerm unReadInboxMessage = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
            SearchTerm unReadTodayInboxMessage = new AndTerm(todayTerm, unReadInboxMessage);

            Message[] messages = inbox.search(unReadTodayInboxMessage);

            if (messages.length == 0) {
                inbox.close(false);
                return new ResponseResult(false, MessageConstants.NO_UNREAD_MAIL);
            }

            logger.info("Total Messages: {}", messages.length);

            // Step 1: Process Messages to create EmailLogs
            List<EmailLog> emailLogs = new ArrayList<>();
            Map<String, Message> messageMap = new HashMap<>();
            for (Message message : messages) {
                try {
                    logger.debug("Processing email: {}", message.getSubject());

                    String fromEmail = message.getFrom()[0].toString();
                    String toEmail = message.getRecipients(Message.RecipientType.TO)[0].toString();
                    String subject = message.getSubject();
                    String body = getTextFromMessage(message);
                    Date mailReceiveDate = message.getSentDate();

                    // Set the user that created the log
                    EmailLog emailLog = new EmailLog(fromEmail, toEmail, subject, body, mailReceiveDate, userId);
                    emailLogs.add(emailLog);

                    // Map the message to a unique key
                    String key = message.getSubject() + "_" + message.getSentDate();
                    messageMap.put(key, message);

                    logger.info("Email processed: From: {}, To: {}, Subject: {}", fromEmail, toEmail, subject);
                } catch (Exception e) {
                    logger.error("Error processing email: {}", e.getMessage(), e);
                }
            }

            if (emailLogs.isEmpty()) {
                return new ResponseResult(false, MessageConstants.EMAIL_NOT_SAVE_ATTACHMENT_SKIP);
            }

            // Step 2: Handle Attachments
            Map<Message, List<String>> attachmentUrlMap = new HashMap<>();
            for (Message message : messages) {
                List<String> attachmentUrls = handleAttachmentUrls(message);
                attachmentUrlMap.put(message, attachmentUrls);
            }

            // Step 3: Save Email Logs and Attachments
            List<EmailLog> allSavedEmailLogs = new ArrayList<>();
            List<List<EmailLog>> emailLogsBatch = CollectionUtils.chunkList(emailLogs, MessageConstants.CHUNK_SIZE);

            for (List<EmailLog> emailLogBatch : emailLogsBatch) {
                logger.info("Saving email batch of size: {}", emailLogBatch.size());

                List<EmailLog> savedEmailLogBatch = emailLogRepository.saveAll(emailLogBatch);
                allSavedEmailLogs.addAll(savedEmailLogBatch);

                boolean isAttachmentsFound = false;
                List<Attachment> allAttachments = new ArrayList<>();

                for (EmailLog emailLog : savedEmailLogBatch) {
                    String emailIdentifier = emailLog.getSubject() + "_" + emailLog.getMailReceiveDate();
                    Message correspondingMessage = messageMap.get(emailIdentifier);
                    List<String> urls = attachmentUrlMap.get(correspondingMessage);

                    if (urls != null && !urls.isEmpty()) {
                        isAttachmentsFound = true;
                        List<Attachment> attachments = urls.stream().map(url -> {
                            Attachment attachment = new Attachment();
                            attachment.setAttachmentUrl(url);
                            attachment.setEmailLog(emailLog);
                            attachment.setCreatedBy(userId);
                            return attachment;
                        }).collect(Collectors.toList());

                        allAttachments.addAll(attachments);
                        emailLog.setAttachments(new HashSet<>(attachments));
                    }
                }

                if (isAttachmentsFound) {
                    try {
                        List<Attachment> savedAttachments = attachmentRepository.saveAll(allAttachments);
                        if (!savedAttachments.isEmpty()) {
                            logger.info("Attachments saved successfully.");
                        } else {
                            logger.warn("Failed to save attachments.");
                        }
                    } catch (Exception e) {
                        logger.error("Error occurred while saving attachments: {}", e.getMessage(), e);
                        throw new RuntimeException("Failed to save attachments", e);
                    }
                }
            }

            // Step 4: Mark Emails as Seen
            if (!allSavedEmailLogs.isEmpty()) {
                Message[] processedMessages = allSavedEmailLogs.stream()
                        .map(log -> {
                            String key = log.getSubject() + "_" + log.getMailReceiveDate();
                            return messageMap.get(key);
                        })
                        .filter(Objects::nonNull)
                        .toArray(Message[]::new);

                if (processedMessages.length > 0) {
                    inbox.setFlags(processedMessages, new Flags(Flags.Flag.SEEN), true);
                    logger.info("Emails marked as SEEN.");
                } else {
                    logger.warn("No messages to mark as SEEN.");
                }
            } else {
                logger.warn("No email logs were saved, so no emails marked as SEEN.");
            }

            inbox.close(true); // Close folder after processing
            return new ResponseResult(true, MessageConstants.EMAIL_SEND_SUCCESS);

        } catch (Exception e) {
            logger.error("Error fetching emails: ", e);
            return new ResponseResult(false, "Error: " + e.getMessage());
        }
    }

    /**
     * getTextFromMessage is validating it is text/plain or multipart/
     *
     * @param message from body of the unread mail and validating it is text/plain or multipart/*
     */
    private String getTextFromMessage(Message message) throws Exception {
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            return getTextFromMultipart((Multipart) message.getContent());
        }
        return "";
    }

    /**
     * getTextFromMultipart is validating it is text/plain or multipart/
     *
     * @param multipart from body of the unread mail and validating multipart from getTextFromMessage/*
     */
    private String getTextFromMultipart(Multipart multipart) throws Exception {
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart part = multipart.getBodyPart(i);

            if (part.isMimeType("text/plain")) {
                return part.getContent().toString();
            } else if (part.isMimeType("text/html")) {
                String html = (String) part.getContent();
                return Jsoup.parse(html).text();
            } else if (part.getContent() instanceof Multipart) {
                return getTextFromMultipart((Multipart) part.getContent());
            }
        }
        return "";
    }

    /**
     * Extracts and saves attachment files from the given email part,
     * returning the list of URLs or file paths where the attachments are stored.
     *
     * @param part the email part containing potential attachments
     * @return a list of file paths or URLs pointing to the saved attachments
     * @throws Exception if an error occurs while processing attachments
     */
    private List<String> handleAttachmentUrls(Part part) throws Exception {
        List<String> attachmentUrls = new ArrayList<>();
        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                String disposition = bodyPart.getDisposition();

                if (disposition != null &&
                        (disposition.equalsIgnoreCase(Part.ATTACHMENT) || disposition.equalsIgnoreCase(Part.INLINE))) {
                    String fileName = bodyPart.getFileName();
                    InputStream is = bodyPart.getInputStream();

                    String uniqueId = String.valueOf(System.nanoTime());

                    File tempFile = File.createTempFile("attachment-" + uniqueId + "-", "-" + fileName);
                    Files.copy(is, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    String fileUrl = s3service.uploadFile(tempFile);
                    attachmentUrls.add(fileUrl);

                    tempFile.delete();
                } else if (bodyPart.isMimeType("multipart/*")) {
                    attachmentUrls.addAll(handleAttachmentUrls(bodyPart));
                }
            }
        }
        return attachmentUrls;
    }

    /**
     * Retrieves all email logs from the database along with their associated attachments.
     *
     * @return a list of EmailLogWithAttachmentsDTO objects representing emails and their attachments
     * if role is admin, it will return attachment otherwise attachment won't send
     */
    public List<EmailLogWithAttachmentsDTO> getAllEmailLogsWithAttachments() {

        boolean isAdmin = getRole();
        logger.info("Check is admin: {} ", isAdmin);

        List<EmailLogWithAttachmentsDTO> resultList = new ArrayList<>();

        for (EmailLog log : emailLogRepository.findAllWithAttachments()) {
            try {
                List<String> attachmentUrls = isAdmin
                        ? log.getAttachments().stream()
                        .map(attachment -> {
                            try {
                                return attachment.getAttachmentUrl();
                            } catch (Exception e) {
                                logger.error("Error getting attachment URL for email log ID {}: {}", log.getId(), e.getMessage());
                                return null;  // Return null if an exception occurs
                            }
                        })
                        .filter(Objects::nonNull)  // Filter out null values
                        .toList()
                        : List.of();  // Return empty list for non-admins

                EmailLogWithAttachmentsDTO emailLogDTO = new EmailLogWithAttachmentsDTO(
                        log.getFromEmail(),
                        log.getToEmail(),
                        log.getSubject(),
                        log.getBody(),
                        log.getMailReceiveDate(),
                        log.getCreatedBy(),
                        attachmentUrls
                );

                // Add to result list after processing the log
                resultList.add(emailLogDTO);

            } catch (Exception e) {
                logger.error("Error processing email log ID {}: {}", log.getId(), e.getMessage());
            }
        }

        // Return the final result list after processing all logs
        return resultList;
    }

    /**
     * Retrieves current role for logged user
     *
     * @return boolean if current role is admin, it will return true
     * otherwise false based on logger user using security context
     */
    public boolean getRole() {
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

