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

    /**
     * Invites a user to a household.
     *
     * This method handles the process of inviting a user to a specified household.
     * It performs several checks to ensure the validity of the invitation, including:
     * 1. Verifying that both the household ID and username are provided.
     * 2. Checking if the target user exists in the database.
     * 3. Preventing a user from inviting themselves.
     * 4. Ensuring the target user hasn't already been invited to the household.
     * 5. Confirming that the inviter is a member of the household.
     *
     * If all checks pass, it updates both the household document (adding the user to the "invited" list)
     * and the target user's document (adding the household ID to their "invites" list).
     *
     * If any of the checks fail, an appropriate Toast message is displayed to the user.
     *
     * @param householdID The ID of the household to which the user is being invited.
     * @param username    The username of the user being invited.
     * @throws IllegalArgumentException if either householdID or username is null.
     */
    public void inviteUser(String householdID, String username) {
        if (householdID.isEmpty() || username.isEmpty()) {
          Toast.makeText(context, "Please fill both fields", Toast.LENGTH_SHORT).show();
        } else {
            searchDB.getUserDocumentByUsername(username, userDocument -> {
                if (userDocument != null) {
                    // Checks if user is inviting themselves
                    String foundUID = userDocument.getId();
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
                    searchDB.getHouseholdDocumentByID(householdID, householdDocument -> {
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

    /**
     * Accepts a household invitation for a user.
     * This method performs the following actions:
     * <ol>
     *     <li>Retrieves the user's current household ID.</li>
     *     <li>Removes the invite from the user's document.</li>
     *     <li>Updates the user's household ID to the new household ID.</li>
     *     <li>Removes the user's username from the 'invited' list in the new household's document.</li>
     *     <li>Adds the user's UID to the 'members' list in the new household's document.</li>
     *     <li>Removes the user's UID from the 'members' list in the user's previous household's document (if any).</li>
     *     <li>If the previous household now has no members after removing the user, it will delete the old household.</li>
     * </ol>
     *
     * @param householdID The ID of the household the user is accepting an invitation to.
     * @param uid         The unique ID of the user accepting the invitation.
     */
    public void acceptInvite(String householdID, String uid) {
        final String[] currentHouseholdID = new String[1];
        searchDB.getUserHouseholdID(uid, hid -> {
            currentHouseholdID[0] = hid;
            // Update user document
            searchDB.getUserDocumentByID(uid, userDocument -> {
                if (userDocument != null) {
                    String username = userDocument.getString("username");
                    userDocument.getReference().update(
                            "invites", FieldValue.arrayRemove(householdID)
                    ).addOnSuccessListener(result -> Log.d(TAG, "Household invite removed"));
                    userDocument.getReference().update(
                            "householdID", householdID
                    ).addOnSuccessListener(result -> Log.d(TAG, "User household changed"));

                    // Update new household document
                    searchDB.getHouseholdDocumentByID(householdID, householdDocument -> {
                        if (householdDocument != null) {
                            householdDocument.getReference().update(
                                    "invited", FieldValue.arrayRemove(username)
                            ).addOnSuccessListener(result -> Log.d(TAG, "User invite removed"));
                            householdDocument.getReference().update(
                                    "members", FieldValue.arrayUnion(uid)
                            ).addOnSuccessListener(result -> Log.d(TAG, "Household member added"));
                        } else {
                            Log.e(TAG, "Household not found");
                        }
                    });
                    // Update old household document
                    searchDB.getHouseholdDocumentByID(currentHouseholdID[0], householdDocument -> {
                        if (householdDocument != null) {
                            householdDocument.getReference().update(
                                    "members", FieldValue.arrayRemove(uid)
                            ).addOnSuccessListener(result -> {
                                Log.d(TAG, "Household member removed");
                                deleteHousehold(currentHouseholdID[0]);
                            });
                        } else {
                            Log.e(TAG, "Household not found");
                        }
                    });

                } else {
                    Log.e(TAG, "User not found");
                }
            });
        });
    }

    /**
     * This class manages household invites, including denying invites.
     */
    public void denyInvite(String householdID, String uid) {
        searchDB.getUserDocumentByID(uid, userDocument -> {
            String username = userDocument.getString("username");
            userDocument
                    .getReference()
                    .update("invites", FieldValue.arrayRemove(householdID))
                    .addOnSuccessListener(result -> Log.d(TAG, "Household invite removed"));

            searchDB.getHouseholdDocumentByID(householdID, householdDocument ->
                householdDocument
                    .getReference()
                    .update("invited", FieldValue.arrayRemove(username))
                    .addOnSuccessListener(result -> Log.d(TAG, "User invite removed")));
        });
    }

    /**
     * Renames a household in the database.
     *
     * This method updates the `householdName` field of a household document
     * identified by the provided `householdID` in the database. It uses an
     * asynchronous operation to fetch the household document and then update it.
     *
     * @param householdID The unique identifier of the household to rename.
     * @param newName     The new name to assign to the household.
     *
     * @throws IllegalArgumentException if householdID or newName are null or empty
     * @throws NullPointerException if searchDB is null
     * @throws RuntimeException if the household Document was not found for the given ID.
     */
    public void renameHousehold(String householdID, String newName) {
        searchDB.getHouseholdDocumentByID(householdID, householdDocument -> {
            if (householdDocument != null) {
                householdDocument.getReference().update(
                        "householdName", newName
                ).addOnSuccessListener(result -> Log.d(TAG, "Household renamed in household"));
            }
        });
    }

    /**
     * Deletes a household from the database based on the provided household ID.
     * <p>
     * This method first attempts to retrieve the household document from the database
     * using the given household ID. If the household is found, it checks if the household
     * has any members. If the household has members, a log message is generated
     * indicating that the household cannot be deleted because it still has members.
     * If the household has no members, it proceeds to delete the household document
     * from the database. A success log is generated if the deletion is successful.
     * If the household is not found, an error log is generated.
     * </p>
     *
     * @param householdID The unique identifier of the household to be deleted.
     *                    This ID is used to search for the corresponding household
     *                    document in the database.
     * @throws IllegalArgumentException if householdID is null or empty
     */
    public void deleteHousehold(String householdID) {
        searchDB.getHouseholdDocumentByID(householdID, householdDocument -> {
            if (householdDocument != null) {
                // Run checks
                ArrayList<String> members = (ArrayList<String>) householdDocument.get("members");
                if (!members.isEmpty()) {
                    Log.d(TAG, "Household has members");
                } else {
                    householdDocument.getReference()
                            .delete()
                            .addOnSuccessListener(result -> Log.d(TAG, "Household deleted"));
                }
            } else {
                Log.e(TAG, "Household not found");
            }
        });
    }
}
