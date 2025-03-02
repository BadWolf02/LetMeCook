package com.example.letmecook.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import java.util.List;

public class RecipeAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final List<String> recipeTitles;
    private final List<String> recipeDetails;

    public RecipeAdapter(Context context, List<String> titles, List<String> details) {
        super(context, android.R.layout.simple_list_item_1, titles);
        this.context = context;
        this.recipeTitles = titles;
        this.recipeDetails = details;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(recipeTitles.get(position));

        convertView.setOnClickListener(v -> showRecipeDetails(position));
        return convertView;
    }

    private void showRecipeDetails(int position) {
        new AlertDialog.Builder(context)
                .setTitle(recipeTitles.get(position))
                .setMessage(recipeDetails.get(position))
                .setPositiveButton("OK", null)
                .show();
    }
}
