package com.ideas2it.emailLoggingSystem.dto;

public class ResponseResult {
    private boolean success;
    private String message;

    public ResponseResult(boolean success, String message) {
        this.success =  success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
