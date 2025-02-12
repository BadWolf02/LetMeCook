package com.example.letmecook;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.example.letmecook.tools.Authentication;
import com.example.letmecook.tools.SearchDB;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class TestActivity extends AppCompatActivity {
    // Declaring variables for each interactable field
    EditText testBar;
    Button testButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        SearchDB searchDB = new SearchDB();

        // Find the fields
        testBar = findViewById(R.id.testBar);
        testButton = findViewById(R.id.testButton);
        testButton.setOnClickListener(view -> {
            searchDB.getUserHouseholds(testBar.getText().toString());
        });
    }
}
