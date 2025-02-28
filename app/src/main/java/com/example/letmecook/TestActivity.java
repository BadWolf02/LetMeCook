package com.example.letmecook;
import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.letmecook.adapters.RecipeSearchAdapter;

import java.util.ArrayList;


public class TestActivity extends AppCompatActivity {
    private RecipeSearchAdapter adapter;
    private RecyclerView recyclerView;
    private EditText searchInput;
    private Button searchButton;
    private Button prevPageButton;
    private Button nextPageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        // Initialize UI components
        recyclerView = findViewById(R.id.recyclerView);
        searchInput = findViewById(R.id.testInput);
        searchButton = findViewById(R.id.testButton);
        prevPageButton = findViewById(R.id.prevButton);
        nextPageButton = findViewById(R.id.nextButton);


        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeSearchAdapter(this); // Initialize adapter
        recyclerView.setAdapter(adapter);
        adapter.applyFilters(null, null, null, null, () -> {});

        searchButton.setOnClickListener(view -> filterRecipes(null, searchInput.getText().toString(), null, null));

        prevPageButton.setOnClickListener(view -> changePage(-1));
        nextPageButton.setOnClickListener(view -> changePage(1));

        prevPageButton.setVisibility(View.GONE); // hide back button on page 1
        if (adapter.hasNextPage) {nextPageButton.setVisibility(View.VISIBLE);} // show next button if more data
    }

    // Call adapter's applyFilters method with user input
    private void filterRecipes(String name, String author, String cuisine, ArrayList<String> ingredients) {
        adapter.applyFilters(name, author, cuisine, ingredients, () -> {});
    }

    private void changePage(int direction) {
        adapter.changePage(direction, () -> {
            if (adapter.page == 1) {
                prevPageButton.setVisibility(View.GONE);
            } else {
                prevPageButton.setVisibility(View.VISIBLE);
            }

            if (!adapter.hasNextPage) {
                nextPageButton.setVisibility(View.GONE);
            } else {
                nextPageButton.setVisibility(View.VISIBLE);
            }
        });
    }
}
