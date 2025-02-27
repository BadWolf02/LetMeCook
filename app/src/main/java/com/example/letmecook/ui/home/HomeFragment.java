package com.example.letmecook.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

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

    private SearchDB searchDB = new SearchDB();
    private RecipeSearchAdapter adapter;

    private Button searchButton, prevPageButton, nextPageButton;

    private ArrayList<String> ingredients = new ArrayList<>();
    private ArrayList<String> filteredIngredients = new ArrayList<>();
    private ArrayAdapter<String> ingredientAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize UI components
        RecyclerView recyclerView = binding.recyclerView;
        EditText nameBar = binding.nameBar;
        EditText authorBar = binding.authorBar;

        searchButton = binding.searchButton;
        prevPageButton = binding.prevButton;
        nextPageButton = binding.nextButton;

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
        adapter = new RecipeSearchAdapter(requireContext()); // Initialize adapter
        recyclerView.setAdapter(adapter);
        filterRecipes(null, null, null, null);


        // Changes pages
        prevPageButton.setOnClickListener(view -> changePage(-1));
        nextPageButton.setOnClickListener(view -> changePage(1));

        prevPageButton.setVisibility(View.GONE); // hide back button on page 1
        if (adapter.hasNextPage) {nextPageButton.setVisibility(View.VISIBLE);} // show next button if more data

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Call adapter's applyFilters method with user input
    private void filterRecipes(String name, String author, String cuisine, ArrayList<String> ingredients) {
        adapter.applyFilters(name, author, cuisine, ingredients, () -> {});
        changePage(0);
    }

    private void changePage(int direction) {
        adapter.changePage(direction, () -> {
            if (adapter.page == 1) {
                prevPageButton.setVisibility(View.GONE);
            } else {
                prevPageButton.setVisibility(View.VISIBLE);
            }

            if (!adapter.hasNextPage) {
                nextPageButton.setVisibility(View.GONE);
            } else {
                nextPageButton.setVisibility(View.VISIBLE);
            }
        });
    }
}

