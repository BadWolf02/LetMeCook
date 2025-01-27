package com.example.letmecook;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.example.letmecook.tools.Firebase;

public class SignUpActivity extends AppCompatActivity {
    // Declaring variables for each interactable field
    EditText username;
    EditText password;
    Button signupButton;
    Button toLoginButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        // Find the fields
        username = findViewById(R.id.signupUsername);
        password = findViewById(R.id.signupPassword);
        signupButton = findViewById(R.id.signUpButton);
        toLoginButton = findViewById(R.id.to_login);
        signupButton.setOnClickListener(view -> {

            Firebase db = new Firebase(SignUpActivity.this);
            boolean signedUp = db.createUser(username.getText().toString(), password.getText().toString()); // add user to database
            // Proceed to Login after successful sign up
            if (signedUp) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                SignUpActivity.this.finish();
            }
        });
        // Switch to Login Activity
        toLoginButton.setOnClickListener(view -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            SignUpActivity.this.finish();
        });
    }
}
