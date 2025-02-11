package com.example.letmecook;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.letmecook.tools.Authentication;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import kotlinx.coroutines.CompletableDeferred;

public class TestActivity extends AppCompatActivity {
    // Declaring variables for each interactable field
    EditText username;
    Button testButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        // Find the fields
        username = findViewById(R.id.testUsername);
        testButton = findViewById(R.id.testButton);
        testButton.setOnClickListener(view -> {
            Authentication db = new Authentication(TestActivity.this);
            db.usernameExists(username.getText().toString()).addOnSuccessListener(exists -> {
                if (exists) {
                    Toast.makeText(TestActivity.this, "Username already exists", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
