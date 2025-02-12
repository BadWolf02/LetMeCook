package com.example.letmecook.tools;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class SearchDB {
    FirebaseFirestore db = FirebaseFirestore.getInstance(); // initialise database
    FirebaseAuth mAuth = FirebaseAuth.getInstance(); // initialise authentication

    // Constructor
    public SearchDB() {}

    // Methods
    public AtomicReference<ArrayList<String>> getUserHouseholds(String uid) {
        // Must use AtomicReference due to async search
        AtomicReference<ArrayList<String>> households = new AtomicReference<>();
        getUserDocumentByIDAsync(uid, userDocument -> {
            households.set(((ArrayList<String>) userDocument.get("households")));
            Log.d(TAG, "User households: " + households.get());
        });
        return households;
    }

    public AtomicReference<ArrayList<String>> getUserInvites(String uid) {
        // Must use AtomicReference due to async search
        AtomicReference<ArrayList<String>> invites = new AtomicReference<>();
        getUserDocumentByIDAsync(uid, userDocument -> {
            invites.set(((ArrayList<String>) userDocument.get("invites")));
            Log.d(TAG, "User invites: " + invites.get());
        });
        return invites;
    }

    // Async methods

    // Callback to handle asynchronously retrieving a user
    public interface OnUserRetrievedListener {
        void onUserRetrieved(DocumentSnapshot userDocument);
    }

    // Get snapshot for user by uid
    public void getUserDocumentByIDAsync(String uid, OnUserRetrievedListener listener) {
        db.collection("users")
                .whereEqualTo("uid", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
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

    // Get snapshot for user by uid
    public void getUserDocumentByUsernameAsync(String username, OnUserRetrievedListener listener) {
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
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
    public void getHouseholdByIDAsync(String hid, OnHouseholdRetrievedListener listener) {
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
