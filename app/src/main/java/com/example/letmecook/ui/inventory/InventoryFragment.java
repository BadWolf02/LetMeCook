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
import com.example.letmecook.R;

public class InventoryFragment extends Fragment {

    private InventoryViewModel inventoryViewModel;
    private TextView inventoryText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventory, container, false);
        inventoryText = view.findViewById(R.id.inventory_text);

        inventoryViewModel = new ViewModelProvider(this).get(InventoryViewModel.class);
        inventoryViewModel.getInventoryText().observe(getViewLifecycleOwner(), inventoryText::setText);

        return view;
    }
}
