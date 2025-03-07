package com.example.letmecook.db_tools;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Recipes {
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final SearchDB searchDB = new SearchDB();
    private final Context context;

    // Constructor to pass activity context
    public Recipes(Context context) {
        this.context = context;
    }

    /**
     * Adds a recipe to the user's list of favorite recipes in the database.
     */
    public void addRecipeToFavourites(String recipeID) {
        db.collection("users")
                .document(mAuth.getCurrentUser().getUid())
                .update("favourite_recipes", FieldValue.arrayUnion(recipeID));
    }

    /**
     * Adds a new review to a recipe in the database.
     *
     * This method takes a user ID, recipe ID, rating, and comment, and adds a new review to the specified recipe's
     * "reviews" array in the database. It also updates the recipe's average rating.
     *
     * The method performs the following actions:
     * 1. Retrieves the user's document based on the provided user ID to get the username.
     * 2. Retrieves the recipe's document based on the provided recipe ID.
     * 3. Checks if the user has already reviewed the recipe. If so, it displays a Toast message and returns early.
     * 4. If the user has not already reviewed the recipe, it creates a new review map containing the comment, rating, and username.
     * 5. Adds the new review to the "reviews" array of the recipe document using `FieldValue.arrayUnion`.
     * 6. Calculates the new average rating for the recipe, considering the newly added review.
     * 7. Rounds the average rating to the nearest 0.5.
     * 8. Updates the recipe document's "avgRating" field with the new average rating.
     *
     * @param uid       The unique ID of the user adding the review.
     * @param recipeID  The unique ID of the recipe being reviewed.
     * @param rating    The rating given by the user (e.g., 1 to 5).
     * @param comment   The comment provided by the user for the review.
     *
     * @throws NullPointerException if either user document or recipe document could not be retrieved from the db.
     * @throws ClassCastException if the review list or rating isn't the expected type.
     * @throws Exception if any other database error occurred.
     */
    public void addReview(String uid, String recipeID, int rating, String comment) {
        Map<Object, Object> newReview = new HashMap<>();
        // Get user doc to find user's username
        searchDB.getUserDocumentByID(uid, userDoc -> {
            // Get recipe doc to check if user has already commented
            searchDB.getRecipeDocumentByID(recipeID, recipeDoc -> {
                List<Map<Object, Object>> allReviews = (List<Map<Object, Object>>) recipeDoc.get("reviews");
                long totalRating = 0L;
                int numRatings = allReviews.size();
                for (Map<Object, Object> review : allReviews) {
                    totalRating = totalRating + (Long) review.get("rating");
                    if (Objects.equals(review.get("username"), userDoc.getString("username"))) {
                        Toast.makeText(context, "You have already commented on this recipe", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                // Add to reviews array
                newReview.put("comment", comment);
                newReview.put("rating", rating);
                newReview.put("username", userDoc.getString("username"));
                db.collection("recipes")
                        .document(recipeID)
                        .update("reviews", FieldValue.arrayUnion(newReview));

                // Update recipe's average rating
                totalRating = totalRating + rating;
                numRatings++;
                float averageRating = (float) totalRating / numRatings; // Get average rating
                averageRating = (float) (Math.round(averageRating * 2) / 2.0); // Round to nearest 0.5
                db.collection("recipes")
                        .document(recipeID)
                        .update("avgRating", averageRating);
            });
        });
    }

    /**
     * This method adds ingredients from a specified recipe to a user's shopping list,
     * while considering the user's current inventory and existing shopping list.
     *
     * @param recipeID The ID of the recipe from which to add ingredients.
     * @param uid      The unique ID of the user whose shopping list should be updated.
     *
     * <p>
     * The method performs the following steps:
     * <ol>
     *   <li>
     *     <b>Retrieves Recipe Ingredients:</b> Fetches the recipe document from the database
     *     using the provided `recipeID`. Extracts the list of ingredients from the recipe.
     *   </li>
     *   <li>
     *     <b>Retrieves User's Household Data:</b> Fetches the user's household document
     *     from the database using the provided `uid`. Extracts the current inventory
     *     and shopping list from the household document.
     *   </li>
     *   <li>
     *     <b>Compares Recipe Ingredients with Inventory:</b> Compares each ingredient
     *     in the recipe against the user's inventory. If an ingredient from the recipe
     *     is NOT found in the user's inventory, it's marked for addition to the shopping list.
     *      If the user does not have any inventory, then all the recipe ingredients are marked to be added.
     *   </li>
     *   <li>
     *     <b>Checks Against Existing Shopping List:</b>  Compares the ingredients marked for addition
     *     against the user's existing shopping list. If an ingredient is already present
     *     in the shopping list, it's removed from the list of ingredients to be added.
     *   </li>
     *   <li>
     *     <b>Updates Shopping List:</b> Adds the remaining ingredients (those not in the inventory and not
     *     already on the shopping list) to the user's shopping list in the database.
     *   </li>
     * </ol>
     * </p>
     * <p>
     * <b>Error Handling:</b>
     *   This method assumes that the `searchDB` object handles potential errors
     *   during database interactions (e.g., recipe or household document not found).
     *   If the user does not have any inventory, then the whole ingredients list will be added to the shopping list
     * </p>
     * <p>
     * <b>Assumptions: */
    public void addToShoppingList(String recipeID, String uid) {
        searchDB.getRecipeDocumentByID(recipeID, recipeDoc -> {
            Map<String, Object> ingredientMap = (Map<String, Object>) recipeDoc.get("ingredients");
            List<String> recipeIngredients = new ArrayList<>(ingredientMap.keySet()); // list of ingredients in the recipe
            List<String> ingredientsToAdd = new ArrayList<>(); // list of ingredients to be added to shopping list
            searchDB.getUserHouseholdDocument(uid, householdDoc -> {
                Map<String, Integer> inventory = (Map<String, Integer>) householdDoc.get("ingredients");
                // If true, then user has an inventory
                if (inventory != null && !inventory.isEmpty()) {
                    // Check if recipe ingredients are in household ingredients
                    for (String ingredient : recipeIngredients) {
                      if (!inventory.keySet().contains(ingredient)) {
                          ingredientsToAdd.add(ingredient);
                      }
                    }
                } else {
                    ingredientsToAdd.addAll(recipeIngredients);
                }
                List<String> householdShoppingList = (List<String>) householdDoc.get("shopping-list");
                // Removes any items that are already in the shopping list
                if (householdShoppingList != null && !householdShoppingList.isEmpty()) {
                    ingredientsToAdd.removeIf(householdShoppingList::contains);
                }
                // Adds new ingredients to shopping list
                if (!ingredientsToAdd.isEmpty()) {
                    for (String ingredient : ingredientsToAdd) {
                        householdDoc.getReference().update("shopping-list", FieldValue.arrayUnion(ingredient));
                    }
                }
            });
        });
    }
}
