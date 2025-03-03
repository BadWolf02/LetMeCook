package com.example.letmecook;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.letmecook.adapters.RecipeAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebScrapingActivity extends AppCompatActivity {

    private EditText urlInput;
    private Button scrapeButton;
    private ListView recipesListView;
    private List<String> recipeTitles;
    private List<String> recipeDetails;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_web_scraping);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.url_input), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        urlInput = findViewById(R.id.url_input);
        scrapeButton = findViewById(R.id.scrape_button);
        recipesListView = findViewById(R.id.recipes_list);

        recipeTitles = new ArrayList<>();
        recipeDetails = new ArrayList<>();

        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        scrapeButton.setOnClickListener(v -> scrapeRecipe());
    }

    private void scrapeRecipe() {
        String url = urlInput.getText().toString().trim();
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(this, "Please enter a URL", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                Document document = Jsoup.connect(url).get();
                Element titleElement = document.selectFirst("h1");
                Element ingredientsElement = document.selectFirst(".ingredients-list");
                Element instructionsElement = document.selectFirst(".method-steps");
                Element nutritionElement = document.selectFirst(".nutrition-list");

                if (titleElement != null && ingredientsElement != null && instructionsElement != null && nutritionElement != null) {
                    String title = titleElement.text();
                    String ingredients = ingredientsElement.text();
                    String instructions = instructionsElement.text();
                    String nutritional_info = nutritionElement.text();

                    runOnUiThread(() -> {
                        recipeTitles.add(title);
                        recipeDetails.add("Ingredients:\n" + ingredients + "\n\n Instructions:\n" + instructions
                        + "\n\n Nutritional Information: \n" + nutritional_info);
                        updateListView();
                        saveRecipeToFirestore(title, ingredients, instructions, nutritional_info); // Save to Firestore
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Failed to extract recipe", Toast.LENGTH_LONG).show());
                }

            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, "Error fetching recipe", Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }
        }).start();
    }

    private void saveRecipeToFirestore(String title, String ingredients, String instructions, String nutritional_info) {
        Map<String, Object> recipeData = new HashMap<>();
        recipeData.put("r_name", title);
        recipeData.put("ingredients", ingredients);
        recipeData.put("Nutritional Information", nutritional_info);
        recipeData.put("steps", instructions);
        recipeData.put("cuisine", "Unknown"); // Default value for now

        db.collection("recipes")
                .add(recipeData)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(this, "Recipe saved!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error saving recipe", Toast.LENGTH_SHORT).show());
    }

    private void updateListView() {
        RecipeAdapter adapter = new RecipeAdapter(this, recipeTitles, recipeDetails);
        recipesListView.setAdapter(adapter);
    }
}
