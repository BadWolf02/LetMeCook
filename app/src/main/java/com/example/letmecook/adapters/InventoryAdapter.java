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

        holder.ingredientAmount.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                handleQuantityUpdate(holder, ingredient);
                return true;
            }
            return false;
        });

        holder.ingredientAmount.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                handleQuantityUpdate(holder, ingredient);
            }
        });




        // Handle delete button click
        holder.deleteButton.setOnClickListener(v -> deleteListener.onDeleteIngredient(ingredient.getName()));
    }

    private void handleQuantityUpdate(ViewHolder holder, Ingredient ingredient) {
        String updatedAmount = holder.ingredientAmount.getText().toString().trim();

        if (!updatedAmount.isEmpty() && updatedAmount.matches("\\d+[gG]?")) {
            String amountWithoutG = updatedAmount.replaceAll("[gG]", "").trim();

            if (!amountWithoutG.equals(ingredient.getAmount().replace("g", "").trim())) {
                listener.onQuantityChanged(ingredient.getName(), amountWithoutG);
            }
        }
    }


    @Override
    public int getItemCount() {
        return ingredientList.size();
    }

    public void updateList(List<Ingredient> newList) {
        ingredientList.clear();
        ingredientList.addAll(newList);
        notifyDataSetChanged();
    }

}
