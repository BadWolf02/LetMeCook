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

public class Recipes {
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final SearchDB searchDB = new SearchDB();
    private final Context context;

    // Constructor to pass activity context
    public Recipes(Context context) {
        this.context = context;
    }

    public void addRecipeToFavourites(String recipeID) {
        db.collection("users")
                .document(mAuth.getCurrentUser().getUid())
                .update("favourite_recipes", FieldValue.arrayUnion(recipeID));
    }

    public void addReview(String uid, String recipeID, int rating, String comment) {
        Map<Object, Object> newReview = new HashMap<>();
        // Get user doc to find user's username
        searchDB.getUserDocumentByID(uid, userDoc -> {
            // Get recipe doc to check if user has already commented
            searchDB.getRecipeDocumentByID(recipeID, recipeDoc -> {
                List<Map<Object, Object>> allReviews = (List<Map<Object, Object>>) recipeDoc.get("reviews");
                if (allReviews != null) {
                    for (Map<Object, Object> review : allReviews) {
                        if (review.get("username").equals(userDoc.getString("username"))) {
                            Toast.makeText(context, "You have already commented on this recipe", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }

                // Add to reviews array
                newReview.put("comment", comment);
                newReview.put("rating", rating);
                newReview.put("username", userDoc.getString("username"));
                db.collection("recipes")
                        .document(recipeID)
                        .update("reviews", FieldValue.arrayUnion(newReview));
            });
        });
    }
}
