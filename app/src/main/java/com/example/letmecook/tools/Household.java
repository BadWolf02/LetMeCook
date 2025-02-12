package com.example.letmecook.tools;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.*;

import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class Household {
    // FirebaseFirestore db = FirebaseFirestore.getInstance(); // initialise database
    FirebaseAuth mAuth = FirebaseAuth.getInstance(); // initialise authentication
    SearchDB searchDB = new SearchDB();
    private final Context context;

    // Constructor to pass activity context
    public Household(Context context) {
        this.context = context;
    }

    public void inviteUser(String householdID, String username) {
        if (householdID.isEmpty() || username.isEmpty()) {
          Toast.makeText(context, "Please fill both fields", Toast.LENGTH_SHORT).show();
        } else {
            searchDB.getUserDocumentByUsernameAsync(username, userDocument -> {
                if (userDocument != null) {
                    // Checks if user is inviting themselves
                    String foundUID = userDocument.getString("uid");
                    if (foundUID.equals(mAuth.getCurrentUser().getUid())) {
                        Toast.makeText(context, "You cannot invite yourself", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Checks if user is already invited
                    ArrayList<String> invites = (ArrayList<String>) userDocument.get("invites");
                    if (invites.contains(householdID)) {
                        Toast.makeText(context, "User is already invited", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Add target user to invited of current user's household
                    // TODO change to select a household
                    searchDB.getHouseholdByIDAsync(householdID, householdDocument -> {
                        if (householdDocument != null) {
                            // Checks that user is a member
                            ArrayList<String> members = (ArrayList<String>) householdDocument.get("members");
                            if (!members.contains(mAuth.getCurrentUser().getUid())) {
                                Toast.makeText(context, "You are not apart of this household. No permissions", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            householdDocument.getReference().update(
                                    "invited", FieldValue.arrayUnion(username)
                            ).addOnSuccessListener(result -> Log.d(TAG, "Household updated"));
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

    public void acceptInvite(String householdID, String uid) {
        searchDB.getUserDocumentByIDAsync(uid, userDocument -> {
            userDocument.getReference().update(
                    "invites", FieldValue.arrayRemove(householdID)
            ).addOnSuccessListener(result -> Log.d(TAG, "Household invite removed"));
            userDocument.getReference().update(
                    "households", FieldValue.arrayUnion(householdID)
            ).addOnSuccessListener(result -> Log.d(TAG, "User household added"));
        });
        searchDB.getHouseholdByIDAsync(householdID, householdDocument -> {
            householdDocument.getReference().update(
                    "invited", FieldValue.arrayRemove(uid)
            ).addOnSuccessListener(result -> Log.d(TAG, "User invite removed"));
            householdDocument.getReference().update(
                    "members", FieldValue.arrayUnion(uid)
            ).addOnSuccessListener(result -> Log.d(TAG, "Household member added"));
        });
    }
}
