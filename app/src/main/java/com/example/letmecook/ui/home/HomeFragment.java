package com.example.letmecook.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letmecook.R;
import com.example.letmecook.adapters.RecipeSearchAdapter;
import com.example.letmecook.databinding.FragmentHomeBinding;
import com.example.letmecook.db_tools.SearchDB;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

private FragmentHomeBinding binding;

    private final SearchDB searchDB = new SearchDB();
    private RecipeSearchAdapter recipeAdapter;

    private Button prevPageButton, nextPageButton;
    private LinearLayout filteredIngredientsLayout;

    private final ArrayList<String> ingredients = new ArrayList<>();
    private final ArrayList<String> filteredIngredients = new ArrayList<>();


    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize UI components
        RecyclerView recyclerView = binding.recyclerView;
        EditText nameBar = binding.nameBar;
        EditText authorBar = binding.authorBar;
        filteredIngredientsLayout = binding.ingredientsLayout;

        Button showMenuButton = binding.showMenuButton;
        Button resetButton = binding.resetButton;
        Button searchButton = binding.searchButton;
        prevPageButton = binding.prevButton;
        nextPageButton = binding.nextButton;

        ListPopupWindow listPopupWindow = new ListPopupWindow(requireContext());
        listPopupWindow.setAnchorView(showMenuButton); // Anchor view to menu button

        // Create an adapter with the menu items
        searchDB.getIngredients(foundIngredients -> {
            ingredients.clear();
            ingredients.addAll(foundIngredients);

            // Create an adapter to hold all ingredients in dropdown
            ArrayAdapter<String> menuAdapter = new ArrayAdapter<>(requireContext(),
                    R.layout.ingredient_dropdown_item, ingredients);
            listPopupWindow.setAdapter(menuAdapter);
        });

        // https://m2.material.io/components/menus/android#dropdown-menus
        // TODO fix bug where some items cannot be clicked
        listPopupWindow.setOnItemClickListener((parent, view, position, id) -> {
            // Add item to filteredIngredients on click
            String selectedItem = ingredients.get(position);
            if (!filteredIngredients.contains(selectedItem)) {
                filteredIngredients.add(selectedItem);
                addIngredient();
            }
            listPopupWindow.dismiss(); // Dismiss the popup after selection
        });

        // Show the ListPopupWindow when the button is clicked
        showMenuButton.setOnClickListener(v -> {
            if (!ingredients.isEmpty()) {
                listPopupWindow.show();
            }
        });

        // Reset all filters
        resetButton.setOnClickListener(v -> resetFilters());

        // Searches based on filters
        searchButton.setOnClickListener(view -> {
            filterRecipes(
                    nameBar.getText().toString(),
                    authorBar.getText().toString(),
                    null,
                    filteredIngredients);
        });

        // Set up RecyclerView for recipes
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recipeAdapter = new RecipeSearchAdapter(requireContext()); // Initialize adapter
        recyclerView.setAdapter(recipeAdapter);
        filterRecipes(null, null, null, null);


        // Changes pages
        prevPageButton.setOnClickListener(view -> changePage(-1));
        nextPageButton.setOnClickListener(view -> changePage(1));

        prevPageButton.setVisibility(View.GONE); // hide back button on page 1
        if (recipeAdapter.hasNextPage) {nextPageButton.setVisibility(View.VISIBLE);} // show next button if more data

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Call adapter's applyFilters method with user input
    private void filterRecipes(String name, String author, String cuisine, ArrayList<String> ingredients) {
        recipeAdapter.applyFilters(name, author, cuisine, ingredients, () -> {});
        changePage(0);
    }

    // Handle pagination logic to hide/display page buttons
    private void changePage(int direction) {
        recipeAdapter.changePage(direction, () -> {
            if (recipeAdapter.page == 1) {
                prevPageButton.setVisibility(View.GONE);
            } else {
                prevPageButton.setVisibility(View.VISIBLE);
            }

            if (!recipeAdapter.hasNextPage) {
                nextPageButton.setVisibility(View.GONE);
            } else {
                nextPageButton.setVisibility(View.VISIBLE);
            }
        });
    }

    // Resets all filter and displays default
    private void resetFilters() {
        recipeAdapter.resetFilters(()->{});
        changePage(0);
        filteredIngredientsLayout.removeAllViews();
    }

    // Handle displaying ingredients that are selected for filter
    private void addIngredient() {
        filteredIngredientsLayout.removeAllViews();
        for (String ingredient : filteredIngredients) {
            TextView ingredientView = new TextView(requireContext());
            ingredientView.setText(ingredient);
            ingredientView.setTextSize(14);
            ingredientView.setPadding(8,8,8,8);
            ingredientView.setTextColor(Color.BLACK);

            LinearLayout listItemLayout = new LinearLayout(requireContext());
            listItemLayout.setOrientation(LinearLayout.HORIZONTAL);
            listItemLayout.addView(ingredientView);
            filteredIngredientsLayout.addView(listItemLayout);
        }

    }
}

