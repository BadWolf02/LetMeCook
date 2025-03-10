package com.example.letmecook;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.example.letmecook.db_tools.Authentication;

public class SignUpActivity extends AppCompatActivity {
    // Declaring variables for each interactable field
    EditText username;
    EditText email;
    EditText password;
    Button signupButton;
    Button toLoginButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        // Find the fields
        username = findViewById(R.id.signupUsername);
        email = findViewById(R.id.signupEmail);
        password = findViewById(R.id.signupPassword);
        signupButton = findViewById(R.id.signUpButton);
        toLoginButton = findViewById(R.id.to_login);
        signupButton.setOnClickListener(view -> {
            Authentication db = new Authentication(SignUpActivity.this);
            // add user to database
            db.addUserAuth(
                    username.getText().toString(),
                    email.getText().toString(),
                    password.getText().toString()
            );
        });
        // Switch to Login Activity
        toLoginButton.setOnClickListener(view -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            SignUpActivity.this.finish();
        });
    }
}
