package com.ideas2it.emailLoggingSystem.context;

public class UserContextHolder {

    private static final ThreadLocal<UserContext> userContext = new ThreadLocal<>();

    public static void setUser(UserContext context) {
        userContext.set(context);
    }

    public static UserContext getUser() {
        return userContext.get();
    }

    public static Long getUserId() {
        return userContext.get() != null ? userContext.get().getUserId() : null;
    }

    public static void clear() {
        userContext.remove();
    }
}
