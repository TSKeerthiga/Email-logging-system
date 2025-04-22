package com.ideas2it.emailLoggingSystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "email_logs")
public class EmailLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_email", nullable = false, length = 255)
    private String fromEmail;

    @Column(name = "to_email", nullable = false, length = 255)
    private String toEmail;

    @Column(nullable = false, length = 255)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "mail_receive_date", columnDefinition = "TIMESTAMP")
    private LocalDateTime mailReceiveDate;

    @Column(name="created_by")
    private Long createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "emailLog", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Attachment> attachments;

    // Constructor for initializing email log
    public EmailLog(String fromEmail, String toEmail, String subject, String body, Date sentDate, Long createdBy) {
        this.fromEmail = fromEmail;
        this.toEmail = toEmail;
        this.subject = subject;
        this.body = body;
        // Convert Date to LocalDateTime
        this.mailReceiveDate = convertToLocalDateTime(sentDate);
        this.createdBy = createdBy;
    }

    // Convert java.util.Date to java.time.LocalDateTime
    private LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime();
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Overriding toString to make the log output more informative
    @Override
    public String toString() {
        return "EmailLog{" +
                "id=" + id +
                ", fromEmail='" + fromEmail + '\'' +
                ", toEmail='" + toEmail + '\'' +
                ", subject='" + subject + '\'' +
                ", body='" + (body != null ? body.substring(0, Math.min(body.length(), 50)) : "") + "..." + '\'' + // truncate body for logging
                ", mailReceiveDate=" + mailReceiveDate +
                ", createdBy=" + createdBy +
                '}';
    }
}
