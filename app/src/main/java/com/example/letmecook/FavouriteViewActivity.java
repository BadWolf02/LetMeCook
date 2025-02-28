package com.example.letmecook;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letmecook.adapters.FavouriteRecipeAdapter;
import com.example.letmecook.db_tools.SearchDB;
import com.google.firebase.auth.FirebaseAuth;


public class FavouriteViewActivity extends AppCompatActivity {

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final SearchDB searchDB = new SearchDB();

    // private Button prevPageButton, nextPageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite_view);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        // prevPageButton = findViewById(R.id.prevButton);
        // nextPageButton = findViewById(R.id.nextButton);

        // Set up RecyclerView for recipes
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        FavouriteRecipeAdapter recipeAdapter = new FavouriteRecipeAdapter(mAuth.getCurrentUser().getUid(), this); // Initialize adapter
        recyclerView.setAdapter(recipeAdapter);

    }

}