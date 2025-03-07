package com.example.letmecook.db_tools;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Map;

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
        /*
        if (ingredients != null && !ingredients.isEmpty()) {
            for (String ingredient : ingredients) {
                // ChatGPT solution to searching through maps in Firestore
                recipeQuery = recipeQuery
                        .whereGreaterThanOrEqualTo("ingredients." + ingredient, "")
                        .whereLessThan("ingredients." + ingredient, "\uf8ff");
            }
        }
         */

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
                .addOnFailureListener(queryDocumentSnapshots -> listener.onDocumentArrayRetrieved(new ArrayList<>()));
    }

    public void getWhatCanICook(String uid, int page, OnDocumentArrayRetrievedListener listener) {
        getUserHouseholdDocument(uid, householdDocument -> {
            Map<String, Integer> inventoryMap = (Map<String, Integer>) householdDocument.get("inventory"); // map of inventory items
            List<String> inventoryItems = new ArrayList<>(inventoryMap.keySet()); // name of inventory items
            List<DocumentSnapshot> cookableRecipes = new ArrayList<>(); // return list
            getAllRecipeDocuments(allRecipes -> {
                if (allRecipes != null) {
                    // Loop through all recipes
                    for (DocumentSnapshot recipe : allRecipes) {
                        boolean canCook = true;
                        Map<String, Integer> ingredientsMap = (Map<String, Integer>) recipe.get("ingredients");
                        List<String> ingredients = new ArrayList<>(ingredientsMap.keySet());
                        // Loop through each recipe's ingredients
                        for (String ingredient : ingredients) {
                            // Check inventory for recipe ingredient
                            if (!inventoryItems.contains(ingredient)) {
                                canCook = false;
                                break;
                            }
                        }
                        // If all ingredients present in inventory, add
                        if (canCook) {
                            cookableRecipes.add(recipe);
                        }
                    }
                    // Pagination
                    int pageSize = 6;
                    int pageOffset = (page - 1) * pageSize;

                    if (cookableRecipes.isEmpty()) { // Check list is not empty
                        // Pagination, returns all recipes since no matches
                        int startAfter = Math.min(pageOffset + pageSize, allRecipes.size());
                        List<DocumentSnapshot> paginatedRecipes = allRecipes.subList(pageOffset, startAfter);
                        listener.onDocumentArrayRetrieved(paginatedRecipes);
                    } else if (pageOffset >= cookableRecipes.size()) { // Check page is not out of bounds
                        // return empty arraylist to avoid crashes
                        listener.onDocumentArrayRetrieved(new ArrayList<>());
                    } else {
                        // Pagination
                        int startAfter = Math.min(pageOffset + pageSize, cookableRecipes.size());
                        List<DocumentSnapshot> paginatedRecipes = cookableRecipes.subList(pageOffset, startAfter);
                        listener.onDocumentArrayRetrieved(paginatedRecipes);
                    }
                } else {
                    listener.onDocumentArrayRetrieved(new ArrayList<>());
                }
            });
        });
    }

    public void getRecipeDocumentByID(String recipeID, OnDocumentRetrievedListener listener) {
        db.collection("recipes")
                .document(recipeID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Recipe found, retrieve the first matching document
                        Log.d(TAG, "Recipe found by ID");
                        listener.onDocumentRetrieved(documentSnapshot);
                    } else {
                        Log.e(TAG, "Recipe not found");
                        listener.onDocumentRetrieved(null);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Firestore fetch failed: ", e));
    }

    public void getAllRecipeDocuments(OnDocumentArrayRetrievedListener listener) {
        db.collection("recipes")
                .orderBy("r_name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "Recipes");
                        listener.onDocumentArrayRetrieved(queryDocumentSnapshots.getDocuments());
                    } else {
                        Log.e(TAG, "No recipes not found");
                        listener.onDocumentArrayRetrieved(null);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Firestore fetch failed: ", e));
    }


    // Ingredients

    public void getIngredients(OnStringArrayRetrievedListener listener) {
        db.collection("ingredients")
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        List<String> ingredients = new ArrayList<>();
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            ingredients.add(document.getString("name"));
                        }
                        Log.d(TAG, "Ingredients found: " + ingredients);
                        listener.onStringArrayRetrieved(ingredients);

                    } else {
                        listener.onStringArrayRetrieved(new ArrayList<>());
                    }
                });
    }

    // Households

    public void getUserShoppingList(String uid, OnStringArrayRetrievedListener listener) {
        getUserHouseholdDocument(uid, householdDocument -> {
            if (householdDocument != null) {
                List<String> shoppingList = (List<String>) householdDocument.get("shopping-list");
                Log.d(TAG, "Shopping list: " + shoppingList);
                listener.onStringArrayRetrieved(shoppingList);
            } else {
                listener.onStringArrayRetrieved(new ArrayList<>());
            }
        });
    }

    public void getUserHouseholdDocument(String uid, OnDocumentRetrievedListener listener) {
        getUserHouseholdID(uid, hid -> {
            if (hid != null) {
                getHouseholdDocumentByID(hid, listener);
            } else {
                listener.onDocumentRetrieved(null);
            }
        });
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
                })
                .addOnFailureListener(e -> Log.e(TAG, "Firestore fetch failed: ", e));
    }

    // User

    public void getFavouriteRecipes(String uid, OnDocumentArrayRetrievedListener listener) {
        getUserDocumentByID(uid, userDocument -> {
           List<String> favouriteRecipesIDs = (List<String>) userDocument.get("favourite_recipes");
           if (favouriteRecipesIDs != null) {
               List<DocumentSnapshot> favouriteRecipes = new ArrayList<>();

               List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
               for (String recipeID : favouriteRecipesIDs) {
                   tasks.add(db.collection("recipes").document(recipeID).get());
               }
               Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
                   for (Object recipeDocument : results) {
                       favouriteRecipes.add((DocumentSnapshot) recipeDocument);
                   }
                   listener.onDocumentArrayRetrieved(favouriteRecipes);
               });
           } else {
               listener.onDocumentArrayRetrieved(new ArrayList<>());
           }
        });
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

