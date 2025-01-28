package com.example.letmecook;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.example.letmecook.tools.Firebase;

public class FirebaseAuthSignUpActivity extends AppCompatActivity {
    // Declaring variables for each interactable field
    EditText email;
    EditText password;
    Button signupButton;

    //@SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_auth_signup);
        // Find the fields
        email = findViewById(R.id.testemail);
        password = findViewById(R.id.testpassword);
        signupButton = findViewById(R.id.testbutton);
        signupButton.setOnClickListener(view -> {

            Firebase db = new Firebase(FirebaseAuthSignUpActivity.this);
            db.addUserAuth(email.getText().toString(), password.getText().toString()); // add user to database
        });
    }
}
