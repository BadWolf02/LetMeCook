package com.example.letmecook;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letmecook.adapters.ReviewAdapter;
import com.example.letmecook.db_tools.Recipes;
import com.example.letmecook.db_tools.SearchDB;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO deleting reviews ???

public class RecipeViewActivity extends AppCompatActivity {
    private TextView ratingTextView, nameTextView, authorTextView, cuisineTextView, ingredientsTextView, stepsTextView;

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final SearchDB searchDB = new SearchDB();
    private final Recipes recipes = new Recipes(this);

    private String recipeID;
    private ReviewAdapter reviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_view);

        ratingTextView = findViewById(R.id.rating);
        nameTextView = findViewById(R.id.recipeName);
        authorTextView = findViewById(R.id.recipeAuthor);
        cuisineTextView = findViewById(R.id.recipeCuisine);
        ingredientsTextView = findViewById(R.id.recipeIngredients);
        stepsTextView = findViewById(R.id.recipeSteps);

        EditText reviewBox = findViewById(R.id.reviewBox);
        RatingBar ratingBar = findViewById(R.id.ratingBar);
        Button reviewButton = findViewById(R.id.reviewButton);

        // Collect data from intent when button is pressed
        recipeID = getIntent().getStringExtra("recipeID");
        if (recipeID != null) {
            fetchRecipeData(recipeID);
        } else {
            Log.e(TAG, "Error loading recipe");
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter(recipeID); // Initialize adapter
        recyclerView.setAdapter(reviewAdapter);

        Button favouriteButton = findViewById(R.id.favouriteButton);
        favouriteButton.setOnClickListener(view -> recipes.addRecipeToFavourites(recipeID));

        Button addToShoppingList = findViewById(R.id.addToShoppingList);
        addToShoppingList.setOnClickListener(view -> recipes.addToShoppingList(recipeID, mAuth.getCurrentUser().getUid()));

        reviewButton.setOnClickListener(view -> addReview(recipeID, (int) ratingBar.getRating(), reviewBox.getText().toString()));

    }

    // Helper method to format list data
    private String formatIngredients(Map<String, Map<String, String>> items) {
        List<String> ingredientList = new ArrayList<>(items.keySet());
        StringBuilder bulletPoints = new StringBuilder("Ingredients" + ":\n");
        for (int i = 0; i < items.size(); i++) {
            String ingredient = ingredientList.get(i);
            Map<String, String> amountMap = items.get(ingredient);
            String amount = amountMap.get("amount");
            String unit = amountMap.get("amount_type");
            bulletPoints
                    .append(i + 1) // number
                    .append(".) ")
                    .append(amount + " " + unit + " of " + ingredient) // text
                    .append("\n");
        }
        return bulletPoints.toString();
    }

    private String formatSteps(Map<String, String> items) {
        StringBuilder bulletPoints = new StringBuilder("Steps" + ":\n");
        for (int i = 0; i < items.size(); i++) {
            bulletPoints
                    .append(i + 1) // number
                    .append(".) ")
                    .append(items.get(String.valueOf(i+1))) // text
                    .append("\n");
        }
        return bulletPoints.toString();
    }

    // Method to store recipe data in TextViews
    @SuppressLint("SetTextI18n")
    private void fetchRecipeData(String recipeID) {
        searchDB.getRecipeDocumentByID(recipeID, documentSnapshot -> {
            // Pass UI data to TextViews
            ratingTextView.setText("Rating: " + documentSnapshot.getDouble("avgRating").toString() + "/5");
            nameTextView.setText(documentSnapshot.getString("r_name"));
            authorTextView.setText("By: " + documentSnapshot.getString("author"));
            cuisineTextView.setText("Cuisine: " + documentSnapshot.getString("cuisine"));
            ingredientsTextView.setText(formatIngredients((Map<String, Map<String, String>>) documentSnapshot.get("ingredients")));
            stepsTextView.setText(formatSteps((Map<String, String>) documentSnapshot.get("steps")));
        });
    }

    // TODO fix bug where, it doesn't refresh when leaving review
    private void addReview(String recipeID, int rating, String comment) {
        recipes.addReview(mAuth.getCurrentUser().getUid(), recipeID, rating, comment);
        reviewAdapter.notifyDataSetChanged();
    }
}
