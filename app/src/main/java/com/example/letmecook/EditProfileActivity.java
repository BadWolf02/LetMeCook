package com.example.letmecook;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.letmecook.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;


import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editName;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private ImageView profilePicture;
    private int selectedDrawable = R.drawable.ic_profile_picture_placeholder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editName = findViewById(R.id.edit_name);
        Button saveButton = findViewById(R.id.save_button);
        Button closeButton = findViewById(R.id.close_edit_profile);

        profilePicture = findViewById(R.id.profile_picture);
        Button changePictureButton = findViewById(R.id.change_picture_button);

        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        selectedDrawable = prefs.getInt("profile_picture", R.drawable.ic_profile_picture_placeholder);
        profilePicture.setImageResource(selectedDrawable);

        changePictureButton.setOnClickListener(v -> showProfilePicturePopup());

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

    private void showProfilePicturePopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View popupView = LayoutInflater.from(this).inflate(R.layout.profile_picture_selection, null);
        builder.setView(popupView);

        AlertDialog dialog = builder.create();
        dialog.show();

        ImageButton[] buttons = {
                popupView.findViewById(R.id.pfp1),
                popupView.findViewById(R.id.pfp2),
                popupView.findViewById(R.id.pfp3),
                popupView.findViewById(R.id.pfp4),
                popupView.findViewById(R.id.pfp5),
                popupView.findViewById(R.id.pfp6),
                popupView.findViewById(R.id.pfp7),
                popupView.findViewById(R.id.pfp8),
                popupView.findViewById(R.id.pfp9)
        };

        int[] drawableIds = {
                R.drawable.ic_profile_picture_placeholder,
                R.drawable.ic_profile_picture_placeholder_2,
                R.drawable.ic_profile_picture_placeholder_3,
                R.drawable.ic_profile_picture_placeholder_4,
                R.drawable.ic_profile_picture_placeholder_5,
                R.drawable.ic_profile_picture_placeholder_6,
                R.drawable.ic_profile_picture_placeholder_7,
                R.drawable.ic_profile_picture_placeholder_8,
                R.drawable.ic_profile_picture_placeholder_9
        };

        for (int i = 0; i < buttons.length; i++) {
            final int index = i;
            buttons[i].setOnClickListener(v -> {
                selectedDrawable = drawableIds[index];
                profilePicture.setImageResource(selectedDrawable);
            });
        }

        Button saveButton = popupView.findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = getSharedPreferences("UserProfile", MODE_PRIVATE).edit();
            editor.putInt("profile_picture", selectedDrawable);
            editor.apply();

            dialog.dismiss();
        });

        Button closeButton = popupView.findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> dialog.dismiss());
    }
}

