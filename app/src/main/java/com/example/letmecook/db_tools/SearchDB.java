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

    /**
     * Filters and retrieves recipes from the Firestore database based on the provided criteria.
     * This method supports filtering by recipe name, author, cuisine, and ingredients.
     * It also implements pagination to efficiently retrieve large datasets.
     * <p>
     * Filtering Behavior:
     * - If a parameter (name, author, cuisine, ingredients) is provided (not null and not empty),
     *   the query will be filtered to include only recipes that exactly match the given value.
     * - If a parameter is null or empty, it is ignored in the filtering process.
     * - For ingredients, if the list is not null and not empty, it will attempt to search
     *   for recipes that have each ingredient. (currently commented out)
     *
     * Pagination:
     * - The method retrieves results in pages of 6 recipes.
     * - The 'page' parameter specifies which page of results to retrieve (starting from 1).
     * - If the requested page is beyond the available data, an empty list is returned.
     * - If the first page doesn't require pagination(fewer than 6 results) it just returns the results
     *
     * Result Ordering:
     * - The results are always ordered alphabetically by the recipe name ("r_name") in ascending order.
     *
     * Error Handling:
     * - If no recipes match the filter criteria or if an error occurs during the database query,
     *   an empty list is returned via the listener.
     * - If the requested page has no results an empty list is returned.
     * - If the page number is out of bound for example trying to go to page 2 when only 1 page exists, an empty list is returned
     *
     * */ // Returns recipes based on name, cuisine and ingredients. Accepts null in absence of parameter
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

    /**
     * Retrieves a list of recipes that the user can cook based on their current inventory.
     * This method fetches the user's inventory, then compares it against the ingredients
     * of all available recipes to determine which recipes can be made. The results are
     * paginated and returned through a listener.
     *
     * @param uid      The unique identifier of the user. Used to retrieve the user's household document.
     * @param page     The page number of the results to retrieve (starting from 1). Used for pagination.
     * @param listener A listener that will be notified when the list of recipes is retrieved.
     *                 The listener's {@code onDocumentArrayRetrieved} method will be called with the
     *                 list of recipes (as {@code DocumentSnapshot} objects) that the user can cook.
     *                 If no recipes can be cooked or if an error occurs, an empty list will be passed to the listener.
     *
     *                 The logic behind returning all recipes if there are no cookable recipes is to ensure that the UI has something to display,
     *                 if the user just runs out of ingredients.
     *                 If the page number is greater than the number of available pages, an empty array will be passed to the listener.
     *                 If there is no recipes document, an empty array will be passed to the listener.
     *
     *
     *                 The recipes returned are paginated, with a page size of 6.
     *                 Each recipe is represented by a DocumentSnapshot that contains the recipe data.
     *                 The method fetches the inventory from the user's household document,
     *                 and recipes from a "recipes" collection.
     */
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

    /**
     * Retrieves a recipe document from the "recipes" collection in Firestore by its unique ID.
     *
     * This method queries the Firestore database for a specific recipe document using the provided recipe ID.
     * If a document with the matching ID is found, it is returned via the listener. If no matching document
     * is found or an error occurs during the database operation, the listener is notified accordingly.
     *
     * @param recipeID The unique ID of the recipe document to retrieve.
     * @param listener  A callback listener that will be notified when the document is retrieved or an error occurs.
     *                  - onDocumentRetrieved(DocumentSnapshot documentSnapshot): Called when the document is successfully
     *                    retrieved. The documentSnapshot will be null if no document was found.
     *                  - If a failure happens the listener will be notified with a null DocumentSnapshot.
     *
     * @throws IllegalArgumentException if recipeID is null or empty.
     */
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

    /**
     * Interface for listening to the result of retrieving an array of recipe documents.
     */
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

    /**
     * Retrieves the shopping list associated with a user's household.
     *
     * This method fetches the shopping list from the user's household document in the database.
     * It uses an asynchronous callback (OnStringArrayRetrievedListener) to deliver the list once it's retrieved.
     *
     * @param uid      The unique identifier (UID) of the user. This is used to find the user's household document.
     * @param listener The listener to be notified when the shopping list is retrieved or if an empty list is returned.
     *                 The listener's {@code onStringArrayRetrieved} method will be called with the list of shopping items
     *                 (as Strings) or an empty list if the household document or shopping list is not found.
     *                 Should implement the OnStringArrayRetrievedListener interface.
     * @throws NullPointerException if listener is null.
     */
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

    /**
     * Retrieves the household document associated with a given user ID (UID).
     * <p>
     * This method first fetches the household ID (HID) associated with the provided UID.
     * If a household ID is found, it then retrieves the corresponding household document.
     * If no household ID is found, it indicates that the user is not associated with any household.
     * </p>
     *
     * @param uid      The unique identifier of the user.
     * @param listener The listener to be notified when the document is retrieved or if an error occurs.
     *                 The onDocumentRetrieved() method will be called with:
     *                 <ul>
     *                     <li>The household document if it's found.</li>
     *                     <li>{@code null} if the user is not associated with a household or if any error occurs during retrieval.</li>
     *                 </ul>
     * @throws IllegalArgumentException if the uid or listener is null.
     */
    public void getUserHouseholdDocument(String uid, OnDocumentRetrievedListener listener) {
        getUserHouseholdID(uid, hid -> {
            if (hid != null) {
                getHouseholdDocumentByID(hid, listener);
            } else {
                listener.onDocumentRetrieved(null);
            }
        });
    }

    /**
     * This class likely interacts with a database or external data source to retrieve user information.
     */
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

    /**
     * Retrieves the household name associated with a given user ID.
     */
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

    /**
     * Retrieves the list of household IDs that a user has been invited to.
     *
     * This method fetches a user document from the database using the provided user ID (uid).
     * If the user document is found, it extracts the "invites" field, which is expected to be a list of strings
     * representing household IDs. It then passes this list to the provided listener.
     * If the user document is not found or if the "invites" field is not present, it calls the listener
     * with an empty list.
     *
     * @param uid      The unique identifier of the user whose invites are to be retrieved.
     * @param listener The listener to be notified when the list of invited household IDs is retrieved.
     *                 The listener's {@link OnStringArrayRetrievedListener#onStringArrayRetrieved(List)} method will be called
     *                 with the list of household IDs or an empty list if the user or invites were not found.
     * @throws IllegalArgumentException if the uid or listener is null.
     */
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

    /**
     * Retrieves the list of user IDs that have been invited to a specific household.
     */
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

    /**
     * Retrieves the list of member IDs associated with a given household ID.
     *
     * This method fetches the household document corresponding to the provided
     * household ID (`hid`) and extracts the list of member IDs from the "members" field.
     * If the household document is found, it invokes the listener's
     * `onStringArrayRetrieved` method with the list of member IDs. If the household is not found,
     * it invokes the listener with an empty list.
     *
     * @param hid      The unique identifier of the household.
     * @param listener An instance of `OnStringArrayRetrievedListener` to handle the
     *                 retrieved list of member IDs or an empty list if the household is not found.
     * @throws IllegalArgumentException if `hid` or `listener` is null.
     */
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

    /**
     * Class containing methods to interact with Firestore for household data.
     */ // Get snapshot for household by householdID
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

    /**
     * Retrieves a list of the user's favorite recipes based on their user ID.
     *
     * This method fetches the user's document from the Firestore database using the provided user ID (UID).
     * It then extracts the list of recipe IDs from the "favourite_recipes" field in the user's document.
     * For each recipe ID, it retrieves the corresponding recipe document from the "recipes" collection.
     * Finally, it assembles a list of these recipe documents and provides them to the listener.
     *
     * @param uid      The unique identifier of the user whose favorite recipes are to be retrieved.
     * @param listener The listener to be notified when the list of favorite recipes is retrieved or if an error occurs.
     *                 The listener's `onDocumentArrayRetrieved` method will be called with the list of
     *                 DocumentSnapshots representing the favorite recipes. If the user has no favorite recipes, it will be empty.
     *                 If the user document or the "favourite_recipes" field does not exist it will be empty.
     *
     *                 @see OnDocumentArrayRetrievedListener
     *
     * @throws IllegalArgumentException if the uid is null or empty.
     */
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


    /**
     * Interface definition for a callback to be invoked when a document is retrieved.
     */ // Get snapshot for user by uid
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

    /**
     * Class containing methods to interact with the Firestore database.
     */ // Get snapshot for user by uid
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
    /**
     * Retrieves the list of allergens associated with a specific ingredient from the Firestore database.
     *
     * This method queries the "ingredients" collection in Firestore to find a document matching the provided ingredient name.
     * If a matching document is found and it contains the "allergens" field, it retrieves the list of allergens
     * and invokes the listener's onAllergensRetrieved method with this list.
     * If the document does not contain the "allergens" field or if any error occurs during the database query,
     * it invokes the listener's onAllergensRetrieved method with null.
     *
     * @param i_name   The name of the ingredient to search for in the database.
     * @param listener An OnAllergensRetrievedListener instance that will be notified when the allergens
     *                 are retrieved (or when an error occurs).
     *                 The listener's `onAllergensRetrieved` method will be called with either:
     *                 - A List<String> containing the allergens of the ingredient, if found.
     *                 - null, if the ingredient is not found, the "allergens" field is missing, or an error occurred.
     *
     * @throws IllegalArgumentException if i_name is null or empty, or if listener is null
     */
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

    /**
     * Retrieves a list of all ingredient names from the "ingredients" collection in Firestore.
     * The ingredients are sorted alphabetically by their names in ascending order.
     *
     * <p>
     * This method performs an asynchronous query to Firestore. Upon successful retrieval,
     * it extracts the "name" field from each document and passes a list of these names
     * to the provided listener. If the retrieval fails, it logs the error and passes
     * an empty list to the listener.
     * </p>
     *
     * @param listener The listener to be notified when the ingredient names are retrieved or an error occurs.
     *                 The listener's OnStringArrayRetrieved method will be called with
     *                 either the list of ingredient names or an empty list in case of failure.
     *
     * @throws RuntimeException if the Firebase setup is not done correctly.
     * @see OnStringArrayRetrievedListener
     * @see FirebaseFirestore
     * @see Query
     */
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
