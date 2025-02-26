package com.example.letmecook.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letmecook.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    private final Context context;
    private Map<String, Integer> inventoryMap;
    private final OnItemRemovedListener itemRemovedListener;
    private final FirebaseFirestore firestore;

    public interface OnItemRemovedListener {
        void onItemRemoved(Map<String, Integer> updatedInventory);
    }

    public InventoryAdapter(Context context, Map<String, Integer> inventoryMap, OnItemRemovedListener listener) {
        this.context = context;
        this.inventoryMap = inventoryMap;
        this.itemRemovedListener = listener;
        // Initialize Firestore
        this.firestore = FirebaseFirestore.getInstance();
    }

    public void updateInventory(Map<String, Integer> newInventory) {
        this.inventoryMap = newInventory;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.inventory_item_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        List<String> ingredientList = new ArrayList<>(inventoryMap.keySet());
        String ingredient = ingredientList.get(position);
        int quantity = inventoryMap.getOrDefault(ingredient, 0);

        holder.itemText.setText(ingredient + ": " + quantity);

        holder.removeButton.setOnClickListener(v -> showRemoveDialog(ingredient, quantity));
    }

    @Override
    public int getItemCount() {
        return inventoryMap.size();
    }

    private void showRemoveDialog(String ingredient, int currentQuantity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Remove " + ingredient);

        final EditText input = new EditText(context);
        input.setHint("Enter quantity (1 to " + currentQuantity + ")");
        builder.setView(input);

        builder.setPositiveButton("Remove", (dialog, which) -> {
            try {
                int amountToRemove = Integer.parseInt(input.getText().toString());

                if (amountToRemove <= 0 || amountToRemove > currentQuantity) {
                    Toast.makeText(context, "Invalid quantity", Toast.LENGTH_SHORT).show();
                } else {
                    updateInventory(ingredient, currentQuantity - amountToRemove);
                    updateFirestoreInventory(ingredient, currentQuantity - amountToRemove); // Update Firestore
                }
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateInventory(String ingredient, int newQuantity) {
        if (newQuantity <= 0) {
            inventoryMap.remove(ingredient);
        } else {
            inventoryMap.put(ingredient, newQuantity);
        }

        itemRemovedListener.onItemRemoved(inventoryMap);
        notifyDataSetChanged();
    }

    // Update the inventory in Firestore after a change
    private void updateFirestoreInventory(String ingredient, int newQuantity) {
        DocumentReference ingredientRef = firestore.collection("inventory").document(ingredient);

        if (newQuantity <= 0) {
            // If quantity becomes 0 or less, delete the ingredient from Firestore
            ingredientRef.delete()
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, ingredient + " removed from Firestore", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(context, "Error removing ingredient", Toast.LENGTH_SHORT).show());
        } else {
            // Update the quantity of the ingredient in Firestore
            ingredientRef.update("quantity", newQuantity)
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, ingredient + " updated in Firestore", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(context, "Error updating ingredient", Toast.LENGTH_SHORT).show());
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemText;
        ImageButton removeButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemText = itemView.findViewById(R.id.inventory_item_text);
            removeButton = itemView.findViewById(R.id.remove_button);
        }
    }
}
