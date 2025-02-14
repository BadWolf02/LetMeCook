package com.example.letmecook;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.example.letmecook.adapters.HouseholdInvitesAdapter;
import com.example.letmecook.db_tools.Household;
import com.google.firebase.auth.FirebaseAuth;

public class ViewInviteActivity extends AppCompatActivity implements HouseholdInvitesAdapter.OnInviteActionListener  {
    // Declaring variables for each interactable field
    Household household = new Household(ViewInviteActivity.this);
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    HouseholdInvitesAdapter adapter = new HouseholdInvitesAdapter(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invites);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onAcceptInvite(String householdId) {
        household.acceptInvite(householdId, mAuth.getCurrentUser().getUid());
        Log.d(TAG, "Invite accepted");
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDenyInvite(String householdId) {
        household.denyInvite(householdId, mAuth.getCurrentUser().getUid());
        Log.d(TAG, "Invite denied");
        adapter.notifyDataSetChanged();
    }
}
