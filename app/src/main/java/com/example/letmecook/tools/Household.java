package com.example.letmecook.tools;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.*;
import com.google.firebase.auth.*;

import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;

public class Household {
    FirebaseFirestore db = FirebaseFirestore.getInstance(); // initialise database
    FirebaseAuth mAuth = FirebaseAuth.getInstance(); // initialise authentication
    private final Context context;

    // Constructor to pass activity context
    public Household(Context context) {
        this.context = context;
    }

    public void inviteUser(String householdID, String userID) {
        Query householdQuery = db.collection("users").whereEqualTo("householdID", householdID);
        if (userID.length() != 28) {
            Toast.makeText(context, "Please enter a valid user ID", Toast.LENGTH_SHORT).show();
        } else {
            Query userQuery = db.collection("users").whereEqualTo("uid", userID);
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
}
