package com.example.letmecook.tools;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.*;
import com.google.firebase.auth.*;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

public class Household {
    FirebaseFirestore db = FirebaseFirestore.getInstance(); // initialise database
    FirebaseAuth mAuth = FirebaseAuth.getInstance(); // initialise authentication
    private final Context context;

    // Constructor to pass activity context
    public Household(Context context) {
        this.context = context;
    }

    public void inviteUser(String householdID, String userID) {
        if (householdID.isEmpty() || userID.isEmpty()) {
          Toast.makeText(context, "Please fill both fields", Toast.LENGTH_SHORT).show();
        } else if (userID.length() != 28) { // Standard userID length
            Toast.makeText(context, "Please enter a valid user ID", Toast.LENGTH_SHORT).show();
        } else {
            getUserByID(userID, userDocument -> {
                if (userDocument != null) {
                    String foundUser = userDocument.getString("username");
                    // Add current user to invites of target user
                    // TODO change to add household ID with selected household
                    userDocument.getReference().update(
                        "invites", FieldValue.arrayUnion(mAuth.getCurrentUser().getUid())
                            ).addOnSuccessListener(result -> {
                                Log.d(TAG, "User invited");
                                Toast.makeText(context, "Invited " + foundUser, Toast.LENGTH_SHORT).show();
                    });
                    // Add target user to invited of current user's household
                    // TODO change to select a household
                    getHouseholdByID(householdID, householdDocument -> {
                        if (householdDocument != null) {
                            householdDocument.getReference().update(
                                    "invited", FieldValue.arrayUnion(foundUser)
                            ).addOnSuccessListener(result -> {
                                        Log.d(TAG, "Household updated");
                            });
                        }
                    });
                } else {
                    Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    // Callback to handle asynchronously retrieving a user
    public interface OnUserRetrievedListener {
        void onUserRetrieved(DocumentSnapshot userDocument);
    }

    // Get snapshot for user by uid
    public void getUserByID(String uid, OnUserRetrievedListener listener) {
        db.collection("users").
                whereEqualTo("uid", uid)
                .get().
                addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                // User found, retrieve the first matching document
                Log.d(TAG, "User found");
                listener.onUserRetrieved(queryDocumentSnapshots.getDocuments().get(0));
            } else {
                Log.e(TAG, "User not found");
                listener.onUserRetrieved(null);
            }
        });
    }

    // Callback to handle asynchronously retrieving a household
    public interface OnHouseholdRetrievedListener {
        void onHouseholdRetrieved(DocumentSnapshot householdDocument);
    }

    // Get snapshot for household by householdID
    public void getHouseholdByID(String hid, OnHouseholdRetrievedListener listener) {
        db.collection("households").
                whereEqualTo("householdID", hid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                // Household found, retrieve the first matching document
                Log.d(TAG, "Household found");
                listener.onHouseholdRetrieved(queryDocumentSnapshots.getDocuments().get(0));
            } else {
                Log.e(TAG, "Household not found");
                listener.onHouseholdRetrieved(null);
            }
        });
    }


}
