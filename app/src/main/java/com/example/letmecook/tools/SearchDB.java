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

    // Used for if there are multiple households allowed
    public interface OnHouseholdsRetrievedListener {
        void onHouseholdsRetrieved(ArrayList<String> households);
    }

    public void getUserHouseholdIDs(String uid, OnHouseholdsRetrievedListener listener) {
        getUserDocumentByIDAsync(uid, userDocument -> {
                if (userDocument != null) {
                    ArrayList<String> households = (ArrayList<String>) userDocument.get("households");
                    Log.d(TAG, "User households: " + households);
                    listener.onHouseholdsRetrieved(households);
                } else {
                    listener.onHouseholdsRetrieved(new ArrayList<String>());
                }
        });
    }

    // Used for if there is one household allowed
    public interface OnHouseholdRetrievedListener {
        void onHouseholdRetrieved(String hid);
    }

    public void getUserHouseholdID(String uid, OnHouseholdRetrievedListener listener) {
        getUserDocumentByIDAsync(uid, userDocument -> {
           if (userDocument != null) {
               String hid = (String) userDocument.get("householdID");
               Log.d(TAG, "User household: " + hid);
               listener.onHouseholdRetrieved(hid);
           } else {
               listener.onHouseholdRetrieved(null);
           }
        });
    }

    public interface OnHouseholdNameRetrievedListener {
        void onHouseholdNameRetrieved(String householdName);
    }

    public void getHouseholdName(String uid, OnHouseholdNameRetrievedListener listener) {
        getUserHouseholdID(uid, hid -> {
            if (hid != null) {
                getHouseholdByIDAsync(hid, householdDocument -> {
                    if (householdDocument != null) {
                        String householdName = (String) householdDocument.get("householdName");
                        Log.d(TAG, "Household name: " + householdName);
                        listener.onHouseholdNameRetrieved(householdName);
                    } else {
                        listener.onHouseholdNameRetrieved(null);
                    }
                });
            } else {
                listener.onHouseholdNameRetrieved(null);
            }
        });
    }

    public void getUserInvites(String uid, OnHouseholdsRetrievedListener listener) {
        getUserDocumentByIDAsync(uid, userDocument -> {
            if (userDocument != null) {
                ArrayList<String> invites = (ArrayList<String>) userDocument.get("invites");
                Log.d(TAG, "Households invited to: " + invites);
                listener.onHouseholdsRetrieved(invites);
            } else {
                listener.onHouseholdsRetrieved(new ArrayList<String>());
            }
        });
    }

    public void getHouseholdInvites(String hid, OnHouseholdsRetrievedListener listener) {
        getHouseholdByIDAsync(hid, householdDocument -> {
            if (householdDocument != null) {
                ArrayList<String> invited = (ArrayList<String>) householdDocument.get("invited");
                Log.d(TAG, "Users invited: " + invited);
                listener.onHouseholdsRetrieved(invited);
            } else {
                listener.onHouseholdsRetrieved(new ArrayList<String>());
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
    public void getHouseholdByIDAsync(Object hid, OnDocumentRetrievedListener listener) {
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
