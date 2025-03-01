package com.example.letmecook;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letmecook.adapters.ReviewAdapter;
import com.example.letmecook.db_tools.SearchDB;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class RecipeViewActivity extends AppCompatActivity {
    private TextView nameTextView, authorTextView, cuisineTextView, ingredientsTextView, stepsTextView;

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final SearchDB searchDB = new SearchDB();

    private String recipeID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_view);

        nameTextView = findViewById(R.id.recipeName);
        authorTextView = findViewById(R.id.recipeAuthor);
        cuisineTextView = findViewById(R.id.recipeCuisine);
        ingredientsTextView = findViewById(R.id.recipeIngredients);
        stepsTextView = findViewById(R.id.recipeSteps);

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
        ReviewAdapter reviewAdapter = new ReviewAdapter(recipeID); // Initialize adapter
        recyclerView.setAdapter(reviewAdapter);

        Button favouriteButton = findViewById(R.id.favouriteButton);
        favouriteButton.setOnClickListener(view -> addRecipeToFavourites(recipeID));

        // reviewButton.setOnClickListener(view -> ???);

    }

    // Helper method to format list data
    private String formatList(String title, List<String> items) {
        StringBuilder bulletPoints = new StringBuilder(title + ":\n");
        for (int i = 0; i < items.size(); i++) {
            bulletPoints
                    .append(i + 1) // number
                    .append(".) ")
                    .append(items.get(i)) // text
                    .append("\n");
        }
        return bulletPoints.toString();
    }

    // Method to store recipe data in TextViews
    @SuppressLint("SetTextI18n")
    private void fetchRecipeData(String recipeID) {
        searchDB.getRecipeDocumentByID(recipeID, documentSnapshot -> {
            // Pass UI data to TextViews
            nameTextView.setText(documentSnapshot.getString("r_name"));
            authorTextView.setText("By: " + documentSnapshot.getString("author"));
            cuisineTextView.setText("Cuisine: " + documentSnapshot.getString("cuisine"));
            ingredientsTextView.setText(formatList("Ingredients", (List<String>) documentSnapshot.get("ingredients")));
            stepsTextView.setText(formatList("Steps", (List<String>) documentSnapshot.get("steps")));
        });
    }

    private void addRecipeToFavourites(String recipeID) {
        db.collection("users")
                .document(mAuth.getCurrentUser().getUid())
                .update("favourite_recipes", FieldValue.arrayUnion(recipeID));
    }
}
