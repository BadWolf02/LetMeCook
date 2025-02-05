package com.example.letmecook.ui.recipes;

import android.os.Bundle; //Passes data to the fragment and restores its state after config changes
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater; // handles the XML layout file into a View object
import android.view.View;
import android.view.ViewGroup; // View object that gets displayed by the fragment
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull; //Arguments or return values that cannot be null
import androidx.fragment.app.Fragment; //base class for the fragment
import androidx.lifecycle.ViewModelProvider; //managing ViewModelProvide

import com.example.letmecook.R;
import com.example.letmecook.databinding.FragmentRecipesBinding;

public class RecipesFragment extends Fragment {

private FragmentRecipesBinding binding; // binding object allows interaction with views

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRecipesBinding.inflate(inflater, container, false);
        RecipesViewModel recipesViewModel =
                new ViewModelProvider(this).get(RecipesViewModel.class);

        final TextView textView = binding.textRecipes;
        recipesViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);



    View root = binding.getRoot();


        recipesViewModel = new ViewModelProvider(this).get(RecipesViewModel.class);
        EditText edit_r_name = binding.textViewRecipeName;
        recipesViewModel.getText().observe(getViewLifecycleOwner(), edit_r_name::setText);
        edit_r_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
              //  edit_r_name.setText(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        return root;




    }





@Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}