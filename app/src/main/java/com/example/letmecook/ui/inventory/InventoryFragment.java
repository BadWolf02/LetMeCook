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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letmecook.CameraActivity;
import com.example.letmecook.R;
import com.example.letmecook.adapters.InventoryAdapter;
import com.example.letmecook.databinding.FragmentInventoryBinding;
import com.example.letmecook.db_tools.SearchDB;
import com.example.letmecook.models.Ingredient;
import com.google.firebase.auth.FirebaseAuth;

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
    private boolean isFragmentActive = true;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        inventoryViewModel = new ViewModelProvider(this).get(InventoryViewModel.class);

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

        inventoryAdapter = new InventoryAdapter(ingredientList, this::updateIngredientQuantity, this::deleteIngredient);
        recyclerView.setAdapter(inventoryAdapter);

        addButton = root.findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> showAddIngredientDialog());

        searchDB = new SearchDB();

        // Fetch and display inventory
        String userID = FirebaseAuth.getInstance().getUid();
        loadUserInventory(userID);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        isFragmentActive = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        isFragmentActive = false;
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
            if (documentSnapshot != null && documentSnapshot.exists() && isFragmentActive) {
                Map<String, Object> inventory = (Map<String, Object>) documentSnapshot.get("inventory");
                if (inventory != null) {
                    ingredientList.clear();

                    for (Map.Entry<String, Object> entry : inventory.entrySet()) {
                        String ingredientName = entry.getKey();
                        String amount = entry.getValue() != null ? entry.getValue().toString() + "g" : "0g";
                        ingredientList.add(new Ingredient(ingredientName, amount));
                    }

                    recyclerView.post(() -> {
                        if (isFragmentActive && recyclerView != null && inventoryAdapter != null) {
                            inventoryAdapter.notifyDataSetChanged();
                            updateItemCount();
                        }
                    });
                }
            }
        });
    }

    private void showAddIngredientDialog() {
        searchDB.getAllIngredients(ingredientNames -> {
            if (getContext() == null || ingredientNames.isEmpty()) {
                Toast.makeText(getContext(), "No ingredients found!", Toast.LENGTH_SHORT).show();
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Select Ingredient");

            String[] ingredientsArray = ingredientNames.toArray(new String[0]);
            builder.setItems(ingredientsArray, (dialog, which) -> {
                String selectedIngredient = ingredientsArray[which];
                showQuantityDialog(selectedIngredient);
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            builder.show();
        });
    }

    private void showQuantityDialog(String ingredientName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter Quantity for " + ingredientName);

        final EditText gramsInput = new EditText(requireContext());
        gramsInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        gramsInput.setHint("Quantity in grams");

        builder.setView(gramsInput);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String grams = gramsInput.getText().toString().trim();
            if (!grams.isEmpty()) {
                addIngredientToInventory(ingredientName, grams + "g");
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void addIngredientToInventory(String name, String amount) {
        int amountToAdd = Integer.parseInt(amount.replace("g", "").trim());

        searchDB.getHouseholdDocumentByID(householdID, documentSnapshot -> {
            if (documentSnapshot != null && documentSnapshot.exists()) {
                Map<String, Object> inventory = (Map<String, Object>) documentSnapshot.get("inventory");
                if (inventory == null) inventory = new HashMap<>();

                int currentAmount = inventory.containsKey(name) ? Integer.parseInt(inventory.get(name).toString()) : 0;
                int newAmount = currentAmount + amountToAdd;

                inventory.put(name, newAmount);
                searchDB.updateHouseholdInventory(householdID, inventory, success -> {
                    if (success) {
                        Toast.makeText(getContext(), "Updated " + name + " to " + newAmount + "g!", Toast.LENGTH_SHORT).show();
                        fetchHouseholdInventory();
                    }
                });
            }
        });
    }

    private void updateIngredientQuantity(String name, String newAmount) {
        searchDB.getHouseholdDocumentByID(householdID, documentSnapshot -> {
            if (documentSnapshot != null && documentSnapshot.exists()) {
                Map<String, Object> inventory = (Map<String, Object>) documentSnapshot.get("inventory");
                if (inventory != null && inventory.containsKey(name)) {
                    inventory.put(name, newAmount);
                    searchDB.updateHouseholdInventory(householdID, inventory, success -> {
                        if (success) {
                            Toast.makeText(getContext(), name + " updated to " + newAmount + "g!", Toast.LENGTH_SHORT).show();
                            fetchHouseholdInventory();
                        } else {
                            Toast.makeText(getContext(), "Failed to update " + name, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }


    public void deleteIngredient(String ingredientName) {
        searchDB.getHouseholdDocumentByID(householdID, documentSnapshot -> {
            if (documentSnapshot != null && documentSnapshot.exists()) {
                Map<String, Object> inventory = (Map<String, Object>) documentSnapshot.get("inventory");
                if (inventory != null && inventory.containsKey(ingredientName)) {
                    inventory.remove(ingredientName);
                    searchDB.updateHouseholdInventory(householdID, inventory, success -> {
                        if (success) {
                            Toast.makeText(getContext(), ingredientName + " removed!", Toast.LENGTH_SHORT).show();
                            fetchHouseholdInventory();
                        }
                    });
                }
            }
        });
    }


    private void updateItemCount() {
        TextView itemCountTextView = binding.itemCount;
        itemCountTextView.setText(ingredientList.size() + " items");
    }

    private void refreshInventory() {
        String userID = FirebaseAuth.getInstance().getUid();
        loadUserInventory(userID);
        inventoryAdapter.notifyDataSetChanged();
        updateItemCount();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
