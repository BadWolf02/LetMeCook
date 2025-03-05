package com.example.letmecook.ui.inventory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.letmecook.R;
import com.example.letmecook.databinding.FragmentInventoryBinding;
import java.util.ArrayList;
import java.util.List;
import com.example.letmecook.models.Ingredient;
import com.example.letmecook.adapters.InventoryAdapter;

public class InventoryFragment extends Fragment {

    private FragmentInventoryBinding binding; // binding object allows interaction with views
    private ImageButton addButton;

    private RecyclerView recyclerView;
    private InventoryAdapter inventoryAdapter;
    private List<Ingredient> ingredientList = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        InventoryViewModel inventoryViewModel =
                new ViewModelProvider(this).get(InventoryViewModel.class);

        binding = FragmentInventoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView titleTextView = binding.titleInventory;
        final TextView itemCountTextView = binding.itemCount; // Correct ID from XML

        inventoryViewModel.getText().observe(getViewLifecycleOwner(), titleTextView::setText);

        recyclerView = root.findViewById(R.id.recycler_view_inventory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        inventoryAdapter = new InventoryAdapter(ingredientList);
        recyclerView.setAdapter(inventoryAdapter);

        addButton = root.findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> showAddIngredientDialog());

        return root;
    }

    private void showAddIngredientDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_ingredient, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();
        // Making background transparent to show rounded corners
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Dialog view references
        EditText nameInput = dialogView.findViewById(R.id.ingredient_name);
        EditText caloriesInput = dialogView.findViewById(R.id.ingredient_calories);
        EditText gramsInput = dialogView.findViewById(R.id.ingredient_grams);
        Spinner allergensSpinner = dialogView.findViewById(R.id.allergens_spinner);
        Spinner categoriesSpinner = dialogView.findViewById(R.id.categories_spinner);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);
        Button addButton = dialogView.findViewById(R.id.add_button);

        // Allergens Spinner
        // TODO: Change to multi-selection?
        // TODO: Replace with actual allergens in the db
        String[] allergens = {"None", "Peanuts", "Dairy", "Gluten", "Eggs"};
        ArrayAdapter<String> allergensAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, allergens);
        allergensSpinner.setAdapter(allergensAdapter);

        // Categories Spinner
        // TODO: Change to multi-selection?
        // TODO: Replace with actual categories in db
        String[] categories = {"None", "Vegetables", "Fruits", "Dairy", "Meat", "Seafood"};
        ArrayAdapter<String> categoriesAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, categories);
        categoriesSpinner.setAdapter(categoriesAdapter);

        // Close Dialog
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        // Add Button Click Event
        // TODO: Handle adding to the database
        // TODO: Save ingredient to the inventory
        addButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String calories = caloriesInput.getText().toString().trim();
            String grams = gramsInput.getText().toString().trim();
            String selectedAllergen = allergensSpinner.getSelectedItem().toString();
            String selectedCategory = categoriesSpinner.getSelectedItem().toString();

            // TODO: Add logic to save ingredient (e.g., save to ViewModel or database)
            if (!name.isEmpty() && !grams.isEmpty()) {
                //TODO: Create the new Ingredient object
                Ingredient newIngredient = new Ingredient(name, grams + "g");


                // Add to list and update RecyclerView
                ingredientList.add(newIngredient);
                inventoryAdapter.notifyItemInserted(ingredientList.size() - 1);
            }

            dialog.dismiss(); // Close dialog after adding ingredient
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}