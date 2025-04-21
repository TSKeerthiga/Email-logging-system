package com.ideas2it.emailLoggingSystem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.junit.jupiter.api.Test;

@SpringBootTest
public class EmailServiceTest {

    @Autowired
    private JavaMailSender emailSender;

    @Test
    void testMailSender() {
        // Verify if the JavaMailSender is properly injected and available
        assert emailSender != null;
        System.out.println("JavaMailSender is available!");
    }
}
