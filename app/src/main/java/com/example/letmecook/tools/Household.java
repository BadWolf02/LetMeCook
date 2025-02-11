package com.example.letmecook.tools;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.*;
import com.google.firebase.auth.*;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;

public class Household {
    FirebaseFirestore db = FirebaseFirestore.getInstance(); // initialise database
    FirebaseAuth mAuth = FirebaseAuth.getInstance(); // initialise authentication
    private final Context context;

    // Constructor to pass activity context
    public Household(Context context) {
        this.context = context;
    }

    public void inviteUser(String householdID, String username) {
        if (householdID.isEmpty() || username.isEmpty()) {
          Toast.makeText(context, "Please fill both fields", Toast.LENGTH_SHORT).show();
        } else {
            getUserDocumentByUsername(username, userDocument -> {
                if (userDocument != null) {
                    // Checks if user is inviting themselves
                    String foundUID = userDocument.getString("uid");
                    if (foundUID.equals(mAuth.getCurrentUser().getUid())) {
                        Toast.makeText(context, "You cannot invite yourself", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ArrayList<String> invites = (ArrayList<String>) userDocument.get("invites");
                    if (invites.contains(householdID)) {
                        Toast.makeText(context, "User is already invited", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Add target user to invited of current user's household
                    // TODO change to select a household
                    getHouseholdByID(householdID, householdDocument -> {
                        if (householdDocument != null) {
                            ArrayList<String> members = (ArrayList<String>) householdDocument.get("members");
                            if (!members.contains(foundUID)) {
                                Toast.makeText(context, "User is not apart of this household", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            householdDocument.getReference().update(
                                    "invited", FieldValue.arrayUnion(username)
                            ).addOnSuccessListener(result -> {
                                        Log.d(TAG, "Household updated");
                            });
                        }
                    });
                    // Add current user to invites of target user
                    userDocument.getReference().update(
                            "invites", FieldValue.arrayUnion(householdID)
                    ).addOnSuccessListener(result -> {
                        Log.d(TAG, "User invited");
                        Toast.makeText(context, "Invited " + username, Toast.LENGTH_SHORT).show();
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
    public void getUserDocumentByUsername(String username, OnUserRetrievedListener listener) {
        db.collection("users").
                whereEqualTo("username", username)
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
