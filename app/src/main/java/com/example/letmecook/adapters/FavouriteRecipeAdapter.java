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

// https://developer.android.com/develop/ui/views/layout/recyclerview#java

public class FavouriteRecipeAdapter extends RecyclerView.Adapter<FavouriteRecipeAdapter.ViewHolder> {
    private final List<DocumentSnapshot> recipesList = new ArrayList<>();
    private final Context context;
    private final SearchDB searchDB = new SearchDB();

    // public int page = 1;
    // public boolean hasNextPage = true;

    private final String uid;

    /**
     * Initialize the dataset of the Adapter
     * recipesList ArrayList<String> containing the data to populate views to be used
     * by RecyclerView
     */
    public FavouriteRecipeAdapter(String uid, Context context) {
        this.uid = uid;
        this.context = context;
        fetchRecipes(() -> {});
    }


    /*
    public void changePage(int direction, Runnable onPageChanged) {
        this.page += direction;
        if (this.page < 1) {this.page=1;}
        Log.d(TAG, "Page: " + this.page);
        fetchRecipes(onPageChanged);
    }
     */


    @SuppressLint("NotifyDataSetChanged")
    private void fetchRecipes(Runnable onPageChanged) {
        searchDB.getFavouriteRecipes(uid, recipes -> {
            recipesList.clear();
            if (!recipes.isEmpty()) {
                for (DocumentSnapshot doc : recipes) {
                    Log.d(TAG, "Recipe ID: " + doc.getId());
                    Log.d(TAG, "Recipe name: " + doc.getString("r_name"));
                    recipesList.add(doc);
                }
                // hasNextPage = recipes.size() == 6;
            }
            /* else {
                hasNextPage = false;
            }
             */
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
                            Glide.with(context)
                                    .load(uri.toString())
                                    .placeholder(R.drawable.placeholder_24dp)
                                    .error(R.drawable.placeholder_24dp)
                                    .into(imageView);
                        })
                        .addOnFailureListener(exception -> Log.e(TAG, "Image failed to load"));
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
