package code.springboot.poject.emailLoggingSystem.dto;

public class FetchResult {
    private boolean success;
    private String message;

    public FetchResult(boolean success, String message) {
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