//    // Get snapshot for household by householdID
//    public void getHouseholdDocumentByID(String hid, OnDocumentRetrievedListener listener) {
//        db.collection("households")
//                .document(hid)
//                .get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    if (documentSnapshot.exists()) {
//                        // Household found, retrieve the first matching document
//                        Log.d(TAG, "Household found");
//                        listener.onDocumentRetrieved(documentSnapshot);
//                    } else {
//                        Log.e(TAG, "Household not found");
//                        listener.onDocumentRetrieved(null);
//                    }
//                });
//    }

    public void updateHouseholdInventory(String householdID, Map<String, Object> updatedInventory, OnUpdateListener listener) {
        db.collection("households").document(householdID)
                .update("inventory", updatedInventory)
                .addOnSuccessListener(aVoid -> listener.onUpdate(true))
                .addOnFailureListener(e -> listener.onUpdate(false));
    }
    //TODO next: this isn't working, so maybe try with callback interface
    public interface IngredientsCallback{
        public void onIngredientsLoaded(ArrayList<Object> ingredients);
    }
    //TODO next: this isn't working, so maybe try with callback interface
    public void getIngredientsList(IngredientsCallback ingreedients_callback){

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


    public interface OnAllergensRetrievedListener {
        void onAllergensRetrieved(List<String> i_allergens);
    }
    public void getIngredientDocumentAllergens(String i_name, OnAllergensRetrievedListener listener) {
        db.collection("ingredients")
                .whereEqualTo("name", i_name)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
//                       Log.d(TAG, "not found");
//                       listener.onAllergensRetrieved(queryDocumentSnapshots.getDocuments().get(0));
//                       DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        if (document.contains("allergens")) {
                            List<String> i_allergens = (List<String>) document.get("allergens");
                            listener.onAllergensRetrieved(i_allergens);
                        } else {
                            Log.e(TAG, "User not found");
                            listener.onAllergensRetrieved(null);
                        }
                    }}).addOnFailureListener(e -> {
                    Log.e(TAG, "Error retrieving allergens", e);
                    listener.onAllergensRetrieved(null);
                });
    }

    public void getAllIngredients(OnStringArrayRetrievedListener listener) {
        db.collection("ingredients")
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<String> ingredientNames = new ArrayList<>();
                    queryDocumentSnapshots.forEach(doc -> ingredientNames.add(doc.getString("name")));
                    listener.onStringArrayRetrieved(ingredientNames);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch ingredients", e);
                    listener.onStringArrayRetrieved(new ArrayList<>());
                });
    }


    public interface OnUpdateListener {
        void onUpdate(boolean success);
    }
    public interface OnIngredientsFetchedListener {
        void onIngredientsFetched(Map<String, String> ingredients);
    }
}
