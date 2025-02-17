package com.example.letmecook.db_tools;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Objects;

public class SearchDB {
    FirebaseFirestore db = FirebaseFirestore.getInstance(); // initialise database

    // Constructor
    public SearchDB() {}

    // Methods

    // Callback to handle asynchronously retrieving an array of strings
    public interface OnStringArrayRetrievedListener {
        void onStringArrayRetrieved(ArrayList<String> foundArray);
    }

    // Callback to handle asynchronously retrieving an array of documents
    public interface OnDocumentArrayRetrievedListener {
        void onDocumentArrayRetrieved(ArrayList<DocumentSnapshot> foundArray);
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
                              ArrayList<String> ingredients,
                              OnDocumentArrayRetrievedListener listener) {
        Query recipeQuery = db.collection("recipes");

        // Checks for exact name match
        if (name != null && !name.isEmpty()) {
            recipeQuery = recipeQuery.whereEqualTo("r_name", name);
        }

        // Checks for exact author match
        if (name != null && !name.isEmpty()) {
            recipeQuery = recipeQuery.whereEqualTo("author", author);
        }

        // Checks for exact cuisine match
        if (cuisine != null && !cuisine.isEmpty()) {
            recipeQuery = recipeQuery.whereEqualTo("cuisine", cuisine);
        }

        // Checks for match of ingredients
        if (ingredients != null && !ingredients.isEmpty()) {
            for (String ingredient : ingredients) {
                recipeQuery = recipeQuery.whereArrayContains("ingredients", ingredient);
            }
        }

        recipeQuery
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "Recipe(s) found");
                        // Store all documents in a list
                        ArrayList<DocumentSnapshot> filteredRecipes = new ArrayList<>(queryDocumentSnapshots.getDocuments());
                        listener.onDocumentArrayRetrieved(filteredRecipes);
                    } else {
                        Log.e(TAG, "No recipes found");
                        listener.onDocumentArrayRetrieved(new ArrayList<DocumentSnapshot>());
                    }
                });
    }

    public void getRecipesByName(String name, OnDocumentArrayRetrievedListener listener) {
        db.collection("recipes")
                .whereEqualTo("r_name", name)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "Recipe(s) found");
                        ArrayList<DocumentSnapshot> documents = (ArrayList<DocumentSnapshot>) queryDocumentSnapshots.getDocuments();
                        listener.onDocumentArrayRetrieved(documents);
                    } else {
                        Log.e(TAG, "No recipes found");
                        listener.onDocumentArrayRetrieved(new ArrayList<DocumentSnapshot>());
                    }
                });
    }

    public void getRecipesByAuthor(String author, OnDocumentArrayRetrievedListener listener) {
        db.collection("recipes")
                .whereEqualTo("author", author)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "Recipe(s) found");
                        ArrayList<DocumentSnapshot> documents = (ArrayList<DocumentSnapshot>) queryDocumentSnapshots.getDocuments();
                        listener.onDocumentArrayRetrieved(documents);
                    } else {
                        Log.e(TAG, "No recipes found");
                        listener.onDocumentArrayRetrieved(new ArrayList<DocumentSnapshot>());
                    }
                });
    }

    public void getRecipesByIngredient(String ingredient, OnDocumentArrayRetrievedListener listener) {
        db.collection("recipes")
                .whereArrayContains("ingredients", ingredient)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "Recipe(s) found");
                        ArrayList<DocumentSnapshot> documents = (ArrayList<DocumentSnapshot>) queryDocumentSnapshots.getDocuments();
                        listener.onDocumentArrayRetrieved(documents);
                    } else {
                        Log.e(TAG, "No recipes found");
                        listener.onDocumentArrayRetrieved(new ArrayList<DocumentSnapshot>());
                    }
                });
    }

    public void getRecipesByCuisine(String cuisine, OnDocumentArrayRetrievedListener listener) {
        db.collection("recipes")
                .whereEqualTo("cuisine", cuisine)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "Recipe(s) found");
                        ArrayList<DocumentSnapshot> documents = (ArrayList<DocumentSnapshot>) queryDocumentSnapshots.getDocuments();
                        listener.onDocumentArrayRetrieved(documents);
                    } else {
                        Log.e(TAG, "No recipes found");
                        listener.onDocumentArrayRetrieved(new ArrayList<DocumentSnapshot>());
                    }
                });
    }

    public void getRecipeIngredients(String rid, OnStringArrayRetrievedListener listener) {
            getRecipeDocumentByID(rid, recipeDocument -> {
                if (recipeDocument != null) {
                    ArrayList<String> ingredients = (ArrayList<String>) recipeDocument.get("ingredients");
                    Log.d(TAG, "Ingredients: " + ingredients);
                    listener.onStringArrayRetrieved(ingredients);
                } else {
                    listener.onStringArrayRetrieved(new ArrayList<>());
                }
            });
    }

    public void getRecipeDocumentByID(String rid, OnDocumentRetrievedListener listener) {
        db.collection("recipes").
                whereEqualTo("recipeID", rid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        // Recipe found, retrieve the first matching document
                        Log.d(TAG, "Recipe found");
                        listener.onDocumentRetrieved(queryDocumentSnapshots.getDocuments().get(0));
                    } else {
                        Log.e(TAG, "Recipe not found");
                        listener.onDocumentRetrieved(null);
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

    public interface OnHouseholdNameRetrievedListener {
        void onHouseholdNameRetrieved(String householdName);
    }

    public void getUserHouseholdName(String uid, OnHouseholdNameRetrievedListener listener) {
        getUserHouseholdID(uid, hid -> {
            if (hid != null) {
                getHouseholdByID(hid, householdDocument -> {
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
        getHouseholdByID(hid, householdDocument -> {
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
        getHouseholdByID(hid, householdDocument -> {
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

    // User

    // Get snapshot for user by uid
    public void getUserDocumentByID(String uid, OnDocumentRetrievedListener listener) {
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
    public void getHouseholdByID(Object hid, OnDocumentRetrievedListener listener) {
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
