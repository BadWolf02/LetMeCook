package com.example.letmecook.ui.inventory;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letmecook.CameraActivity;
import com.example.letmecook.R;
import com.example.letmecook.adapters.InventoryAdapter;
import com.example.letmecook.databinding.FragmentInventoryBinding;
import com.example.letmecook.db_tools.SearchDB;
import com.example.letmecook.models.Ingredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryFragment extends Fragment {

    private FragmentInventoryBinding binding;
    private RecyclerView recyclerView;
    private InventoryAdapter inventoryAdapter;
    private List<Ingredient> ingredientList = new ArrayList<>();
    private SearchDB searchDB;
    private String householdID;
    private ImageButton addButton;
    private InventoryViewModel inventoryViewModel;
    private static final String TAG = "InventoryFragment";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        InventoryViewModel inventoryViewModel =
                new ViewModelProvider(this).get(InventoryViewModel.class);

        binding = FragmentInventoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Button toCameraButton = root.findViewById(R.id.toCamera);
        Button refreshInventoryButton = root.findViewById(R.id.refresh_button);

        final TextView titleTextView = binding.titleInventory;
        final TextView itemCountTextView = binding.itemCount;

        toCameraButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CameraActivity.class);
            startActivity(intent);
        });
        refreshInventoryButton.setOnClickListener(v -> refreshInventory());

        inventoryViewModel.getText().observe(getViewLifecycleOwner(), titleTextView::setText);

        recyclerView = root.findViewById(R.id.recycler_view_inventory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        inventoryAdapter = new InventoryAdapter(ingredientList, this::deleteIngredient);
        recyclerView.setAdapter(inventoryAdapter);

        addButton = root.findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> showAddIngredientDialog());

        searchDB = new SearchDB();

        // Fetch and display inventory
        String userID = "Exb0LIB0vIMVYJN2kAKHO0zd1mD2"; // Set your actual user ID
        loadUserInventory(userID);

        return root;
    }

    private void loadUserInventory(String userID) {
        if (userID == null || userID.isEmpty()) {
            Log.e(TAG, "User ID is null or empty.");
            return;
        }
        Log.d(TAG, "Fetching household ID for user: " + userID);
        searchDB.getUserHouseholdID(userID, hid -> {
            if (hid != null) {
                Log.d(TAG, "Household ID found: " + hid);
                householdID = hid;
                fetchHouseholdInventory();
            } else {
                Log.e(TAG, "Household ID not found for user.");
            }
        });
    }

    private void fetchHouseholdInventory() {
        Log.d(TAG, "Fetching inventory for household ID: " + householdID);
        searchDB.getHouseholdDocumentByID(householdID, documentSnapshot -> {
            if (documentSnapshot != null && documentSnapshot.exists()) {
                Log.d(TAG, "Household document found.");
                Map<String, Object> inventory = (Map<String, Object>) documentSnapshot.get("inventory");
                if (inventory != null) {
                    ingredientList.clear();

                    for (Map.Entry<String, Object> entry : inventory.entrySet()) {
                        String ingredientName = entry.getKey();
                        String amount = entry.getValue() != null ? entry.getValue().toString() + "g" : "0g";
                        ingredientList.add(new Ingredient(ingredientName, amount));
                    }

                    inventoryAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Inventory loaded and displayed.");
                } else {
                    Log.d(TAG, "Inventory is empty.");
                }
            } else {
                Log.e(TAG, "Household document not found.");
            }
        });
    }

    public void deleteIngredient(String ingredientName) {
        Log.d(TAG, "Deleting ingredient: " + ingredientName);

        // Remove from the local list
        ingredientList.removeIf(ingredient -> ingredient.getName().equals(ingredientName));
        inventoryAdapter.notifyDataSetChanged();

        // Remove from Firestore
        searchDB.getHouseholdDocumentByID(householdID, documentSnapshot -> {
            if (documentSnapshot != null && documentSnapshot.exists()) {
                Map<String, Object> inventory = (Map<String, Object>) documentSnapshot.get("inventory");
                if (inventory != null && inventory.containsKey(ingredientName)) {
                    inventory.remove(ingredientName);
                    searchDB.updateHouseholdInventory(householdID, inventory, success -> {
                        if (success) {
                            Log.d(TAG, "Ingredient deleted from Firestore.");
                        } else {
                            Log.e(TAG, "Failed to delete ingredient from Firestore.");
                        }
                    });
                }
            }
        });
    }

    private void showAddIngredientDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_ingredient, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText nameInput = dialogView.findViewById(R.id.ingredient_name);
        EditText gramsInput = dialogView.findViewById(R.id.ingredient_grams);
        Button addButton = dialogView.findViewById(R.id.add_button);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        addButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String grams = gramsInput.getText().toString().trim();

            if (!name.isEmpty() && !grams.isEmpty()) {
                Ingredient newIngredient = new Ingredient(name, grams + "g");
                ingredientList.add(newIngredient);
                inventoryAdapter.notifyItemInserted(ingredientList.size() - 1);
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void refreshInventory() {
        Toast.makeText(getContext(), "Refreshing Inventory...", Toast.LENGTH_SHORT).show();
        inventoryViewModel.refreshInventory(); // Call ViewModel to fetch updated data
    }
}
