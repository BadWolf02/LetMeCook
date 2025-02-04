package com.example.letmecook.ui.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.letmecook.tools.Firebase;

import com.example.letmecook.R;
import com.example.letmecook.databinding.FragmentUserBinding;

public class UserFragment extends Fragment {

    private FragmentUserBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        UserViewModel userViewModel =
                new ViewModelProvider(this).get(UserViewModel.class);

        Firebase db = new Firebase(requireContext());

        binding = FragmentUserBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textSearchMember;
        userViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // TODO implement checkbox / force user to click on the household first before inviting
        EditText householdInput = binding.getRoot().findViewById(R.id.householdInput);
        EditText searchBar = binding.getRoot().findViewById(R.id.searchBar);
        Button searchButton = binding.getRoot().findViewById(R.id.searchButton);
        searchButton.setOnClickListener(view -> db.inviteUser(householdInput.getText().toString(), searchBar.getText().toString()));

        Button signOutButton = binding.getRoot().findViewById(R.id.sign_out);
        signOutButton.setOnClickListener(view -> db.signOut());


        return root;
    }

@Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}