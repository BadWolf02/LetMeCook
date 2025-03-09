package com.example.letmecook.adapters;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.letmecook.R;

import java.util.ArrayList;
import java.util.List;

import com.example.letmecook.RecipeViewActivity;
import com.example.letmecook.db_tools.SearchDB;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Document;

// https://developer.android.com/develop/ui/views/layout/recyclerview#java

/**
 * RecipeSearchAdapter is a RecyclerView.Adapter responsible for displaying a list of recipes
 * in a RecyclerView. It fetches recipes from a database, handles pagination, filtering, and
 * updates the UI accordingly.
 */
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


    /**
     * Changes the current page number and triggers a fetch for recipes on the new page.
     *
     * @param direction      The direction to change the page. Use -1 to go back a page, 1 to go forward, or 0 to stay on the current page (though this likely won't change the page).
     *                       Any other value will adjust the page number by that amount.
     * @param onPageChanged A Runnable that will be executed after the recipes for the new page have been fetched.
     *                      This is typically used to update the UI or perform other actions that depend on the new page data.
     *                      If null, no action will be taken after the recipes are fetched.
     *
     * @throws IllegalArgumentException if direction is not within acceptable bounds (though this is not enforced in the current implementation).
     *
     * @implNote The page number is automatically capped at 1 if it falls below 1.
     *           The actual fetching of recipes happens asynchronously in the `fetchRecipes` method.
     *           This method will immediately return and the onPageChanged runnable will be called some time later.
     */
    public void changePage(int direction, Runnable onPageChanged) {
        this.page += direction;
        if (this.page < 1) {this.page=1;}
        Log.d(TAG, "Page: " + this.page);
        fetchRecipes(onPageChanged);
    }


    /**
     * Fetches recipes from the database based on the current filter criteria and page number.
     *
     * This method performs the following actions:
     * 1. **Queries the database:** It uses the `searchDB.filterRecipes()` method to fetch recipes that match
     *    the specified `filterName`, `filterAuthor`, `filterCuisine`, and `filterIngredients`.
     *    The `page` parameter determines which page of results to retrieve.
     * 2. **Clears existing data:** It clears the `recipesList` to make way for the new set of recipes.
     * 3. **Processes the results:**
     *    - If recipes are found:
     *      - It iterates through the `DocumentSnapshot` objects representing each recipe.
     *      - It logs the recipe ID and name for debugging purposes.
     *      - It adds each recipe to the `recipesList`.
     *      - It determines if there's a next page of results by checking if the number of received recipes is equal to 6 (assuming a page size of 6).
     *    - If no recipes are found:
     *      - It sets `hasNextPage` to `false`.
     * 4. **Notifies data change:** It calls `notifyDataSetChanged()` to inform the UI (e.g., a RecyclerView) that the data has been updated.
     * 5. **Executes callback:** It calls the `onPageChanged.run()` callback. This allows the caller to perform actions after
     *    the data has been fetched and the UI has been updated, like updating UI element.
     *
     * @param onPageChanged A `Runnable` that will be executed after the recipes have been fetched,
     *                      processed, and the UI has been notified of the changes. This callback is typically
     *                      used to perform actions that depend on the updated recipe data or UI state.
     * @SuppressLint("NotifyDataSetChanged") Supresses a Lint warning about calling notifyDataSetChanged.
     * The context in which it is used is correct and safe.
     */
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

    /**
     * Retrieves a list of recipes that the user can cook based on their available ingredients.
     * This method fetches data from the database asynchronously and updates the UI accordingly.
     *
     * @param uid           The unique identifier of the user. This is used to fetch recipes
     *                      based on the ingredients they have available.
     * @param onPageChanged A Runnable object that will be executed after the recipes data
     *                      is fetched and the UI is updated. This is typically used to signal
     *                      that the data has been loaded and the UI can reflect the changes, such as updating a RecyclerView.
     *                      It allows performing actions like hiding loading indicators or updating UI elements after data fetching.
     *
     * <p>
     * The method performs the following actions:
     * 1.  It uses `searchDB.getWhatCanICook` to fetch a list of recipes from the database based on the user's ID (`uid`) and the current page (`this.page`).
     * 2.  Upon receiving the recipes:
     *     -   It clears the existing `recipesList`.
     *     -   If recipes are found:
     *         -   It iterates through each `DocumentSnapshot` in the `recipes` list.
     *         -   It logs the recipe ID and name for debugging purposes.
     *         -   It adds the `DocumentSnapshot` to the `recipesList`.
     *         -   It determines if there is a next page of results by checking if the size of the returned recipes is equal to 6 (assuming a page size of 6).
     *     -   If no recipes are found:
     *         - It sets `hasNextPage` to false.
     *     - It Notifies the adapter (e.g., RecyclerView.Adapter) that the underlying data has changed, so it will refresh the view.
     *     -   It executes the `onPageChanged` callback to signal completion.
     * </p>
     *
     * <p>
     * Note: This method assumes the presence of a `searchDB` object that provides the `getWhatCanICook` method for database interaction.
     * It also assumes `recipesList`, `hasNextPage`, `notifyDataSetChanged`, and `page` are instance members of the class.
     */
    public void whatCanICook(String uid, Runnable onPageChanged) {
        searchDB.getWhatCanICook(uid, this.page, recipes -> {
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
        private final ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            textView = view.findViewById(R.id.recipeName);
            imageView = view.findViewById(R.id.recipeImage);
        }

        public void bind(DocumentSnapshot recipe, Context context) {
            String recipeName = recipe.getString("r_name");
            String imgSrc = recipe.getString("img");
            if (recipeName != null && !recipeName.isEmpty()) {
                textView.setText(recipeName);
            } else {
                textView.setText("No name found"); // DEBUGGING
            }

            if (imgSrc != null && !imgSrc.isEmpty()) {
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageReference = storage.getReferenceFromUrl(imgSrc);
                storageReference.getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        Log.d(TAG, "Image URI: " + uri.toString());
                        // Load image using Glide
                        Glide.with(context)
                                .load(uri.toString())
                                .placeholder(R.drawable.placeholder_24dp)
                                .error(R.drawable.placeholder_24dp)
                                .into(imageView);
                    })
                    .addOnFailureListener(exception -> {
                        Log.e(TAG, "Image failed to load");
                    });
            } else {
                imageView.setImageResource(R.drawable.placeholder_24dp);
            }

            // Start intent for viewing recipe
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
            DocumentSnapshot recipe = recipesList.get(position);
            viewHolder.bind(recipe, context);

            Glide
                .with(context)
                .load(recipe.getString("img"))
                .placeholder(R.drawable.placeholder_24dp)
                .error(R.drawable.placeholder_24dp)
                .into(viewHolder.imageView);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        Log.d("TAG", "Size of recipeList: " + recipesList.size());
        return recipesList.size();
    }
}
