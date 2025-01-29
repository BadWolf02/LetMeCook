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

public class Firebase {
    FirebaseFirestore db = FirebaseFirestore.getInstance(); // initialise database
    FirebaseAuth mAuth = FirebaseAuth.getInstance(); // initialise authentication
    private final Context context;

    // Constructor to pass activity context
    public Firebase(Context context) {
        this.context = context;
    }

    /*
    // Method adds a user to the database
    public boolean createUser(String username, String password) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("password", password);

        // Password strength check
        if (password.length() >= 8) {
            // Log information on successful sign up
            // Log information on failed sign up
            db.collection("users")
                    .add(user)
                    .addOnSuccessListener(documentReference -> Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId()))
                    .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));

            Toast.makeText(context, "Sign Up successful!", Toast.LENGTH_SHORT).show();
            return true;
        }
        Toast.makeText(context, "Sign Up failed. Password must be at least 8 characters long", Toast.LENGTH_SHORT).show();
        return false;
    }
    */
    /*
    public void loginUser(String username, String password) {
        db.collection("users")
                .whereEqualTo("username", username)
                .whereEqualTo("password", password)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) { // if success and not empty
                        // Login successful message
                        Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show();

                        // Navigate to MainActivity after successful login
                        Intent intent = new Intent(context, MainActivity.class);
                        context.startActivity(intent);

                    } else {
                        // Login failed message
                        Toast.makeText(context, "Invalid username or password", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Display errors
                    Toast.makeText(context, "An error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    */
    // Create user using email and password
    public void addUserAuth(String username, String email, String password) {
        Map<String, Object> user = new HashMap<>();
        // TODO email, username & password restrictions
        // TODO automatically logs user in when account created. Must fix
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener((Activity) context, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success");
                        // Proceed to Login after successful sign up
                        sendEmailVerification();
                        Intent intent = new Intent(context, LoginActivity.class);
                        context.startActivity(intent);
                        ((Activity) context).finish();
                        // Create custom user details in Firestore
                        user.put("username", username);
                        user.put("email", email);
                        user.put("password", password); // TODO remove
                        // TODO add more fields as they become necessary
                        db.collection("users")
                                .add(user)
                                .addOnSuccessListener(documentReference -> Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId()))
                                .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(context, "Account creation failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void loginUserAuth(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener((Activity) context, task -> {
                    if (task.isSuccessful() && isEmailVerified()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");
                        // FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(context, "Authentication successful.", Toast.LENGTH_SHORT).show();
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

    public void signOut() {
        mAuth.signOut();
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
        ((Activity) context).finish();
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

}