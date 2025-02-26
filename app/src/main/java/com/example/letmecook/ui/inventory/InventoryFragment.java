package com.example.letmecook.ui.inventory;

import android.content.Intent;
import android.os.Bundle; //Passes data to the fragment and restores its state after config changes
import android.view.LayoutInflater; // handles the XML layout file into a View object
import android.view.View;
import android.view.ViewGroup; // View object that gets displayed by the fragment
import android.widget.TextView;
import androidx.annotation.NonNull; //Arguments or return values that cannot be null
import androidx.fragment.app.Fragment; //base class for the fragment
import androidx.lifecycle.ViewModelProvider; //managing ViewModelProvider

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.letmecook.R;
import com.example.letmecook.CameraActivity;
import com.example.letmecook.databinding.FragmentInventoryBinding;

public class InventoryFragment extends Fragment {

    private static final String TAG = "InventoryFragment";
    private FragmentInventoryBinding binding; // binding object allows interaction with views

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        InventoryViewModel inventoryViewModel =
                new ViewModelProvider(this).get(InventoryViewModel.class);

        binding = FragmentInventoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.toCamera.setOnClickListener(v -> openCamera());

        final TextView textView = binding.textInventory;
        inventoryViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }
    private InventoryViewModel inventoryViewModel;
    private TextView inventoryTextView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventory, container, false);

        inventoryTextView = view.findViewById(R.id.inventory_text_view);
        inventoryViewModel = new ViewModelProvider(this).get(InventoryViewModel.class);

        inventoryViewModel.getInventoryText().observe(getViewLifecycleOwner(), inventoryText -> {
            inventoryTextView.setText(inventoryText);
        });

        return view;
        }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void openCamera() {
        Intent intent = new Intent(requireContext(), CameraActivity.class);
        startActivity(intent);
    }
}