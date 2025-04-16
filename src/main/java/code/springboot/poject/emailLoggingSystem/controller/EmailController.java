package code.springboot.poject.emailLoggingSystem.controller;

import code.springboot.poject.emailLoggingSystem.dto.FetchResult;
import code.springboot.poject.emailLoggingSystem.entity.EmailLog;
import code.springboot.poject.emailLoggingSystem.repository.EmailLogRepository;
import code.springboot.poject.emailLoggingSystem.security.JwtUtil;
import code.springboot.poject.emailLoggingSystem.service.EmailService;
import code.springboot.poject.emailLoggingSystem.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/emails")
public class EmailController {
    @Autowired
    private EmailLogRepository emailLogRepository;

    private final EmailService emailService;
    private JwtUtil jwtUtil;
    private UserService userService;

    @Autowired
    public EmailController(EmailService emailService, JwtUtil jwtUtil) {
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/list")
    public List<EmailLog> listEmails(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").substring(7);
            Set<String> roles = jwtUtil.extractRoles(token);

            if (roles.contains("ADMIN")) {
                // Admin can see all emails with attachment URLs
                return emailLogRepository.findAll();
            } else {
                // Other roles: Hide attachment URLs
                return emailLogRepository.findAll().stream()
                        .map(email -> {
                            email.setAttachmentUrls(null);
                            return email;
                        })
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @GetMapping("/log/{userId}")
    public ResponseEntity<List<EmailLog>> getEmailLogById(@PathVariable Long userId, HttpServletRequest request) {
        try {
            System.out.println("userId" +userId);
            String token = request.getHeader("Authorization").substring(7);
            String username =  jwtUtil.extractUsername(token);
            Set<String> roles = jwtUtil.extractRoles(token);

            if ( roles.contains("ADMIN")) {
                Long jwtUserID =  userService.getUserIdByUsername(username);
                if (!userId.equals(jwtUserID)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.emptyList());
                }
            }

            List<EmailLog> emails = emailLogRepository.findByUserId(userId);

            // Optional strip attachment URLs if not ADMIN
            if (!roles.contains("ADMIN")) {
                emails.forEach(email -> email.setAttachmentUrls(null));
            }

            return ResponseEntity.ok(emails);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body((List<EmailLog>) Map.of("error", "Something went wrong"));        }
    }

    @GetMapping("/fetch")
    public ResponseEntity<String> fetchEmails(@RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, HttpServletRequest request) {
        try {
            // If no date is provided, use today's date
            if (date == null) {
                date = LocalDate.now();
            }
            FetchResult fetchResult  = emailService.fetchEmails(request, date);
            if (!fetchResult.isSuccess()) {
                return ResponseEntity.ok("✅ " + fetchResult.getMessage());
            }

            return ResponseEntity.ok("✅ " + fetchResult.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching emails: " + e.getMessage());
        }
    }
}
