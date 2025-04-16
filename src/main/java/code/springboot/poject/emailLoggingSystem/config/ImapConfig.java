package code.springboot.poject.emailLoggingSystem.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Properties;

@Configuration
//@ConfigurationProperties(prefix = "spring.mail.imap")
public class ImapConfig {

    private AwsConfig awsConfig;

    @Value("${spring.mail.imap.host}")
    private String host;

    @Value("${spring.mail.imap.port}")
    private int port;

//    @Value("${spring.mail.imap.username}")
    private String username;

//    @Value("${spring.mail.imap.password}")
    private String password;

    public ImapConfig(AwsConfig awsConfig) {
        this.awsConfig = awsConfig;
    }
    @Bean
    public Store imapStore() throws MessagingException, JsonProcessingException {
        String secretJson = awsConfig.getSecret();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> credentials = mapper.readValue(secretJson, Map.class);

        String username = credentials.get("imap-username");
        String password = credentials.get("imap-password");
        System.out.println("IMAP username "+ username + "password: " +password);

        Properties properties = new Properties();
        properties.put("mail.imap.host", host);
        properties.put("mail.imap.port", String.valueOf(port));
        properties.put("mail.imap.ssl.enable", "true");
        properties.put("mail.imap.starttls.enable", "true");

        Session session = Session.getInstance(properties);
        Store store = session.getStore("imap");
        try {
            System.out.println("host: "+ host + ", username: "+ username + ", password: "+ password + ", port: "+ port);
            store.connect(host, username, password);
        } catch (MessagingException e) {
            System.err.println("Connection to IMAP failed: " + e.getMessage());
            throw e;  // Rethrow or handle as needed
        }
        return store;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }
}
