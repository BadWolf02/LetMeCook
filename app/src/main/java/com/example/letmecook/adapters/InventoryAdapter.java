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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

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
        this.firestore = FirebaseFirestore.getInstance(); // Initialize Firestore
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
        String[] ingredientList = inventoryMap.keySet().toArray(new String[0]);
        String ingredient = ingredientList[position];
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
                }
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateInventory(String ingredient, int newQuantity) {
        // Update the local map
        if (newQuantity <= 0) {
            inventoryMap.remove(ingredient); // Remove from local map
        } else {
            inventoryMap.put(ingredient, newQuantity); // Update local map
        }

        // Update Firestore
        updateFirestore(inventoryMap);

        // Notify the listener to update the UI
        itemRemovedListener.onItemRemoved(inventoryMap);
        notifyDataSetChanged();
    }

    private void updateFirestore(Map<String, Integer> updatedInventory) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Assume user is authenticated

        firestore.collection("users")
                .document(userId)
                .update("inventory", updatedInventory) // Update the entire inventory map
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Inventory updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to update inventory", Toast.LENGTH_SHORT).show();
                });
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
