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

        toCameraButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CameraActivity.class);
            startActivity(intent);
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

}
