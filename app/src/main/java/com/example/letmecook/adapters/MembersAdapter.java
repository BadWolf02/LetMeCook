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

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.ViewHolder> {
    private final ArrayList<String> membersList = new ArrayList<>();
    private final SearchDB searchDB = new SearchDB();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    /**
     * Initialize the dataset of the Adapter
     *
     * userHouseholds ArrayList<String> containing the data to populate views to be used
     * by RecyclerView
     */
    public MembersAdapter() {
        fetchMembers();
    }

    private void fetchMembers() {
        // Get the user's household
        searchDB.getUserHouseholdID(mAuth.getCurrentUser().getUid(), hid -> {
            if (hid != null) {
                // Get invites in the household
                searchDB.getHouseholdMembers(hid, members -> {
                    if (members != null) {
                        Log.d(TAG, "Fetched members");
                        membersList.clear();
                        membersList.addAll(members); // Store invited users
                        notifyDataSetChanged(); // Refresh RecyclerView after getting data
                    } else {
                        Log.e(TAG, "Member list is null");
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
            textView = view.findViewById(R.id.rowText);
        }

        public void bind(String username) {
            textView.setText(username);
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
        viewHolder.bind(membersList.get(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return membersList.size();
    }
}
