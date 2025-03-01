package com.example.letmecook.adapters;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letmecook.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.letmecook.db_tools.SearchDB;
import com.google.firebase.firestore.DocumentSnapshot;

// https://developer.android.com/develop/ui/views/layout/recyclerview#java

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
    private final List<Map> reviewList = new ArrayList<>();
    private final String recipeID;
    private DocumentSnapshot recipeDoc;
    private final SearchDB searchDB = new SearchDB();

    /**
     * Initialize the dataset of the Adapter
     * reviewList List<Map> containing the data to populate views to be used
     * by RecyclerView
     */
    public ReviewAdapter(String recipeID) {
        this.recipeID = recipeID;
        fetchReviews();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchReviews() {
        reviewList.clear();
        if (recipeID != null && !recipeID.isEmpty()) {
            searchDB.getRecipeDocumentByID(recipeID, documentSnapshot -> {
                recipeDoc = documentSnapshot;
                if (recipeDoc.get("reviews") != null) {
                    reviewList.addAll((List<Map>) recipeDoc.get("reviews"));
                } else {
                    Log.e(TAG, "No reviews found");
                }
                Log.d(TAG, "Review List Content: " + reviewList); // Logs the content of reviewList
                notifyDataSetChanged();
            });
        } else {
            Log.e(TAG, "No recipeID found");
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView ratingView;
        private final TextView usernameView;
        private final TextView commentView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            ratingView = view.findViewById(R.id.rating);
            usernameView = view.findViewById(R.id.username);
            commentView = view.findViewById(R.id.comment);
        }

        @SuppressLint("SetTextI18n")
        public void bind(Map review) {
            String comment = (String) review.get("comment");
            Long rating = (Long) review.get("rating");
            String username = (String) review.get("username");

            if (comment != null && !comment.isEmpty()) {
                commentView.setText(comment);
            } else {
                Log.e(TAG, "No review found");
                commentView.setText("No review found"); // DEBUGGING
            }
            if (rating != null) {
                ratingView.setText("Rating: " + String.valueOf(rating));
            } else {
                Log.e(TAG, "No rating found");
                ratingView.setText("?"); // DEBUGGING
            }
            if (username != null && !username.isEmpty()) {
                usernameView.setText(username);
            } else {
                Log.e(TAG, "No username found");
                usernameView.setText("No username found"); // DEBUGGING
            }
        }
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.review_row_item, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    // Replace the contents of a view (invoked by the layout manager)
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        Log.d(TAG, "Binding review at position: " + position + " ...");
        viewHolder.bind(reviewList.get(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        Log.d(TAG, "Size of reviewList: " + reviewList.size());
        return reviewList.size();
    }
}
