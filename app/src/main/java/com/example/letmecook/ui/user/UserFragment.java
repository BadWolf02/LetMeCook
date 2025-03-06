package com.example.letmecook.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.letmecook.HouseholdManageActivity;
import com.example.letmecook.EditProfileActivity;
import com.example.letmecook.ViewInviteActivity;
import com.example.letmecook.db_tools.Authentication;

import com.example.letmecook.R;
import com.example.letmecook.databinding.FragmentUserBinding;
import com.example.letmecook.db_tools.Household;


public class UserFragment extends Fragment {

    private FragmentUserBinding binding;
    private UserViewModel userViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        UserViewModel userViewModel =
                new ViewModelProvider(this).get(UserViewModel.class);

        Authentication auth = new Authentication(requireContext());
        Household household = new Household(requireContext());

        binding = FragmentUserBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        userViewModel.getUserData().observe(getViewLifecycleOwner(), userData -> {
            binding.userName.setText(userData.getOrDefault("username", "Username not found"));
            binding.userEmail.setText(userData.getOrDefault("email", "Email not found"));
            binding.userHousehold.setText("Household ID: " + userData.getOrDefault("householdID", "N/A"));
        });

        userViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        // final TextView textView = binding.textUser;
        // userViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        binding.editProfile.setOnClickListener(view -> {
            Intent intent = new Intent(requireActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        Button householdButton = binding.getRoot().findViewById(R.id.householdButton);
        householdButton.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), HouseholdManageActivity.class);
            startActivity(intent);
        });

        Button inviteButton = binding.getRoot().findViewById(R.id.inviteButton);
        inviteButton.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), ViewInviteActivity.class);
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