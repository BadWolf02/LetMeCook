package com.example.letmecook;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class RecipeViewActivity extends AppCompatActivity {
    private TextView nameTextView, authorTextView, cuisineTextView, ingredientsTextView, stepsTextView;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_view);

        nameTextView = findViewById(R.id.recipeName);
        authorTextView = findViewById(R.id.recipeAuthor);
        cuisineTextView = findViewById(R.id.recipeCuisine);
        ingredientsTextView = findViewById(R.id.recipeIngredients);
        stepsTextView = findViewById(R.id.recipeSteps);

        // Collect data from intent when button is pressed
        String recipeID = getIntent().getStringExtra("recipeID");
        if (recipeID != null) {
            fetchRecipeData(recipeID);
        } else {
            Log.e(TAG, "Error loading recipe");
        }

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
    private void fetchRecipeData(String recipeID) {
        db.collection("recipes")
                .document(recipeID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                   if (documentSnapshot.exists()) {
                       // Pass UI data to TextViews
                       nameTextView.setText(documentSnapshot.getString("r_name"));
                       authorTextView.setText("By: " + documentSnapshot.getString("author"));
                       cuisineTextView.setText("Cuisine: " + documentSnapshot.getString("cuisine"));
                       ingredientsTextView.setText(formatList("Ingredients", (List<String>) documentSnapshot.get("ingredients")));
                       stepsTextView.setText(formatList("Steps", (List<String>) documentSnapshot.get("steps")));
                   }
                });
    }
}
