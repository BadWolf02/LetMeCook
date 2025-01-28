package com.example.letmecook.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.letmecook.tools.Firebase;
import com.google.firebase.auth.*;

import com.example.letmecook.LoginActivity;
import com.example.letmecook.R;
import com.example.letmecook.databinding.FragmentAccountBinding;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        AccountViewModel accountViewModel =
                new ViewModelProvider(this).get(AccountViewModel.class);

        Firebase db = new Firebase(requireContext());

        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textAccount;
        accountViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        Button signOutButton = binding.getRoot().findViewById(R.id.sign_out);
        signOutButton.setOnClickListener(view -> {
            db.signOut();
        });

        return root;
    }

@Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    /*
    public void signOut() {
        // Navigate to LoginActivity
        // TODO implement actual sign out using Firebase. This is simple navigation.

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
    }
    */
}