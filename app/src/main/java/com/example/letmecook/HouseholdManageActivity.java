package com.example.letmecook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.*;

import com.example.letmecook.db_tools.Household;
import com.example.letmecook.adapters.UsersInvitedAdapter;
import com.example.letmecook.db_tools.SearchDB;

public class HouseholdManageActivity extends AppCompatActivity {
    // Declaring variables for each interactable field
    TextView name;
    TextView id;
    EditText searchBar;
    Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_household);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        SearchDB searchDB = new SearchDB();
        Household household = new Household(HouseholdManageActivity.this);
        UsersInvitedAdapter adapter = new UsersInvitedAdapter();

        name = findViewById(R.id.name);
        id = findViewById(R.id.id);
        searchDB.getUserHouseholdName(mAuth.getCurrentUser().getUid(), householdName -> name.setText(householdName));
        searchDB.getUserHouseholdID(mAuth.getCurrentUser().getUid(), hid -> id.setText(hid));

        searchBar = findViewById(R.id.searchBar);
        searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(view ->
                household.inviteUserSingleHousehold(searchBar.getText().toString())
        );

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}
