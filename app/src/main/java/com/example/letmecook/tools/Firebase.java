package com.example.letmecook.tools;

import static android.content.ContentValues.TAG;
import android.util.Log;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.*;

import java.util.*;

public class Firebase {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Test function to add a sample user to the database
    public void createUserTest() {
        // User info
        Map<String, Object> user = new HashMap<>();
        user.put("username", "Ada");
        user.put("password", "Lovelace");

        // Add a new document with a generated ID
        db.collection("users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
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
            db.collection("users")
                    .add(user)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding document", e);
                        }
                    });
            return true;
        }
        return false;
    }
}
