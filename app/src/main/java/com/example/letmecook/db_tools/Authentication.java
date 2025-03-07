package com.example.letmecook.db_tools;

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

    /**
     * Adds a new user to the authentication system.
     *
     * This method takes a username, email, and password as input and performs the following steps:
     * 1. **Input Validation:** Checks if any of the input fields (username, email, password) are empty.
     *    If any field is empty, it displays a toast message "Please enter all fields" and returns, preventing further processing.
     * 2. **Username Uniqueness Check:** Calls the `usernameExists` method (assumed to be defined elsewhere) to check if the provided username already exists in the system.
     * 3. **Conditional User Creation:**
     *    - If `usernameExists` returns `true` (username exists), it displays a toast message "Username already exists".
     *    - If `usernameExists` returns `false` (username is unique), it calls the `createUser` method (assumed to be defined elsewhere) to create the user with the provided credentials.
     *
     * @param username The desired username for the new user.
     * @param email    The email address of the new user.
     * @param password The password for the new user.
     *
     * @throws IllegalArgumentException if context is null
     */ // Create user using email and password
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

    /**
     * Creates a new user account with the provided username, email, and password.
     *
     * This method utilizes Firebase Authentication to create a new user account.
     * Upon successful creation, it sends an email verification link to the user's
     * email address, stores the user's information in Firestore, and navigates
     * to the LoginActivity.
     * If the account creation fails, it displays an error message via Toast.
     *
     * @param username The desired username for the new user.
     * @param email    The email address of the new user.
     * @param password The password for the new user's account.
     * @throws IllegalArgumentException if any of the input parameters (username, email, password) are null or empty
     */
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

    /**
     * Authenticates a user with the provided email and password using Firebase Authentication.
     *
     * This method attempts to sign in a user with the given credentials. It performs the following:
     * 1. **Input Validation:** Checks if the email and password fields are empty. If either is empty,
     *    it displays a Toast message prompting the user to enter both.
     * 2. **Firebase Authentication:** If the inputs are valid, it utilizes `mAuth.signInWithEmailAndPassword()`
     *    to authenticate with Firebase.
     * 3. **Authentication Result Handling:**
     *    - **Success with Email Verification:** If the authentication is successful and the user's email
     *      is verified, it logs the success, shows a success Toast, navigates to the `MainActivity`,
     *      and finishes the current activity.
     *    - **Success without Email Verification:** If the authentication is successful but the user's
     *      email is not verified, it logs a warning, and displays a Toast indicating that the email
     *      needs to be verified.
     *    - **Failure:** If the authentication fails (incorrect credentials, network issue, etc.),
     *      it logs the failure with the exception details and displays a Toast indicating that
     *      the login details are incorrect.
     *
     * @param email    The user's email address.
     * @param password The user's password.
     */
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

        String householdID = UUID.randomUUID().toString();
        // Create user details in Firestore
        user.put("username", username);
        user.put("email", email);
        user.put("householdID", householdID);
        user.put("invites", new ArrayList<>());
        user.put("favourite_recipes", new ArrayList<>());
        db.collection("users")
                .document(uid)
                .set(user)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "DocumentSnapshot added with ID: " + uid))
                .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));// Create new Household
        // Create household details in Firestore
        household.put("householdName", username + "'s Household");
        ArrayList<String> members = new ArrayList<>();
        members.add(mAuth.getCurrentUser().getUid());
        household.put("members", members);
        household.put("invited", new ArrayList<>());
        household.put("inventory", new HashMap<String, Integer>());
        household.put("shopping-list", new ArrayList<>());
        db.collection("households")
                .document(householdID)
                .set(household)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "DocumentSnapshot added with ID: " + householdID))
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