package com.ideas2it.emailLoggingSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmailLogWithAttachmentsDTO {

    private String fromEmail;
    private String toEmail;
    private String subject;
    private String body;
    private LocalDateTime mailReceiveDate;
    private List<String> attachmentUrls;
}
