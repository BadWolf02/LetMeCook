package com.example.letmecook.adapters;

import static android.content.ContentValues.TAG;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letmecook.R;

import java.util.ArrayList;

import com.example.letmecook.db_tools.SearchDB;
import com.google.firebase.auth.*;

// https://developer.android.com/develop/ui/views/layout/recyclerview#java

public class HouseholdInvitesAdapter extends RecyclerView.Adapter<HouseholdInvitesAdapter.ViewHolder> {
    private final ArrayList<String> myInvites = new ArrayList<>();
    private final SearchDB searchDB = new SearchDB();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final OnInviteActionListener inviteActionListener;

    public interface OnInviteActionListener {
        void onAcceptInvite(String householdId);
        void onDenyInvite(String householdId);
    }

    /**
     * Initialize the dataset of the Adapter
     * myInvites ArrayList<String> containing the data to populate views to be used
     * by RecyclerView
     */
    public HouseholdInvitesAdapter(OnInviteActionListener listener) {
        this.inviteActionListener = listener;
        fetchHouseholds();
    }

    private void fetchHouseholds() {
        searchDB.getUserInvites(mAuth.getCurrentUser().getUid(), invites -> {
            if (invites != null) {
                Log.d(TAG, "Fetched user's invites");
                this.myInvites.clear();
                this.myInvites.addAll(invites); // Store invites
                notifyDataSetChanged(); // Refresh RecyclerView after getting data
            } else {
                Log.e(TAG, "Invite list is null");
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final TextView acceptButton;
        private final TextView denyButton;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            textView = view.findViewById(R.id.rowText);
            acceptButton = view.findViewById(R.id.acceptInviteButton);
            denyButton = view.findViewById(R.id.denyInviteButton);
        }

        public void bind(String householdID, OnInviteActionListener listener) {
            textView.setText(householdID);

            acceptButton.setOnClickListener(v -> {
                Log.d(TAG, "Accept clicked for: " + householdID);
                listener.onAcceptInvite(householdID);
            });

            denyButton.setOnClickListener(v -> {
                Log.d(TAG, "Deny clicked for: " + householdID);
                listener.onDenyInvite(householdID);
            });
        }
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.text_row_button, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.bind(myInvites.get(position), inviteActionListener);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return myInvites.size();
    }
}
