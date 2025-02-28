package com.example.letmecook.db_tools;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SearchDB {
    FirebaseFirestore db = FirebaseFirestore.getInstance(); // initialise database

    // Constructor
    public SearchDB() {}

    // Methods

    // Callback to handle asynchronously retrieving an array of strings
    public interface OnStringArrayRetrievedListener {
        void onStringArrayRetrieved(List<String> foundArray);
    }

    // Callback to handle asynchronously retrieving an array of documents
    public interface OnDocumentArrayRetrievedListener {
        void onDocumentArrayRetrieved(List<DocumentSnapshot> foundArray);
    }

    // Callback to handle asynchronously retrieving a string
    public interface OnStringRetrievedListener {
        void onStringRetrieved(String foundString);
    }

    // Callback to handle asynchronously retrieving a document
    public interface OnDocumentRetrievedListener {
        void onDocumentRetrieved(DocumentSnapshot document);
    }

    // Recipes

    // Returns recipes based on name, cuisine and ingredients. Accepts null in absence of parameter
    // TODO make recipes case insensitive / standardized to capitals
    public void filterRecipes(String name,
                              String author,
                              String cuisine,
                              List<String> ingredients,
                              int page,
                              OnDocumentArrayRetrievedListener listener) {

        Query recipeQuery = db.collection("recipes");
        int pageOffset = ((page - 1) * 6); // create page offset for pagination. 6 items per page

        // Checks for exact name match
        if (name != null && !name.isEmpty()) {recipeQuery = recipeQuery.whereEqualTo("r_name", name);}

        // Checks for exact author match
        if (author != null && !author.isEmpty()) {recipeQuery = recipeQuery.whereEqualTo("author", author);}

        // Checks for exact cuisine match
        if (cuisine != null && !cuisine.isEmpty()) {recipeQuery = recipeQuery.whereEqualTo("cuisine", cuisine);}

        // Checks for match of ingredients
        if (ingredients != null && !ingredients.isEmpty()) {
            for (String ingredient : ingredients) {
                recipeQuery = recipeQuery.whereArrayContains("ingredients", ingredient);
            }
        }

        // https://firebase.google.com/docs/firestore/query-data/query-cursors
        Query finalRecipeQuery = recipeQuery;
        finalRecipeQuery
                .orderBy("r_name", Query.Direction.ASCENDING)
                .limit(6L * page) // collect everything up to the page requested
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        List<DocumentSnapshot> results = queryDocumentSnapshots.getDocuments();
                        Log.d(TAG, "Total recipes found: " + results.size());
                        // Use pagination to only retrieve given page of results
                        if (pageOffset >= results.size()) {
                            Log.e(TAG, "Page offset out of bounds");
                            listener.onDocumentArrayRetrieved(new ArrayList<>());
                            return;
                        }

                        if (results.size() >= 6) {
                            DocumentSnapshot lastDocument = results.get(pageOffset);

                            // Fetch only 6 results starting after the correct document
                            finalRecipeQuery
                                    .orderBy("r_name", Query.Direction.ASCENDING)
                                    .startAfter(lastDocument)
                                    .limit(6)
                                    .get().addOnSuccessListener(newQuerySnapshots -> {
                                        List<DocumentSnapshot> filteredRecipes = new ArrayList<>(newQuerySnapshots.getDocuments());
                                        Log.d(TAG, "Recipes retrieved for this page: " + filteredRecipes.size());
                                        listener.onDocumentArrayRetrieved(filteredRecipes);
                                    });
                        } else {
                            Log.d(TAG, "First page too small. No pagination");
                            listener.onDocumentArrayRetrieved(results);
                        }
                    } else {
                        Log.e(TAG, "No recipes found");
                        listener.onDocumentArrayRetrieved(new ArrayList<>());
                    }
                })
                .addOnFailureListener(queryDocumentSnapshots -> {listener.onDocumentArrayRetrieved(new ArrayList<>());});
    }


    // Ingredients

    public void getIngredients(OnStringArrayRetrievedListener listener) {
        db.collection("ingredients")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        List<String> ingredients = new ArrayList<>();
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            ingredients.add((String) document.getId());
                        }
                        Log.d(TAG, "Ingredients found: " + ingredients);
                        listener.onStringArrayRetrieved(ingredients);

                    } else {
                        listener.onStringArrayRetrieved(new ArrayList<>());
                    }
                });
    }

    // Households

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
                List<String> invites = (List<String>) userDocument.get("invites");
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
                List<String> invited = (List<String>) householdDocument.get("invited");
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
                List<String> members = (List<String>) householdDocument.get("members");
                Log.d(TAG, "Members: " + members);
                listener.onStringArrayRetrieved(members);
            } else {
                Log.e(TAG, "Household not found");
                listener.onStringArrayRetrieved(new ArrayList<>());
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

    // User

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
}
