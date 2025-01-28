package com.example.letmecook;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.example.letmecook.tools.Firebase;

public class LoginActivity extends AppCompatActivity {
    // Declaring variables for each interactable field
    EditText email;
    EditText password;
    Button loginButton;
    Button toSignUpButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Find the fields
        email = findViewById(R.id.loginEmail);
        password = findViewById(R.id.loginPassword);
        loginButton = findViewById(R.id.loginButton);
        toSignUpButton = findViewById(R.id.to_signup);
        loginButton.setOnClickListener(view -> {

            Firebase db = new Firebase(LoginActivity.this);
            db.loginUserAuth(email.getText().toString(), password.getText().toString()); // log user in
            //LoginActivity.this.finish(); // close activity
        });

        toSignUpButton.setOnClickListener(view -> {
            // Navigate to SignUpActivity
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
            LoginActivity.this.finish();
        });
    }
}
