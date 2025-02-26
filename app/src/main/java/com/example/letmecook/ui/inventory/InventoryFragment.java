package com.example.letmecook.ui.inventory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letmecook.R;
import com.example.letmecook.adapters.InventoryAdapter;

public class InventoryFragment extends Fragment {

    private InventoryViewModel inventoryViewModel;
    private InventoryAdapter inventoryAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventory, container, false);

        TextView inventoryText = view.findViewById(R.id.text_inventory);
        RecyclerView recyclerView = view.findViewById(R.id.inventory_recycler_view);
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

        return view;
    }
}
