package com.example.letmecook.db_tools;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.*;

import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;

public class Household {
    FirebaseAuth mAuth = FirebaseAuth.getInstance(); // initialise authentication
    SearchDB searchDB = new SearchDB();
    private final Context context;

    // Constructor to pass activity context
    public Household(Context context) {
        this.context = context;
    }


    public void inviteUserSingleHousehold(String username) {
        String uid = mAuth.getCurrentUser().getUid();
        searchDB.getUserHouseholdID(uid, householdID -> inviteUser(householdID, username));
    }

    public void inviteUser(String householdID, String username) {
        if (householdID.isEmpty() || username.isEmpty()) {
          Toast.makeText(context, "Please fill both fields", Toast.LENGTH_SHORT).show();
        } else {
            searchDB.getUserDocumentByUsername(username, userDocument -> {
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
                    searchDB.getHouseholdByID(householdID, householdDocument -> {
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
        final String[] currentHouseholdID = new String[1];
        searchDB.getUserHouseholdID(uid, hid -> currentHouseholdID[0] =hid);
        // Update user document
        searchDB.getUserDocumentByID(uid, userDocument -> {
            if (userDocument != null) {
                userDocument.getReference().update(
                        "invites", FieldValue.arrayRemove(householdID)
                ).addOnSuccessListener(result -> Log.d(TAG, "Household invite removed"));
                userDocument.getReference().update(
                        "householdID", householdID
                ).addOnSuccessListener(result -> Log.d(TAG, "User household changed"));
            } else {
                Log.e(TAG, "User not found");
            }
        });
        // Update new household document
        searchDB.getHouseholdByID(householdID, householdDocument -> {
            if (householdDocument != null) {
                householdDocument.getReference().update(
                        "invited", FieldValue.arrayRemove(uid)
                ).addOnSuccessListener(result -> Log.d(TAG, "User invite removed"));
                householdDocument.getReference().update(
                        "members", FieldValue.arrayUnion(uid)
                ).addOnSuccessListener(result -> Log.d(TAG, "Household member added"));
            } else {
                Log.e(TAG, "Household not found");
            }
        });
        // Update old household document
        searchDB.getHouseholdByID(currentHouseholdID[0], householdDocument -> {
            if (householdDocument != null) {
                householdDocument.getReference().update(
                        "members", FieldValue.arrayRemove(uid)
                ).addOnSuccessListener(result -> Log.d(TAG, "Household member removed"));
            } else {
                Log.e(TAG, "Household not found");
            }
        });
        deleteHousehold(currentHouseholdID[0]);
        // Update link document
        /*
        Map<String, String> newLink = new HashMap<>();
        newLink.put("uid", uid);
        newLink.put("householdID", householdID);
        db.collection("users-households")
                .add(newLink)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));
         */
    }

    public void denyInvite(String householdID, String uid) {
        searchDB.getUserDocumentByID(uid, userDocument ->
                userDocument.getReference().update(
                "invites", FieldValue.arrayRemove(householdID)
                ).addOnSuccessListener(result -> Log.d(TAG, "Household invite removed")));
        searchDB.getHouseholdByID(householdID, householdDocument ->
                householdDocument.getReference().update(
                "invited", FieldValue.arrayRemove(uid)
                ).addOnSuccessListener(result -> Log.d(TAG, "User invite removed")));
    }

    public void renameHousehold(String householdID, String newName) {
        searchDB.getHouseholdByID(householdID, householdDocument -> {
            if (householdDocument != null) {
                householdDocument.getReference().update(
                        "householdName", newName
                ).addOnSuccessListener(result -> Log.d(TAG, "Household renamed in household"));
            }
        });
        // TODO check if works
        searchDB.getLinkByIDAsync(householdID, "hid", householdDocument -> {
            if (householdDocument != null) {
                householdDocument.getReference().update(
                        "householdName", newName
                ).addOnSuccessListener(result -> Log.d(TAG, "Household renamed in link"));
            }
        });
    }

    public void deleteHousehold(String householdID) {
        searchDB.getHouseholdByID(householdID, householdDocument -> {
            if (householdDocument != null) {
                // Run checks
                ArrayList<String> members = (ArrayList<String>) householdDocument.get("members");
                if (!members.isEmpty()) {
                    Log.d(TAG, "Household has members");
                } else {
                    householdDocument.getReference()
                            .delete().
                            addOnSuccessListener(result -> Log.d(TAG, "Household deleted"));
                }
            } else {
                Log.e(TAG, "Household not found");
            }
        });
    }
}
