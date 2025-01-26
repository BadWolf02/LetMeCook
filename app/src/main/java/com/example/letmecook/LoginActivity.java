package com.example.letmecook;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.letmecook.tools.Firebase;

public class LoginActivity extends AppCompatActivity {
    // Declaring variables for each interactable field
    EditText username;
    EditText password;
    Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Find the fields
        username = findViewById(R.id.loginUsername);
        password = findViewById(R.id.loginPassword);
        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Firebase db = new Firebase(LoginActivity.this);
                db.loginUser(username.getText().toString(), password.getText().toString()); // log user in
            }
        });
    }
}
