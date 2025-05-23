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

    /**
     * Loads IMAP credentials from AWS Secrets Manager and processes them.
     * This method retrieves the secret JSON, parses it, and initializes the necessary credentials
     * for IMAP access.
     *
     * The secret is expected to be a JSON object containing the necessary IMAP credentials.
     *
     * @throws MessagingException if there is an issue with the IMAP credentials or connection
     * @throws JsonProcessingException if there is an error parsing the secret JSON
     */
    @PostConstruct
    public void loadImapCredentials() throws MessagingException, JsonProcessingException {
        String secretJson = awsConfig.getSecret();
        logger.info("AWS Config: " + awsConfig);

        logger.debug("AWS Secret JSON: " + secretJson);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> credentials = mapper.readValue(secretJson, Map.class);

        this.username = credentials.get("imap-username");
        this.password = credentials.get("imap-password");

        if (this.username == null || this.password == null) {
            logger.error("IMAP credentials are missing from AWS Secrets Manager.");
            throw new MessagingException("IMAP credentials are missing.");
        }

        logger.info("Loaded IMAP credentials from AWS Secrets Manager.");
        logger.debug("IMAP Username: " + this.username);
    }

    /**
     * Creates and returns an IMAP Store object for connecting to the IMAP server.
     * The method checks if the necessary IMAP credentials (username and password)
     * are loaded before attempting to create the store.
     *
     * @return the configured IMAP Store object
     * @throws MessagingException if there is an issue with the IMAP connection
     * @throws IllegalStateException if IMAP credentials are not loaded or are invalid
     */
    @Bean
    public Store imapStore() throws MessagingException {
        if (username == null || password == null) {
            throw new IllegalStateException("IMAP credentials not loaded.");
        }

        Properties properties = new Properties();
        properties.put("mail.imap.host", host);
        properties.put("mail.imap.port", String.valueOf(port));
        properties.put("mail.imap.ssl.enable", "true");
        properties.put("mail.imap.starttls.enable", "true");
        properties.put("mail.imap.username", username);
        properties.put("mail.imap.password", password);

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
