package com.example.services;
import com.example.models.User;

public class UserSession {

    private static UserSession instance;
    private User currentUser;
    private String userUid; // Store Firebase UID separately
    private UserAuth userAuth;

    // Private constructor to prevent direct instantiation
    private UserSession() {
        this.currentUser = null;
        this.userUid = null;
    }

    // Get the singleton instance
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    // Set current logged-in user
    public void setCurrentUser(User user, String uid, UserAuth userAuth) {
        this.currentUser = user;
        this.userUid = uid;
        this.userAuth = userAuth;
    }

    // Get current user
    public User getCurrentUser() {
        return currentUser;
    }

    // Get current user's UID
    public String getUserUid() {
        return userUid;
    }

    public UserAuth getUserAuth() {return userAuth;}

    // Check if user is logged in
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    // Get user's full name
    public String getUserFullName() {
        if (currentUser != null) {
            return currentUser.getfName() + " " + currentUser.getlName();
        }
        return "Guest";
    }

    // Get user's email
    public String getUserEmail() {
        if (currentUser != null) {
            return currentUser.getEmail();
        }
        return null;
    }

    // Clear session (logout)
    public void logout() {
        this.currentUser = null;
        this.userUid = null;
    }

    // Check if current user has specific permissions (you can expand this)
    public boolean hasPermission(String permission) {
        if (!isLoggedIn()) {
            return false;
        }

        // Add your permission logic here
        // For example, check user roles, admin status, etc.
        return true; // Placeholder
    }

    // Get session info for debugging
    public String getSessionInfo() {
        if (isLoggedIn()) {
            return "Session: " + getUserFullName() + " (" + getUserEmail() + ")";
        }
        return "Session: Not logged in";
    }


}