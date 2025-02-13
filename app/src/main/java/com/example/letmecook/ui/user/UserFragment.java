package com.example.letmecook.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.letmecook.HouseholdManageActivity;
import com.example.letmecook.tools.Authentication;

import com.example.letmecook.R;
import com.example.letmecook.databinding.FragmentUserBinding;
import com.example.letmecook.tools.Household;

public class UserFragment extends Fragment {

    private FragmentUserBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        UserViewModel userViewModel =
                new ViewModelProvider(this).get(UserViewModel.class);

        Authentication auth = new Authentication(requireContext());
        Household household = new Household(requireContext());

        binding = FragmentUserBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // final TextView textView = binding.textUser;
        // userViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        Button householdButton = binding.getRoot().findViewById(R.id.householdButton);
        householdButton.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), HouseholdManageActivity.class);
            startActivity(intent);
        });

        Button signOutButton = binding.getRoot().findViewById(R.id.sign_out);
        signOutButton.setOnClickListener(view -> auth.signOut());

        return root;
    }

@Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}