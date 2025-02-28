package com.example.letmecook.ui.user;

import android.content.Intent;
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


public class UserFragment extends Fragment {

    private FragmentUserBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {

        Authentication auth = new Authentication(requireContext());

        binding = FragmentUserBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}