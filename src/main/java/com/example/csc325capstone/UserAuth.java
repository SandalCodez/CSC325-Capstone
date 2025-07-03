package com.example.csc325capstone;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;

import java.util.Date;

public class UserAuth {
    private final FirebaseAuth auth;
    private final Firestore db;

    public UserAuth() {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirestoreOptions.getDefaultInstance().getService();
    }

    // Register user with email/password
    public String registerUser(String email, String password, String firstName, String lastName, Date createdAt) throws Exception {
       //MAKE CREATED AT DURING TIME OF REGISTER BTN CLICKED
        CreateRequest request = new CreateRequest()
                .setEmail(email)
                .setPassword(password)
                .setDisplayName(firstName + " " + lastName);

        UserRecord userRecord = auth.createUser(request);
        String uid = userRecord.getUid(); // Firebase generates this ID

        User user = new User(firstName,lastName, email, createdAt);

        // Use Firebase Auth UID as document ID
        db.collection("users").document(uid).set(user).get();

        System.out.println("User registered with UID: " + uid);
        return uid;
    }

    // Verify user login (when they send credentials from client)
    public User loginUser(String idToken) throws Exception {
        // Verify the token from client login
        FirebaseToken decodedToken = auth.verifyIdToken(idToken);
        String uid = decodedToken.getUid();

        // Get user data from Firestore
        DocumentSnapshot doc = db.collection("users").document(uid).get().get();
        User user = doc.toObject(User.class);

        // Update last login
        db.collection("users").document(uid)
                .update("lastLogin", new Date()).get();

        return user;
    }
}
