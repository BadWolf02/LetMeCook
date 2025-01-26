package com.example.letmecook;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.letmecook.tools.Firebase;

public class SignUpActivity extends AppCompatActivity {
    // Declaring variables for each interactable field
    EditText username;
    EditText password;
    Button signupButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        // Find the fields
        username = findViewById(R.id.signupUsername);
        password = findViewById(R.id.signupPassword);
        signupButton = findViewById(R.id.signUpButton);
        signupButton.setOnClickListener(view -> {

            Firebase db = new Firebase(SignUpActivity.this);
            boolean signedUp = db.createUser(username.getText().toString(), password.getText().toString()); // add user to database
            if (signedUp) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}
