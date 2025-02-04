package com.example.letmecook.tools;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.letmecook.LoginActivity;
import com.example.letmecook.MainActivity;
import com.google.firebase.firestore.*;
import com.google.firebase.auth.*;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.UUID;

public class Firebase {
    FirebaseFirestore db = FirebaseFirestore.getInstance(); // initialise database
    FirebaseAuth mAuth = FirebaseAuth.getInstance(); // initialise authentication
    private final Context context;

    // Constructor to pass activity context
    public Firebase(Context context) {
        this.context = context;
    }

    // Create user using email and password
    public void addUserAuth(String username, String email, String password) {
        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            Toast.makeText(context, "Please enter all fields", Toast.LENGTH_SHORT).show();
        } else {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener((Activity) context, task -> {
                        if (task.isSuccessful()) {
                            // Sign up success
                            Log.d(TAG, "createUserWithEmail:success");
                            sendEmailVerification();
                            createUserInFirestore(mAuth.getCurrentUser().getUid(), username, email);
                            // Proceed to Login after successful sign up
                            Intent intent = new Intent(context, LoginActivity.class);
                            context.startActivity(intent);
                            ((Activity) context).finish();
                        } else {
                            // If sign up fails
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(context, "Account creation failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public void loginUserAuth(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show();
        } else {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener((Activity) context, task -> {
                        if (task.isSuccessful() && isEmailVerified()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            Toast.makeText(context, "Authentication successful.", Toast.LENGTH_SHORT).show();
                            // Go to Main
                            Intent intent = new Intent(context, MainActivity.class);
                            context.startActivity(intent);
                            ((Activity) context).finish();
                        } else if (task.isSuccessful() && !isEmailVerified()) {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(context, "Email not verified.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(context, "Login details incorrect.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public void signOut() {
        mAuth.signOut();
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
        ((Activity) context).finish();
    }

    public void createUserInFirestore(String uid, String username, String email) {
        Map<String, Object> user = new HashMap<>();
        Map<String, Object> household = new HashMap<>();

        String householdID = UUID.randomUUID().toString();
        // Create custom user details in Firestore
        user.put("uid", uid);
        user.put("username", username);
        user.put("email", email);
        user.put("householdID", householdID);
        user.put("invites", new ArrayList<>());
        // TODO add more fields as they become necessary
        db.collection("users")
                .add(user)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));// Create new Household
        ArrayList<String> members = new ArrayList<>();
        members.add(mAuth.getCurrentUser().getUid());
        household.put("householdID", householdID);
        household.put("householdName", username + "'s Household");
        household.put("members", members);
        household.put("invited", new ArrayList<>());
        // TODO add more fields as they become necessary
        db.collection("households")
                .add(household)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));
    }

    public void sendEmailVerification() {
        // Send verification email
        // [START send_email_verification]
        final FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        user.sendEmailVerification()
                .addOnCompleteListener((Activity) context, task ->
                        Toast.makeText(context,
                        "Verification email sent to " + user.getEmail(),
                        Toast.LENGTH_SHORT).show());
        // [END send_email_verification]
    }

    public void inviteUser(String userID) {
        if (userID.length() != 28) {
            Toast.makeText(context, "Please enter a valid user ID", Toast.LENGTH_SHORT).show();
        } else {
            Query userQuery = db.collection("users").whereEqualTo("uid", userID);
            Query myQuery = db.collection("users").whereEqualTo("uid", mAuth.getCurrentUser().getUid());
            userQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {
                    // TODO implement Logs
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        // User found, retrieve the first matching document
                        DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                        String foundUser = userDoc.getString("username");
                        // TODO implement invitation logic for household ID rather than userID
                        userDoc.getReference().update("invites", FieldValue.arrayUnion(mAuth.getCurrentUser().getUid()));
                        Toast.makeText(context, "Invited " + foundUser, Toast.LENGTH_SHORT).show();
                    } else {
                        // No user found
                        Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

    public boolean isLoggedIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        return currentUser != null;
    }

    public boolean isEmailVerified() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        return currentUser != null && currentUser.isEmailVerified();
    }

}