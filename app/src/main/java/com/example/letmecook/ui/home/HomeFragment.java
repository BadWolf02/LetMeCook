package com.example.letmecook.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letmecook.R;
import com.example.letmecook.adapters.RecipeSearchAdapter;
import com.example.letmecook.databinding.FragmentHomeBinding;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

private FragmentHomeBinding binding;

    private RecipeSearchAdapter adapter;
    private RecyclerView recyclerView;
    private EditText searchInput;
    private Button searchButton, prevPageButton, nextPageButton;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize UI components
        recyclerView = binding.recyclerView;
        searchInput = binding.testInput;
        searchButton = binding.testButton;
        prevPageButton = binding.prevButton;
        nextPageButton = binding.nextButton;

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RecipeSearchAdapter(requireContext()); // Initialize adapter
        recyclerView.setAdapter(adapter);
        adapter.applyFilters(null, null, null, null, () -> {});

        searchButton.setOnClickListener(view -> filterRecipes(null, searchInput.getText().toString(), null, null));

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

