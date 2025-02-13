package com.example.letmecook.tools;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

public class SearchDB {
    FirebaseFirestore db = FirebaseFirestore.getInstance(); // initialise database
    FirebaseAuth mAuth = FirebaseAuth.getInstance(); // initialise authentication

    // Constructor
    public SearchDB() {}

    // Methods

    public interface OnUserReturnListener {
        void onHouseholdRetrieved(ArrayList<String> households);
    }

    public void getUserHouseholdIDs(String uid, OnUserReturnListener listener) {
        getUserDocumentByIDAsync(uid, userDocument -> {
                if (userDocument != null) {
                    ArrayList<String> households = (ArrayList<String>) userDocument.get("households");
                    Log.d(TAG, "User households: " + households);
                    listener.onHouseholdRetrieved(households);
                } else {
                    listener.onHouseholdRetrieved(new ArrayList<String>());
                }
        });
    }

    public void getUserInvites(String uid, OnUserReturnListener listener) {
        getUserDocumentByIDAsync(uid, userDocument -> {
            if (userDocument != null) {
                ArrayList<String> invites = (ArrayList<String>) userDocument.get("invites");
                Log.d(TAG, "User invites: " + invites);
                listener.onHouseholdRetrieved(invites);
            } else {
                listener.onHouseholdRetrieved(new ArrayList<String>());
            }
        });
    }

    // Async methods

    // Callback to handle asynchronously retrieving a user
    public interface OnDocumentRetrievedListener {
        void onDocumentRetrieved(DocumentSnapshot document);
    }

    // Get snapshot for user by uid
    public void getUserDocumentByIDAsync(String uid, OnDocumentRetrievedListener listener) {
        db.collection("users")
                .whereEqualTo("uid", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        // User found, retrieve the first matching document
                        Log.d(TAG, "User found");
                        listener.onDocumentRetrieved(queryDocumentSnapshots.getDocuments().get(0));
                    } else {
                        Log.e(TAG, "User not found");
                        listener.onDocumentRetrieved(null);
                    }
                });
    }

    // Get snapshot for user by uid
    public void getUserDocumentByUsernameAsync(String username, OnDocumentRetrievedListener listener) {
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        // User found, retrieve the first matching document
                        Log.d(TAG, "User found");
                        listener.onDocumentRetrieved(queryDocumentSnapshots.getDocuments().get(0));
                    } else {
                        Log.e(TAG, "User not found");
                        listener.onDocumentRetrieved(null);
                    }
                });
    }

    // Get snapshot for household by householdID
    public void getHouseholdByIDAsync(String hid, OnDocumentRetrievedListener listener) {
        db.collection("households").
                whereEqualTo("householdID", hid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        // Household found, retrieve the first matching document
                        Log.d(TAG, "Household found");
                        listener.onDocumentRetrieved(queryDocumentSnapshots.getDocuments().get(0));
                    } else {
                        Log.e(TAG, "Household not found");
                        listener.onDocumentRetrieved(null);
                    }
                });
    }

    // Get snapshot for link by uid or householdID
    // TODO check if it returns all results
    public void getLinkByIDAsync(String id, String type, OnDocumentRetrievedListener listener) {
        if (Objects.equals(type, "uid")) {
            db.collection("users-households").
                    whereEqualTo("uid", id)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            // Household found, retrieve the first matching document
                            Log.d(TAG, "Link found");
                            listener.onDocumentRetrieved((DocumentSnapshot) queryDocumentSnapshots.getDocuments());
                        } else {
                            Log.e(TAG, "Link not found");
                            listener.onDocumentRetrieved(null);
                        }
                    });
        } else if (Objects.equals(type, "hid")) {
            db.collection("users-households").
                    whereEqualTo("householdID", id)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            // Household found, retrieve the first matching document
                            Log.d(TAG, "Link found");
                            listener.onDocumentRetrieved((DocumentSnapshot) queryDocumentSnapshots.getDocuments());
                        } else {
                            Log.e(TAG, "Link not found");
                            listener.onDocumentRetrieved(null);
                        }
                    });
        }

    }
}
