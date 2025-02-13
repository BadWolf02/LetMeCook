package com.example.letmecook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.example.letmecook.tools.Household;
import com.example.letmecook.tools.HouseholdAdapter;

public class HouseholdManageActivity extends AppCompatActivity {
    // Declaring variables for each interactable field
    EditText searchBar;
    Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_household);

        Household household = new Household(HouseholdManageActivity.this);
        HouseholdAdapter adapter = new HouseholdAdapter();
        // Find the fields
        searchBar = findViewById(R.id.searchBar);
        searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(view ->
                household.inviteUserSingleHousehold(searchBar.getText().toString())
        );

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}
