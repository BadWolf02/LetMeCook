package com.example.letmecook.tools;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.letmecook.MainActivity;
import com.google.firebase.firestore.*;

import java.util.*;

public class Firebase {
    FirebaseFirestore db = FirebaseFirestore.getInstance(); // initialise database
    private final Context context;

    // Constructor to pass activity context
    public Firebase(Context context) {
        this.context = context;
    }

    // Method adds a user to the database
    public boolean createUser(String username, String password) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        // TODO hash password
        user.put("password", password);

        // Password strength check
        // TODO implement check for existing users & improve password check
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

                        // TODO close login?

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
}
