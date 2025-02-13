package com.example.letmecook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Button;

import com.example.letmecook.tools.Household;
import com.example.letmecook.tools.HouseholdAdapter;

public class ViewInviteActivity extends AppCompatActivity {
    // Declaring variables for each interactable field
    Button acceptInviteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invites);

        Household household = new Household(ViewInviteActivity.this);
        HouseholdAdapter adapter = new HouseholdAdapter();
        // Find the fields
        // acceptInviteButton = findViewById(R.id.acceptInviteButton);
        // acceptInviteButton.setOnClickListener(view -> );

        // TODO Adapt HouseholdAdapter to be able to display invites for user.
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}
