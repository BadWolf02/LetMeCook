package com.example.letmecook;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.text.Editable;
import android.text.TextWatcher;
// import androidx.appcompat.v

public class AddRecipeActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_recipe);

        // View set_recipe_btn_a = findViewById(R.id.set_recipe_name_btn_a);
        // TextView r_name_field_text = findViewById(R.id.recipe_name_text_a);
        // set_recipe_btn_a.setOnClickListener(v -> { String recipe_name_text_a = r_name_field_text.getText().toString();}); //TODO specify function. can I get it to pop up as a fragment?_


        // recipe name
        EditText edit_r_name = findViewById(R.id.recipe_name_text_a);
        edit_r_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                edit_r_name.setText(edit_r_name.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // adding steps

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}