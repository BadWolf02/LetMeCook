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

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {  // ✅ Use InventoryViewHolder

    private List<Ingredient> ingredientList;

    private OnDeleteClickListener deleteClickListener;

    public InventoryAdapter(List<Ingredient> ingredientList, OnDeleteClickListener deleteClickListener) {
        this.ingredientList = ingredientList;
        this.deleteClickListener = deleteClickListener;
    }
    public interface OnDeleteClickListener {
        void onDeleteClick(String ingredientName);
    }


    public static class InventoryViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        EditText amountEditText;
        ImageButton deleteButton, cartButton;

        public InventoryViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.ingredient_name);
            amountEditText = itemView.findViewById(R.id.ingredient_amount);
            deleteButton = itemView.findViewById(R.id.delete_button);
            cartButton = itemView.findViewById(R.id.cart_button);
        }
    }


    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.inventory_list_item, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        Ingredient ingredient = ingredientList.get(position);
        holder.nameTextView.setText(ingredient.getName());
        holder.amountEditText.setText(ingredient.getAmount());

        // Delete button
        holder.deleteButton.setOnClickListener(v -> {
            ingredientList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, ingredientList.size());
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(ingredient.getName());
            }
        });


        // Shopping cart button (TODO: Implement function)
        holder.cartButton.setOnClickListener(v -> {
            // TODO: Handle shopping cart click
        });
    }

    @Override
    public int getItemCount() {
        return ingredientList.size();
    }
}