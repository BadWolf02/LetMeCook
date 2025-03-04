package com.example.letmecook.ui.inventory;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Toast;

import com.example.letmecook.CameraActivity;
import com.example.letmecook.R;
import com.example.letmecook.adapters.InventoryAdapter;

import java.util.HashMap;
import java.util.Map;

public class InventoryFragment extends Fragment {

    private InventoryViewModel inventoryViewModel;
    private InventoryAdapter inventoryAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventory, container, false);

        TextView inventoryText = view.findViewById(R.id.text_inventory);
        RecyclerView recyclerView = view.findViewById(R.id.inventory_recycler_view);
        Button addIngredientButton = view.findViewById(R.id.add_ingredient_button);
        Button toCameraButton = view.findViewById(R.id.toCamera);
        Button chooseFromDatabaseButton = view.findViewById(R.id.choose_from_database_button);

        toCameraButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CameraActivity.class);
            startActivity(intent);
        });

        chooseFromDatabaseButton.setOnClickListener(v -> {
            inventoryViewModel.fetchIngredientsFromDatabase(ingredients -> {
                if (ingredients == null || ingredients.isEmpty()) {
                    Toast.makeText(getContext(), "No ingredients found in the database", Toast.LENGTH_SHORT).show();
                } else {
                    showIngredientSelectionDialog(ingredients);
                }
            });
        });



        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        inventoryViewModel = new ViewModelProvider(this).get(InventoryViewModel.class);

        inventoryViewModel.getInventoryLiveData().observe(getViewLifecycleOwner(), inventory -> {
            if (inventoryAdapter == null) {
                inventoryAdapter = new InventoryAdapter(getContext(), inventory, updatedInventory -> {
                    inventoryViewModel.updateInventory(updatedInventory);
                });
                recyclerView.setAdapter(inventoryAdapter);
            } else {
                inventoryAdapter.updateInventory(inventory);
            }
        });

        inventoryViewModel.getMessageText().observe(getViewLifecycleOwner(), inventoryText::setText);

        addIngredientButton.setOnClickListener(v -> showAddIngredientDialog());

        return view;
    }

    private void showAddIngredientDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Ingredient");

        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.add_ingredient_dialog, null);
        final EditText ingredientNameInput = viewInflated.findViewById(R.id.ingredient_name_input);
        final EditText quantityInput = viewInflated.findViewById(R.id.quantity_input);

        builder.setView(viewInflated);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String ingredient = ingredientNameInput.getText().toString().trim();
            String quantityStr = quantityInput.getText().toString().trim();

            if (!ingredient.isEmpty() && !quantityStr.isEmpty()) {
                try {
                    int quantity = Integer.parseInt(quantityStr);
                    if (quantity > 0) {
                        inventoryViewModel.addIngredient(ingredient, quantity);
                    } else {
                        inventoryViewModel.setMessage("Quantity must be greater than 0");
                    }
                } catch (NumberFormatException e) {
                    inventoryViewModel.setMessage("Invalid quantity");
                }
            } else {
                inventoryViewModel.setMessage("Both fields are required");
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
    private void showIngredientSelectionDialog(Map<String, String> ingredients) {
        String[] ingredientNames = ingredients.keySet().toArray(new String[0]);

        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Choose an Ingredient")
                .setItems(ingredientNames, (dialog, which) -> {
                    String chosenIngredient = ingredientNames[which];
                    showQuantityDialog(chosenIngredient);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void showQuantityDialog(String ingredientName) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Enter Quantity for " + ingredientName);

        final EditText input = new EditText(getContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String quantityStr = input.getText().toString();
            if (!quantityStr.isEmpty()) {
                int quantity = Integer.parseInt(quantityStr);
                if (quantity > 0) {
                    Map<String, Integer> updatedInventory = new HashMap<>();
                    updatedInventory.put(ingredientName, quantity);
                    inventoryViewModel.updateInventory(updatedInventory);
                } else {
                    Toast.makeText(getContext(), "Quantity must be greater than 0", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Quantity cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }



}
