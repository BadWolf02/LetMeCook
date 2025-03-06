package com.example.letmecook;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.letmecook.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editName;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editName = findViewById(R.id.edit_name);
        Button saveButton = findViewById(R.id.save_button);
        Button closeButton = findViewById(R.id.close_edit_profile);

        if (user != null) {
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            editName.setText(documentSnapshot.getString("username"));
                        }
                    });
        }

        saveButton.setOnClickListener(v -> saveUserProfile());
        closeButton.setOnClickListener(v -> finish());
    }

    private void saveUserProfile() {
        if (user != null){
            String updatedName = editName.getText().toString().trim();

            if (updatedName.isEmpty()){
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("username", updatedName);

            db.collection("users").document(user.getUid())
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(EditProfileActivity.this, "Profile updated!", Toast.LENGTH_SHORT).show();
                        finish(); // Close the activity after updating
                    })
                    .addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, "Update failed!", Toast.LENGTH_SHORT).show());
        }
    }

}

