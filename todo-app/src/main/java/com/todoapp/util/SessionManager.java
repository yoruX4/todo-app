package com.todoapp.util;

import com.todoapp.model.User;

/**
 * Simple in-memory session holder.
 * Stores the currently authenticated {@link User} for the lifetime of the session.
 */
public class SessionManager {

    private static User currentUser;

    private SessionManager() { /* static utility */ }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /** Clears the session (logout). */
    public static void clearSession() {
        currentUser = null;
    }
}
