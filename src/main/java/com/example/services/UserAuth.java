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

    public UserAuth(Firestore db) {
        this.db = db;
        this.auth = FirebaseAuth.getInstance();
    }

    // Updated register method with password hashing
    public String registerUser(String email, String password, String firstName, String lastName, LocalDate createdAt) throws Exception {
        // Create Firebase Auth user (without password - we'll manage passwords ourselves)
        CreateRequest request = new CreateRequest()
                .setEmail(email)
                .setDisplayName(firstName + " " + lastName);

        UserRecord userRecord = auth.createUser(request);
        String uid = userRecord.getUid();

        // Hash the password
        String[] saltAndHash = PasswordUtils.hashPasswordWithSalt(password);
        String salt = saltAndHash[0];
        String passwordHash = saltAndHash[1];

        // Create user document with password hash - matching your User model field names
        Map<String, Object> userData = new HashMap<>();
        userData.put("fName", firstName);           // Using your field name
        userData.put("lName", lastName);            // Using your field name
        userData.put("email", email);
        userData.put("hashedPass", passwordHash);   // Using your field name
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
            DocumentSnapshot doc = db.collection("users").document(uid).get().get();

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
            if (doc.contains("balance")) {
                try {
                    balance = doc.getDouble("balance");
                } catch (Exception e) {
                    System.out.println("Error retrieving balance: " + e.getMessage());
                    balance = 0.0;
                }
            } else {
                System.out.println("Balance field not found, defaulting to 0.0");
            }

            // Create and return User object using your constructor
            // Note: Your constructor expects hashedPass, but we don't want to return it for security
            // So I'll create a simplified version
            User user = new User(
                    doc.getString("fName"),
                    doc.getString("lName"),
                    "", // Empty string for hashedPass - don't return password hash
                    doc.getString("email"),
                    LocalDate.now(),// Your constructor uses LocalDate but stores Date - this is a design choice
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

    // Keep the original token-based login for compatibility if needed
    public User loginUserWithToken(String idToken) throws Exception {
        FirebaseToken decodedToken = auth.verifyIdToken(idToken);
        String uid = decodedToken.getUid();

        DocumentSnapshot doc = db.collection("users").document(uid).get().get();
        if (!doc.exists()) {
            throw new Exception("User data not found in database");
        }

        // Update last login
        db.collection("users").document(uid)
                .update("lastLogin", new Date()).get();


        double balance = doc.contains("balance") ? doc.getDouble("balance") : 0.0;

        // Create and return User object
        User user = new User(
                doc.getString("fName"),
                doc.getString("lName"),
                "", // Don't return password hash
                doc.getString("email"),
                LocalDate.now(),
                balance
        );

        return user;
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

}
