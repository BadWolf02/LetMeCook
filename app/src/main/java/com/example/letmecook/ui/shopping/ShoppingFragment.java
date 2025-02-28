package com.example.letmecook.ui.shopping;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.SearchView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.letmecook.R;
import com.example.letmecook.databinding.FragmentShoppingBinding;
import com.example.letmecook.db_tools.SearchDB;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ShoppingFragment extends Fragment {

    private FragmentShoppingBinding binding;

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final SearchDB searchDB = new SearchDB();

    private LinearLayout shoppingListLayout;

    private final ArrayList<String> ingredients = new ArrayList<>();
    private final List<String> shoppingList = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentShoppingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize UI components
        SearchView itemBar = binding.itemBar;
        shoppingListLayout = binding.shoppingListLayout;

        Button showMenuButton = binding.showMenuButton;

        ListPopupWindow listPopupWindow = new ListPopupWindow(requireContext());
        listPopupWindow.setAnchorView(showMenuButton); // Anchor view to menu button

        // Fetch shopping list items
        searchDB.getUserShoppingList(mAuth.getCurrentUser().getUid(), foundShoppingList -> {
            shoppingList.addAll(foundShoppingList);
            addItem();
        });

        // Create an adapter with the menu items
        searchDB.getIngredients(foundIngredients -> {
            ingredients.clear();
            ingredients.addAll(foundIngredients);

            // Create an adapter to hold all ingredients in dropdown
            ArrayAdapter<String> menuAdapter = new ArrayAdapter<>(requireContext(),
                    R.layout.ingredient_dropdown_item, ingredients);
            listPopupWindow.setAdapter(menuAdapter);
        });

        itemBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null && !query.isEmpty()) {
                    shoppingList.add(query);
                    addToShoppingList(query);
                    itemBar.clearFocus();
                    addItem();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // https://m2.material.io/components/menus/android#dropdown-menus
        // TODO fix bug where some items cannot be clicked
        listPopupWindow.setOnItemClickListener((parent, view, position, id) -> {
            // Add item to filteredIngredients on click
            String selectedItem = ingredients.get(position);
            if (!shoppingList.contains(selectedItem)) {
                shoppingList.add(selectedItem);
                addToShoppingList(selectedItem);
                addItem();
            }
            listPopupWindow.dismiss(); // Dismiss the popup after selection
        });

        // Show the ListPopupWindow when the button is clicked
        showMenuButton.setOnClickListener(v -> {
            if (!ingredients.isEmpty()) {
                listPopupWindow.show();
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Handle displaying ingredients that are selected for filter
    @SuppressLint("SetTextI18n")
    private void addItem() {
        shoppingListLayout.removeAllViews();
        for (String ingredient : shoppingList) {
            TextView ingredientView = new TextView(requireContext());
            ingredientView.setText("- " + ingredient);
            ingredientView.setTextSize(14);
            ingredientView.setPadding(8,8,8,8);
            ingredientView.setTextColor(Color.BLACK);

            Button removeButton = new Button(requireContext());
            removeButton.setText("Remove");
            removeButton.setTextSize(14);
            removeButton.setPadding(4,4,4,4);
            removeButton.setTextColor(Color.BLACK);
            removeButton.setBackgroundColor(Color.parseColor("#CCD5AE"));
            removeButton.setOnClickListener(v -> {
                shoppingList.remove(ingredient);
                removeFromShoppingList(ingredient);
                addItem(); // Refresh the list
            });

            LinearLayout listItemLayout = new LinearLayout(requireContext());
            listItemLayout.setOrientation(LinearLayout.HORIZONTAL);
            listItemLayout.addView(ingredientView);
            listItemLayout.addView(removeButton);
            shoppingListLayout.addView(listItemLayout);
        }
    }

    private void addToShoppingList(String ingredient) {
        searchDB.getUserHouseholdDocument(mAuth.getUid(), householdDocument ->
                householdDocument.getReference().update("shopping-list", FieldValue.arrayUnion(ingredient))
        );
    }

    private void removeFromShoppingList(String ingredient) {
        searchDB.getUserHouseholdDocument(mAuth.getUid(), householdDocument ->
                householdDocument.getReference().update("shopping-list", FieldValue.arrayRemove(ingredient))
        );
    }
}