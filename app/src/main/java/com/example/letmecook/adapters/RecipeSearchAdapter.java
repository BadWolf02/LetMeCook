package com.example.letmecook.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letmecook.R;

import java.util.ArrayList;
import java.util.List;

import com.example.letmecook.db_tools.SearchDB;
import com.google.firebase.firestore.DocumentSnapshot;

// https://developer.android.com/develop/ui/views/layout/recyclerview#java

public class RecipeSearchAdapter extends RecyclerView.Adapter<RecipeSearchAdapter.ViewHolder> {
    private final ArrayList<String> recipesList = new ArrayList<>();
    private final SearchDB searchDB = new SearchDB();

    public int page = 1;
    public boolean hasNextPage = true;

    // Filters
    private String filterName = null;
    private String filterAuthor = null;
    private String filterCuisine = null;
    private List<String> filterIngredients = new ArrayList<>();

    /**
     * Initialize the dataset of the Adapter
     * recipesList ArrayList<String> containing the data to populate views to be used
     * by RecyclerView
     */
    public RecipeSearchAdapter() {
        fetchRecipes(() -> {});
    }

    // Applies and stores filters, then updates
    public void applyFilters(String name, String author, String cuisine, List<String> ingredients, Runnable onPageChanged) {
        this.filterName = name;
        this.filterAuthor = author;
        this.filterCuisine = cuisine;
        this.filterIngredients = ingredients;

        this.page = 1; // Reset to first page when applying filters
        fetchRecipes(onPageChanged);
    }

    // Resets all filters to null and updates
    public void resetFilters(Runnable onPageChanged) {
        this.filterName = null;
        this.filterAuthor = null;
        this.filterCuisine = null;
        this.filterIngredients = new ArrayList<>();
        this.page = 1; // Reset to first page when resetting filters
        fetchRecipes(onPageChanged);
    }


    public void changePage(int direction, Runnable onPageChanged) {
        this.page += direction;
        if (this.page < 1) {this.page=1;}
        fetchRecipes(onPageChanged);
    }


    @SuppressLint("NotifyDataSetChanged")
    private void fetchRecipes(Runnable onPageChanged) {
        searchDB.filterRecipes(filterName, filterAuthor, filterCuisine, filterIngredients, this.page, recipes -> {
            recipesList.clear();
            if (!recipes.isEmpty()) {
                for (DocumentSnapshot doc : recipes) {
                    String recipeName = doc.getString("r_name");
                    recipesList.add(recipeName);
                }
                hasNextPage = recipes.size() == 8;
            } else {
                hasNextPage = false;
            }
            notifyDataSetChanged();
            onPageChanged.run();  // Call the callback after data is fetched
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            textView = view.findViewById(R.id.recipeName);
        }

        public void bind(String r_name) {textView.setText(r_name);}
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recipe_row_item, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    // Replace the contents of a view (invoked by the layout manager)
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        if (position < recipesList.size()) {
            viewHolder.bind(recipesList.get(position));
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return recipesList.size();
    }
}
