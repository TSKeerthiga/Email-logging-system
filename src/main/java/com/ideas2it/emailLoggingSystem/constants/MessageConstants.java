package com.ideas2it.emailLoggingSystem.constants;

import software.amazon.awssdk.services.secretsmanager.endpoints.internal.Value;

public class MessageConstants {
    public static final String USER_TAKEN =  "Username is already token";
    public static final String ROLE_EMPTY = "Roles cannot be empty";
    public static final String ROLE_NOT_FOUND = "Role not found";
    public static final String USER_REGISTER_SUCCESS = "User registered Successfully";
    public static final String USER_REGISTER_FAILED = "User registration failed";

    public static final String EMAIL_EMPTY = "Email is required!";
    public static final String EMAIL_INVALID = "Invalid email format!";
    public static final String USERNAME_EMPTY = "Username is required!";
    public static final String PASSWORD_EMPTY = "Password is required!";
    public static final String PASSWORD_TOO_SHORT = "Password must be at least 8 characters long!";
    public static final String PHONE_EMPTY = "Phone number is required!";
    public static final String USER_TOO_SHORT = "Username must be between 3 and 50 characters";

    public static final String INVALID_USER_PASSWORD = "Invalid username or password";
    public static final String INTERNAL_SERVER_ERROR = "Internal server error";
    public static final String EMAIL_ATTACHMENT_SEND_SUCCESS= "Emails and attachments saved successfully.";
    public static final String EMAIL_SEND_SUCCESS= "Emails saved successfully.";
    public static final String EMAIL_NOT_SAVE_ATTACHMENT_SKIP = "Emails not saved. Attachments skipped.";
    public static final String NO_UNREAD_MAIL = "No new (unread) emails to process.";
    public static final int CHUNK_SIZE = 10;
}
