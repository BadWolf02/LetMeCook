package com.example.letmecook.adapters;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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

import com.example.letmecook.RecipeViewActivity;
import com.example.letmecook.db_tools.SearchDB;
import com.google.firebase.firestore.DocumentSnapshot;

// https://developer.android.com/develop/ui/views/layout/recyclerview#java

public class RecipeSearchAdapter extends RecyclerView.Adapter<RecipeSearchAdapter.ViewHolder> {
    private final List<DocumentSnapshot> recipesList = new ArrayList<>();
    private final Context context;
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
    public RecipeSearchAdapter(Context context) {
        this.context = context;
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
        Log.d(TAG, "Page: " + this.page);
        fetchRecipes(onPageChanged);
    }


    @SuppressLint("NotifyDataSetChanged")
    private void fetchRecipes(Runnable onPageChanged) {
        searchDB.filterRecipes(filterName, filterAuthor, filterCuisine, filterIngredients, this.page, recipes -> {
            recipesList.clear();
            if (!recipes.isEmpty()) {
                for (DocumentSnapshot doc : recipes) {
                    Log.d(TAG, "Recipe ID: " + doc.getId());
                    Log.d(TAG, "Recipe name: " + doc.getString("r_name"));
                    recipesList.add(doc);
                }
                hasNextPage = recipes.size() == 6;
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

        public void bind(DocumentSnapshot recipe, Context context) {
            String recipeName = recipe.getString("r_name");
            if (recipeName != null && !recipeName.isEmpty()) {
                textView.setText(recipeName);
            } else {
                textView.setText("No name found"); // DEBUGGING
            }

            //
            itemView.setOnClickListener(view -> {
                Intent intent = new Intent(context, RecipeViewActivity.class);
                intent.putExtra("recipeID", recipe.getId());
                context.startActivity(intent);
            });
        }
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
            viewHolder.bind(recipesList.get(position), context);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        Log.d("TAG", "Size of recipeList: " + recipesList.size());
        return recipesList.size();
    }
}
