package com.example.letmecook;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.example.letmecook.tools.Household;

public class HouseholdManageActivity extends AppCompatActivity {
    // Declaring variables for each interactable field
    EditText householdInput;
    EditText searchBar;
    Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_household);

        Household household = new Household(HouseholdManageActivity.this);
        // Find the fields
        householdInput = findViewById(R.id.householdInput);
        searchBar = findViewById(R.id.searchBar);
        searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(view ->
                household.inviteUser(householdInput.getText().toString(),
                        searchBar.getText().toString())
        );
    }
}
