package com.example.letmecook.db_tools;

import android.content.Context;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Recipes {
    private final SearchDB searchDB = new SearchDB();
    private final Context context;

    public Recipes(Context context) {this.context = context;}

    public List<DocumentSnapshot> displayRecipes(String name, String author, String cuisine, List<String> ingredients, int page) {
        List<DocumentSnapshot> recipes = new ArrayList<>();
        searchDB.filterRecipes(name, author, cuisine, ingredients, recipes::addAll);
        recipes = recipes.subList(page-1, page+6);
        return recipes;
    }

}
