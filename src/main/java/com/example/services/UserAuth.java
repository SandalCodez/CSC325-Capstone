package com.example.services;
import com.example.models.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UserAuth {
    private final FirebaseAuth auth;
    private final Firestore db;
    private User user;

    public UserAuth(Firestore db) {
        this.db = db;
        this.auth = FirebaseAuth.getInstance();
    }

    // Updated register method with password hashing
    public String registerUser(String email, String password, String firstName, String lastName, LocalDate createdAt) throws Exception {
        CreateRequest request = new CreateRequest()
                .setEmail(email)
                .setDisplayName(firstName + " " + lastName);

        UserRecord userRecord = auth.createUser(request);
        String uid = userRecord.getUid();

        String[] saltAndHash = PasswordUtils.hashPasswordWithSalt(password);
        String salt = saltAndHash[0];
        String passwordHash = saltAndHash[1];

        Map<String, Object> userData = new HashMap<>();
        userData.put("fName", firstName);           // Using field name
        userData.put("lName", lastName);            // Using field name
        userData.put("email", email);
        userData.put("hashedPass", passwordHash);   // Using field name
        userData.put("salt", salt);                 // Store salt separately
        userData.put("createdAt", new Date());
        userData.put("balance", 0.0);
        // Using Date instead of LocalDate

        // Store in Firestore
        db.collection("users").document(uid).set(userData).get();

        System.out.println("User registered with UID: " + uid);
        return uid;
    }

    private String currentUserUid; // Store UID for session management

    public User loginUser(String email, String password) throws Exception {
        try {
            // Get user by email from Firebase Auth
            UserRecord userRecord = auth.getUserByEmail(email);
            String uid = userRecord.getUid();

            // Get user data from Firestore
            DocumentSnapshot doc = db.collection("users")
                    .document(uid)
                    .get()
                    .get();

            if (!doc.exists()) {
                throw new Exception("User data not found");
            }

            // Verify password using your field names
            String storedHash = doc.getString("hashedPass");
            String salt = doc.getString("salt");

            if (storedHash == null || salt == null) {
                throw new Exception("Invalid user data");
            }

            if (!PasswordUtils.verifyPassword(password, salt, storedHash)) {
                throw new Exception("Invalid password");
            }

            // Update last login (you might want to add this field to your User model later)
            db.collection("users").document(uid)
                    .update("lastLogin", new Date()).get();

            // Store UID for session management
            this.currentUserUid = uid;

            System.out.println("Checking for balance field...");

            double balance = 0.0;
            if (doc.exists() && doc.contains("balance")) {
                try {
                    balance = doc.getDouble("balance");
                    System.out.println("Final balance before return: " + balance);
                } catch (Exception e) {
                    System.out.println("Error retrieving balance: " + e.getMessage());
                    balance = 0.0;
                }
            } else {
                System.out.println("Balance field not found, defaulting to 0.0");
            }

            User user = new User(
                    doc.getString("fName"),
                    doc.getString("lName"),
                    "", // Empty string for hashedPass - don't return password hash
                    doc.getString("email"),
                    LocalDate.now(),
                    balance
            );

            return user;

        } catch (FirebaseAuthException e) {
            throw new Exception("User not found: " + e.getMessage());
        }
    }

    // Get the UID of the last logged-in user
    public String getCurrentUserUid() {
        return currentUserUid;
    }

    public void updateUserBalance(String uid, double newBalance) {
        new Thread(() -> {
            try {
                ApiFuture<WriteResult> future = db.collection("users").document(uid)
                        .update("balance", newBalance);
                future.get();  // wait for completion
                System.out.println("Balance updated (async).");
            } catch (Exception e) {
                System.out.println("Error updating balance (async): " + e.getMessage());
            }
        }).start();
    }

    public User getLoggedInUser() {
        return this.user;
    }

    public void setUser(User loggedInUser) {
        this.user = loggedInUser;
    }
}
