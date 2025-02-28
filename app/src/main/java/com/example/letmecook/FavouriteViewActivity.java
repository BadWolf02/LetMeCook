package com.example.letmecook;

import static android.content.ContentValues.TAG;
import static android.content.Intent.getIntent;

import static androidx.databinding.DataBindingUtil.setContentView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letmecook.adapters.FavouriteRecipeAdapter;
import com.example.letmecook.db_tools.SearchDB;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

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