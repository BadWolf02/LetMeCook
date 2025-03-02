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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WebScrapingActivity extends AppCompatActivity {

    private EditText urlInput;
    private Button scrapeButton;
    private ListView recipesListView;
    private List<String> recipeTitles;
    private List<String> recipeDetails;

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

        scrapeButton.setOnClickListener(v -> scrapeRecipe());

    }
    private void scrapeRecipe(){
        String url = urlInput.getText().toString().trim();
        if(TextUtils.isEmpty(url)){
            Toast.makeText(this, "Please enter a URL", Toast.LENGTH_SHORT).show();
        }
        new Thread(() -> {
            try {
                Document document = Jsoup.connect(url).get();
                Element titleElement = document.selectFirst("h1");
                Element ingredientsElement = document.selectFirst(".ingredients-list");
                Element instructionsElement = document.selectFirst(".method-steps");

                if(titleElement!=null && ingredientsElement!=null && instructionsElement!=null){
                    String title = titleElement.text();
                    String ingredients = ingredientsElement.text();
                    String instructions = instructionsElement.text();

                    runOnUiThread(() -> {
                        recipeTitles.add(title);
                        recipeDetails.add("Ingredients:\n" + ingredients + "\n\n Instructions:\n" + instructions);
                        updateListView();
                    });
                }else {
                    runOnUiThread(() -> Toast.makeText(this, "Failed to extract recipe", Toast.LENGTH_LONG).show());
                }

            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, "Error fetching recipe", Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }
        }).start();
    }

    private void updateListView(){
        RecipeAdapter adapter = new RecipeAdapter(this, recipeTitles, recipeDetails);
        recipesListView.setAdapter(adapter);
    }
}