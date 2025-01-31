package com.example.letmecook.ui.recipe;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.letmecook.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NameRecipeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NameRecipeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public NameRecipeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NameRecipeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NameRecipeFragment newInstance(String param1, String param2) {
        NameRecipeFragment fragment = new NameRecipeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_recipe_name, container, false);
    }

    public void setR_name(View v){
        TextView r_name_field = getView().findViewById(R.id.recipe_name_field);
        View r_name_confirm_btn = getView().findViewById(R.id.name_recipe_button);
        r_name_confirm_btn.setOnClickListener(l ->
            { String r_name = r_name_field.getText().toString();});

    }
}