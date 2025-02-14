package com.example.letmecook;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.example.letmecook.adapters.UsersInvitedAdapter;
import com.example.letmecook.db_tools.SearchDB;
import com.google.firebase.auth.FirebaseAuth;

public class TestActivity extends AppCompatActivity {
    // Declaring variables for each interactable field
    Button testButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        SearchDB searchDB = new SearchDB();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        UsersInvitedAdapter adapter = new UsersInvitedAdapter();

        // Find the fields
        testButton = findViewById(R.id.testButton);
        testButton.setOnClickListener(view -> {
            Toast.makeText(TestActivity.this, "Testing", Toast.LENGTH_SHORT).show();
        });

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}
