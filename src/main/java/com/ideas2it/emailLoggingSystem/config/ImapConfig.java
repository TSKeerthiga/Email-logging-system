package com.ideas2it.emailLoggingSystem.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Properties;

@Configuration
@Getter
@Setter
public class ImapConfig {
    private static final Logger logger = LoggerFactory.getLogger(ImapConfig.class);

    private AwsConfig awsConfig;

    @Value("${spring.mail.imap.host}")
    private String host;

    @Value("${spring.mail.imap.port}")
    private int port;

    private String username;
    private String password;

    public ImapConfig(AwsConfig awsConfig) {
        this.awsConfig = awsConfig;
    }

    @PostConstruct
    public void loadImapCredentials() throws MessagingException, JsonProcessingException {
        String secretJson = awsConfig.getSecret();
        logger.info("AWS Config: " + awsConfig);

        // Logging the secret JSON for debugging
        logger.debug("AWS Secret JSON: " + secretJson);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> credentials = mapper.readValue(secretJson, Map.class);

        this.username = credentials.get("imap-username");
        this.password = credentials.get("imap-password");

        // Check if credentials are properly loaded
        if (this.username == null || this.password == null) {
            logger.error("IMAP credentials are missing from AWS Secrets Manager.");
            throw new MessagingException("IMAP credentials are missing.");
        }

        logger.info("Loaded IMAP credentials from AWS Secrets Manager.");
        logger.debug("IMAP Username: " + this.username);
    }

    @Bean
    public Store imapStore() throws MessagingException {
        if (username == null || password == null) {
            throw new IllegalStateException("IMAP credentials not loaded.");
        }

        // Set up IMAP connection properties
        Properties properties = new Properties();
        properties.put("mail.imap.host", host);
        properties.put("mail.imap.port", String.valueOf(port));
        properties.put("mail.imap.ssl.enable", "true");
        properties.put("mail.imap.starttls.enable", "true");
        properties.put("mail.imap.username", username);
        properties.put("mail.imap.password", password);

        // Create the session and store
        Session session = Session.getInstance(properties);
        Store store = session.getStore("imap");
        try {
            store.connect(host, username, password);
            logger.info("IMAP store connected successfully.");
        } catch (MessagingException e) {
            logger.error("IMAP connection failed: " + e.getMessage(), e);
            throw e;
        }

        return store;
    }
}
