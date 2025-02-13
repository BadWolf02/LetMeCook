package com.example.letmecook.tools;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.letmecook.LoginActivity;
import com.example.letmecook.MainActivity;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;
import com.google.firebase.auth.*;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.UUID;

public class Authentication {
    FirebaseFirestore db = FirebaseFirestore.getInstance(); // initialise database
    FirebaseAuth mAuth = FirebaseAuth.getInstance(); // initialise authentication
    private final Context context;

    // Constructor to pass activity context
    public Authentication(Context context) {
        this.context = context;
    }

    // Create user using email and password
    public void addUserAuth(String username, String email, String password) {
        // Run checks
        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            Toast.makeText(context, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        // Check if username already exists
        usernameExists(username).addOnSuccessListener(exists -> {
            if (exists) {
                Toast.makeText(context, "Username already exists", Toast.LENGTH_SHORT).show();
            } else {
                createUser(username, email, password);
            }
        });
    }

    public void createUser(String username, String email, String password) {
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

    public void loginUserAuth(String email, String password) {
        // Run checks
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
                            // Sign in success, email unverified
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
        Map<String, Object> link = new HashMap<>();

        String householdID = UUID.randomUUID().toString();
        // Create user details in Firestore
        user.put("uid", uid);
        user.put("username", username);
        user.put("email", email);
        ArrayList<String> households = new ArrayList<>();
        households.add(householdID);
        user.put("households", households);
        user.put("invites", new ArrayList<>());
        user.put("favourite_recipes", new ArrayList<>());
        db.collection("users")
                .add(user)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));// Create new Household
        // Create household details in Firestore
        household.put("householdID", householdID);
        household.put("householdName", username + "'s Household");
        ArrayList<String> members = new ArrayList<>();
        members.add(mAuth.getCurrentUser().getUid());
        household.put("members", members);
        household.put("invited", new ArrayList<>());
        db.collection("households")
                .add(household)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));
        // Create link table for users and households
        link.put("uid", uid);
        link.put("householdID", householdID);
        link.put("householdName", username + "'s Household");
        db.collection("users-households")
                .add(link)
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


    public boolean isLoggedIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        return currentUser != null;
    }

    public boolean isEmailVerified() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        return currentUser != null && currentUser.isEmailVerified();
    }

    // Doesn't use a callback as the task needs to return a boolean
    // https://firebase.blog/posts/2016/09/become-a-firebase-taskmaster-part-3_29/?utm_source=chatgpt.com
    public Task<Boolean> usernameExists(String username) {
        // Check if username already exists in Firestore
        return db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .continueWith(task -> { // returns only when a result is found. Achieves asynchronous behaviour
                    if (task.isSuccessful() && task.getResult() != null) {
                        // Check if any documents were returned
                        return !task.getResult().isEmpty();
                    } else {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "Error checking username", task.getException());
                        }
                        return false;
                    }
                });
    }
}