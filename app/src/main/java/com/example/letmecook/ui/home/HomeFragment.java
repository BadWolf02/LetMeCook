package com.example.letmecook.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListPopupWindow;

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

    private Button showMenuButton, resetButton, searchButton, prevPageButton, nextPageButton;

    private ArrayList<String> ingredients = new ArrayList<>();
    private ArrayList<String> filteredIngredients = new ArrayList<>();


    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize UI components
        RecyclerView recyclerView = binding.recyclerView;
        EditText nameBar = binding.nameBar;
        EditText authorBar = binding.authorBar;

        showMenuButton = binding.showMenuButton;
        resetButton = binding.resetButton;
        searchButton = binding.searchButton;
        prevPageButton = binding.prevButton;
        nextPageButton = binding.nextButton;

        ListPopupWindow listPopupWindow = new ListPopupWindow(requireContext());
        listPopupWindow.setAnchorView(showMenuButton); // Set the view to anchor the popup

        // Create an adapter with the menu items

        searchDB.getIngredients(foundIngredients -> {
            ingredients.clear();
            ingredients.addAll(foundIngredients);

            // Create an adapter and set it to ListPopupWindow
            ArrayAdapter<String> menuAdapter = new ArrayAdapter<>(requireContext(),
                    R.layout.ingredient_dropdown_item, ingredients);
            listPopupWindow.setAdapter(menuAdapter);
        });

        // Set item click listener
        // https://m2.material.io/components/menus/android#dropdown-menus
        // TODO fix bug where some items cannot be clicked
        listPopupWindow.setOnItemClickListener((parent, view, position, id) -> {
            // Add item to filteredIngredients on click
            String selectedItem = ingredients.get(position);
            if (!filteredIngredients.contains(selectedItem)) {
                filteredIngredients.add(selectedItem);
                showMenuButton.setText(selectedItem); // Update button text with last selection
            }
            // Perform actions based on the selected item
            listPopupWindow.dismiss(); // Dismiss the popup after selection
        });

        // Show the ListPopupWindow when the button is clicked
        showMenuButton.setOnClickListener(v -> {
            if (!ingredients.isEmpty()) {
                listPopupWindow.show();
            }
        });

        // Show the ListPopupWindow when the button is clicked
        resetButton.setOnClickListener(v -> {
            recipeAdapter.resetFilters(()->{});
            changePage(0);
        });

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
}

