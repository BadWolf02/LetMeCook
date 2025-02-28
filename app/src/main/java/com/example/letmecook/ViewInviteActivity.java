package com.example.letmecook;

import static android.content.ContentValues.TAG;

import static androidx.core.content.ContentProviderCompat.requireContext;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.letmecook.adapters.HouseholdInvitesAdapter;
import com.example.letmecook.db_tools.Household;
import com.google.firebase.auth.FirebaseAuth;

public class ViewInviteActivity extends AppCompatActivity implements HouseholdInvitesAdapter.OnInviteActionListener  {
    // Declaring variables for each interactable field
    Household household = new Household(ViewInviteActivity.this);
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    HouseholdInvitesAdapter adapter = new HouseholdInvitesAdapter(this);
    LinearLayout noInviteLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invites);

        noInviteLayout = findViewById(R.id.no_invites_layout);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        if (adapter.myInvites.isEmpty()) {
            displayNoInvites();
        }
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

    public void displayNoInvites() {
        noInviteLayout.removeAllViews();
        Log.d(TAG, "No invites to display");
        TextView noInviteView = new TextView(this);
        noInviteView.setText("No invites to display");
        noInviteView.setTextSize(14);
        noInviteView.setTextColor(Color.BLACK);
        noInviteLayout.addView(noInviteView);
    }
}
