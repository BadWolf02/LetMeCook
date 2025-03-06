package com.example.letmecook.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letmecook.R;
import com.example.letmecook.models.Ingredient;

import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    private final List<Ingredient> ingredientList;
    private final OnQuantityChangedListener listener;
    private final OnDeleteIngredientListener deleteListener;

    public interface OnQuantityChangedListener {
        void onQuantityChanged(String ingredientName, String newAmount);
    }

    public interface OnDeleteIngredientListener {
        void onDeleteIngredient(String ingredientName);
    }

    public InventoryAdapter(List<Ingredient> ingredientList, OnQuantityChangedListener listener, OnDeleteIngredientListener deleteListener) {
        this.ingredientList = ingredientList;
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView ingredientName;
        EditText ingredientAmount;
        ImageButton deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            ingredientName = itemView.findViewById(R.id.ingredient_name);
            ingredientAmount = itemView.findViewById(R.id.ingredient_amount);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.inventory_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ingredient ingredient = ingredientList.get(position);
        holder.ingredientName.setText(ingredient.getName());
        holder.ingredientAmount.setText(ingredient.getAmount());

        // Handle quantity change
        holder.ingredientAmount.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) { // Only update on focus loss
                String updatedAmount = holder.ingredientAmount.getText().toString().trim();
                if (!updatedAmount.isEmpty() && updatedAmount.matches("\\d+g")) {
                    String amountWithoutG = updatedAmount.replace("g", "").trim();

                    if (!amountWithoutG.equals(ingredient.getAmount().replace("g", "").trim())) { // 🟢 Avoid unnecessary updates
                        listener.onQuantityChanged(ingredient.getName(), amountWithoutG);
                    }
                }
            }
        });


        // Handle delete button click
        holder.deleteButton.setOnClickListener(v -> deleteListener.onDeleteIngredient(ingredient.getName()));
    }

    @Override
    public int getItemCount() {
        return ingredientList.size();
    }
}
