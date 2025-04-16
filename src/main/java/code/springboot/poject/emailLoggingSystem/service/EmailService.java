package code.springboot.poject.emailLoggingSystem.service;

import code.springboot.poject.emailLoggingSystem.config.ImapConfig;
import code.springboot.poject.emailLoggingSystem.dto.FetchResult;
import code.springboot.poject.emailLoggingSystem.entity.EmailLog;
import code.springboot.poject.emailLoggingSystem.entity.User;
import code.springboot.poject.emailLoggingSystem.repository.EmailLogRepository;
import code.springboot.poject.emailLoggingSystem.repository.UserRepository;
import code.springboot.poject.emailLoggingSystem.security.JwtUtil;
import jakarta.mail.*;
import jakarta.mail.search.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.*;

@Service
public class EmailService {

    private final ImapConfig imapConfig;
    private Store imapStore;
    private final S3Service s3service;

    @Autowired
    private EmailLogRepository emailLogRepository;
    private UserRepository userRepository;

    private User user;
    private JwtUtil jwtUtil;

    @Autowired
    public EmailService(ImapConfig imapConfig, Store imapStore, S3Service s3service, UserRepository userRepository, JwtUtil jwtUtil) {
        this.imapConfig = imapConfig;
        this.imapStore = imapStore;
        this.s3service = s3service;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostConstruct
    public void initializeImapStore() {
        try {
            // Set up IMAP properties based on the configuration
            Properties properties = new Properties();
            properties.put("mail.imap.host", imapConfig.getHost());
            properties.put("mail.imap.port", imapConfig.getPort());
            properties.put("mail.imap.username", imapConfig.getUsername());
            properties.put("mail.imap.password", imapConfig.getPassword());
            properties.put("mail.imap.starttls.enable", "true"); // Enable StartTLS
            properties.put("mail.imap.ssl.enable", "true"); // Enable SSL if needed

            // Create a session with the provided properties
            Session session = Session.getInstance(properties);

            System.out.println("mapConfig.getUsername()" + imapConfig.getUsername() + "pwd" + imapConfig.getPassword());
            // Create the Store object for IMAP
            imapStore = session.getStore("imap");

            // Connect to the IMAP server using the credentials from the configuration
            imapStore.connect(imapConfig.getHost(), imapConfig.getUsername(), imapConfig.getPassword());
            System.out.println("Successfully connected to the IMAP server!");
        } catch (Exception e) {
            System.err.println("Error initializing IMAP store: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<EmailLog> getEmailLogsByUserId(Long userId) {
        return emailLogRepository.findByUserId(userId);
    }

    public FetchResult fetchEmails(HttpServletRequest request , LocalDate date) {
        try {
            // Check if IMAP store is connected, reconnect if not
            if (!imapStore.isConnected()) {
                imapStore.connect(imapConfig.getHost(), imapConfig.getUsername(), imapConfig.getPassword()); // Use correct IMAP server and credentials
            }

            Folder inbox = imapStore.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            // Extract token from the request header
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                System.out.println("Invalid or missing Authorization token.");
                inbox.close(false);
                return new FetchResult(false, "Invalid or missing Authorization token.");
            }

            token = token.substring(7); // Remove "Bearer " prefix
            Long userId = jwtUtil.extractUserId(token);
            if (userId == null) {
                System.out.println("User ID not found in the token.");
                inbox.close(false);
                return new FetchResult(false, "User ID not found in the token");
            }

            System.out.println("User ID extracted: " + userId);

            // Get today's date with time set to 00:00:00
            Calendar startCal = Calendar.getInstance();
            if ( date != null) {
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

            // Create search term for today's emails
            SearchTerm todayTerm = new AndTerm(
                    new ReceivedDateTerm(ComparisonTerm.GE, startOfDay),
                    new ReceivedDateTerm(ComparisonTerm.LT, endOfDay)
            );

            // Only fetch unseen (unread) messages
            FlagTerm unseenFlagTerm = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
            SearchTerm combinedTerm = new AndTerm(todayTerm, unseenFlagTerm);

            // Search for emails
            Message[] messages = inbox.search(combinedTerm);

            if (messages.length == 0) {
                System.out.println("No new (unseen) emails to process");
                inbox.close(false);
                return new FetchResult(false, "No new (unseen) emails to process.");
            }

            System.out.println("Total Messages: " + messages.length);

            for (Message message : messages) {
                Address[] fromAddresses = message.getFrom();
                String fromEmail = (fromAddresses != null && fromAddresses.length > 0) ? fromAddresses[0].toString() : "Unknown";

                Address[] toAddresses = message.getRecipients(Message.RecipientType.TO);
                String toEmail = (toAddresses != null && toAddresses.length > 0) ? toAddresses[0].toString() : "Unknown";

                String subject = message.getSubject();
                String body = getTextFromMessage(message);
                String attachURL = hasAttachment(message);

                System.out.println("Email from: " + fromEmail + " to: " + toEmail + ", Subject: " + subject);
                System.out.println("Body: " + body + ", Attachment: " + attachURL + (attachURL != null ? "Yes" : "No"));

                // Save the email details to the database or logging system
                saveEmailDetailsToDatabase(fromEmail, toEmail, subject, body, message.getSentDate(), attachURL, userId);
                // Mark the message as read
                message.setFlag(Flags.Flag.SEEN, true);
            }

            inbox.close(false);
            return new FetchResult(true, "✅ Emails fetched and processed successfully.");
        } catch (Exception e) {
            System.err.println("Error fetching emails: " + e.getMessage());
            e.printStackTrace();
            try {
                // Close resources in case of error
                if (imapStore.isConnected()) {
                    imapStore.close();
                }
            } catch (Exception ex) {
                System.err.println("Error closing IMAP store: " + ex.getMessage());
                return new FetchResult(false, "❌ Error: " + e.getMessage());
            }
            return new FetchResult(false, "❌ Error: " + e.getMessage());
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

    private String hasAttachment(Part part) throws Exception {
        String fileUrl = null;
        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                String disposition = bodyPart.getDisposition();

                if (disposition != null &&
                        (disposition.equalsIgnoreCase(Part.ATTACHMENT) || disposition.equalsIgnoreCase(Part.INLINE))) {
                    String fileName = bodyPart.getFileName();
                    InputStream is = bodyPart.getInputStream();

                    // Save to a temp file
                    File tempFile = File.createTempFile("attachment-", "-" + fileName);
                    Files.copy(is, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    // Upload using your S3Service
                    fileUrl = s3service.uploadFile(tempFile);
                    System.out.println("Uploaded attachment to s3: " + fileUrl);

                    // Clean up
                    tempFile.delete();
                    return fileUrl; // Return on first found/uploaded attachment

                } else if (bodyPart.isMimeType("multipart/*")) {
                    String nestedUrl = hasAttachment(bodyPart);
                    if (nestedUrl != null) {
                        return nestedUrl;
                    }
                }
            }
        }
        return null;
    }

    // Helper method to save email details to database
    private void saveEmailDetailsToDatabase(String from, String toEmail, String subject, String body, Date timestamp, String s3Link, Long userId) {
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                System.out.println("User not found with ID: " + userId);
                return;
            }
            User user = optionalUser.get();
            // Save the email details (e.g., into a database or log file)
            // Assuming you have an email repository or a database table to save this information.
            EmailLog emailDetails = new EmailLog(from, toEmail, subject, body, timestamp, s3Link, user);
            emailLogRepository.save(emailDetails);  // Example repository save
            System.out.println("Email details saved to the database.");
        } catch (Exception e) {
            // Log the exception (optional: you can log to a file or monitoring tool)
            e.printStackTrace();

            // Return failure message
            System.err.println("Error saving email details to the database: " + e.getMessage());
            e.getMessage();
        }
    }
}
