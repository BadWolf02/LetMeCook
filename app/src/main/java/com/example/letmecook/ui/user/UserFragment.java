package com.example.letmecook.ui.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.letmecook.FavouriteViewActivity;
import com.example.letmecook.HouseholdManageActivity;
import com.example.letmecook.ViewInviteActivity;
import com.example.letmecook.db_tools.Authentication;

import com.example.letmecook.R;
import com.example.letmecook.databinding.FragmentUserBinding;

import androidx.lifecycle.ViewModelProvider;
import com.example.letmecook.EditProfileActivity;
import com.example.letmecook.db_tools.Household;

import android.widget.ImageView;
import android.widget.Toast;

public class UserFragment extends Fragment {

    private FragmentUserBinding binding;
    private UserViewModel userViewModel;
    private ImageView profilePicture;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        Household household = new Household(requireContext());

        Authentication auth = new Authentication(requireContext());

        binding = FragmentUserBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        profilePicture = binding.profilePicture;
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserProfile", requireContext().MODE_PRIVATE);
        int savedDrawable = prefs.getInt("profile_picture", R.drawable.ic_profile_picture_placeholder);
        profilePicture.setImageResource(savedDrawable);

        //User profile & edit
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

        Button favouriteButton = binding.getRoot().findViewById(R.id.favouriteButton);
        favouriteButton.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), FavouriteViewActivity.class);
            startActivity(intent);
        });

        Button signOutButton = binding.getRoot().findViewById(R.id.sign_out);
        signOutButton.setOnClickListener(view -> auth.signOut());

        return root;
    }
    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserProfile", requireContext().MODE_PRIVATE);
        int savedDrawable = prefs.getInt("profile_picture", R.drawable.ic_profile_picture_placeholder);
        profilePicture.setImageResource(savedDrawable);
    }

@Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}