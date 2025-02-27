package com.example.letmecook.db_tools;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchDB {
    FirebaseFirestore db = FirebaseFirestore.getInstance(); // initialise database

    // Constructor
    public SearchDB() {}

    // Methods
    public interface OnStringArrayRetrievedListener {
        void onStringArrayRetrieved(ArrayList<String> stringArrayList);
    }

    // Used for if there is one household allowed
    public interface OnStringRetrievedListener {
        void onStringRetrieved(String inputString);
    }

    public void getUserHouseholdID(String uid, OnStringRetrievedListener listener) {
        getUserDocumentByID(uid, userDocument -> {
           if (userDocument != null) {
               String hid = (String) userDocument.get("householdID");
               Log.d(TAG, "User household: " + hid);
               listener.onStringRetrieved(hid);
           } else {
               listener.onStringRetrieved(null);
           }
        });
    }

    public void getUserHouseholdName(String uid, OnStringRetrievedListener listener) {
        getUserHouseholdID(uid, hid -> {
            if (hid != null) {
                getHouseholdDocumentByID(hid, householdDocument -> {
                    if (householdDocument != null) {
                        String householdName = (String) householdDocument.get("householdName");
                        Log.d(TAG, "Household name: " + householdName);
                        listener.onStringRetrieved(householdName);
                    } else {
                        listener.onStringRetrieved(null);
                    }
                });
            } else {
                listener.onStringRetrieved(null);
            }
        });
    }

    public void getUserInvites(String uid, OnStringArrayRetrievedListener listener) {
        getUserDocumentByID(uid, userDocument -> {
            if (userDocument != null) {
                ArrayList<String> invites = (ArrayList<String>) userDocument.get("invites");
                Log.d(TAG, "Households invited to: " + invites);
                listener.onStringArrayRetrieved(invites);
            } else {
                listener.onStringArrayRetrieved(new ArrayList<>());
            }
        });
    }

    public void getHouseholdInvites(String hid, OnStringArrayRetrievedListener listener) {
        getHouseholdDocumentByID(hid, householdDocument -> {
            if (householdDocument != null) {
                ArrayList<String> invited = (ArrayList<String>) householdDocument.get("invited");
                Log.d(TAG, "Users invited: " + invited);
                listener.onStringArrayRetrieved(invited);
            } else {
                listener.onStringArrayRetrieved(new ArrayList<>());
            }
            });
    }

    public void getHouseholdMembers(String hid, OnStringArrayRetrievedListener listener) {
        getHouseholdDocumentByID(hid, householdDocument -> {
            if (householdDocument != null) {
                ArrayList<String> members = (ArrayList<String>) householdDocument.get("members");
                Log.d(TAG, "Members: " + members);
                listener.onStringArrayRetrieved(members);
            } else {
                Log.e(TAG, "Household not found");
                listener.onStringArrayRetrieved(new ArrayList<>());
            }
        });
    }

    // Callback to handle asynchronously retrieving a user
    public interface OnDocumentRetrievedListener {
        void onDocumentRetrieved(DocumentSnapshot document);
    }

    // Get snapshot for user by uid
    public void getUserDocumentByID(String uid, OnDocumentRetrievedListener listener) {
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // User found, retrieve the first matching document
                        Log.d(TAG, "User found");
                        listener.onDocumentRetrieved(documentSnapshot);
                    } else {
                        Log.e(TAG, "User not found");
                        listener.onDocumentRetrieved(null);
                    }
                });
    }

    // Get snapshot for user by uid
    public void getUserDocumentByUsername(String username, OnDocumentRetrievedListener listener) {
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
    public void getHouseholdDocumentByID(String hid, OnDocumentRetrievedListener listener) {
        db.collection("households")
                .document(hid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Household found, retrieve the first matching document
                        Log.d(TAG, "Household found");
                        listener.onDocumentRetrieved(documentSnapshot);
                    } else {
                        Log.e(TAG, "Household not found");
                        listener.onDocumentRetrieved(null);
                    }
                });
    }

    public interface IngredientsCallback{
        public void onIngredientsLoaded(ArrayList<Object> ingredients);
    }
    //TODO next: this isn't working, so maybe try with callback interface
    public void getIngredients(IngredientsCallback ingreedients_callback){

        CollectionReference ingreedients_ref = db.collection("ingredients");
        ingreedients_ref.get().addOnSuccessListener(ingredients_snapshot -> {
            ArrayList<Object> ingredients_list = new ArrayList<>();
            for (DocumentSnapshot ingredient : ingredients_snapshot.getDocuments()) {
                String ingredient_name = ingredient.getString("name");
                ingredients_list.add(ingredient_name);
            }
            Log.d("getting ingredients", ingredients_list.toString());
            ingreedients_callback.onIngredientsLoaded(ingredients_list);
                        //return ingredients_list.toArray();
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Error fetching ingredients", e);
            ingreedients_callback.onIngredientsLoaded(new ArrayList<Object>());
        });

    };
}
