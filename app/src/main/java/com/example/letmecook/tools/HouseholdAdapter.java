package com.example.letmecook.tools;

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

import com.google.firebase.auth.*;

// https://developer.android.com/develop/ui/views/layout/recyclerview#java

public class HouseholdAdapter extends RecyclerView.Adapter<HouseholdAdapter.ViewHolder> {
    private final ArrayList<String> invitedUsers = new ArrayList<>();
    private final SearchDB searchDB = new SearchDB();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    /**
     * Initialize the dataset of the Adapter
     *
     * userHouseholds ArrayList<String> containing the data to populate views to be used
     * by RecyclerView
     */
    public HouseholdAdapter() {
        fetchHouseholds();
    }

    private void fetchHouseholds() {
        searchDB.getUserHouseholdID(mAuth.getCurrentUser().getUid(), hid -> {
            if (hid != null) {
                searchDB.getHouseholdInvites(hid, invited -> {
                    if (invited != null) {
                        Log.d(TAG, "Fetched invited users");
                        invitedUsers.clear();
                        invitedUsers.addAll(invited);
                        notifyDataSetChanged(); // Refresh RecyclerView after getting data
                    } else {
                        Log.e(TAG, "Invited list is null");
                    }
                });
            } else {
                Log.e(TAG, "Household ID is null");
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            textView = (TextView) view.findViewById(R.id.inviteText);
        }

        public void bind(String householdName) {
            textView.setText(householdName);
        }
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.text_row_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.bind(invitedUsers.get(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return invitedUsers.size();
    }
}
