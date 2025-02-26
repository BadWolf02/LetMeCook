package com.example.letmecook.ui.inventory;

import android.content.Intent;
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
import com.example.letmecook.CameraActivity;
import com.example.letmecook.databinding.FragmentInventoryBinding;

public class InventoryFragment extends Fragment {

    private static final String TAG = "InventoryFragment";
    private FragmentInventoryBinding binding;
    private InventoryViewModel inventoryViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        inventoryViewModel = new ViewModelProvider(this).get(InventoryViewModel.class);

        // Use view binding to inflate the layout
        binding = FragmentInventoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Handle the "Open Camera" button click
        binding.toCamera.setOnClickListener(v -> openCamera());

        // Observe LiveData and update the inventory text
        inventoryViewModel.getInventoryText().observe(getViewLifecycleOwner(), inventoryText -> {
            binding.textInventory.setText(inventoryText);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Clean up the binding
    }

    private void openCamera() {
        Intent intent = new Intent(requireContext(), CameraActivity.class);
        startActivity(intent);
    }
}
