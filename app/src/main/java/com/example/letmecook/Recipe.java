package com.example.letmecook;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

//import com.example.letmecook.tools.Firebase;
import com.google.firebase.firestore.*;


public class Recipe {

    //TODO use not null for required fields??

    public String r_name; //get this from a field
    public String r_id; //autogenerate by firebase db, make sure to add this to a list of IDs that the user has, so they can view their own recepies
    public String author; // username or official/ the site we scraped it from
    public String r_type; // can be public, private or official
    public String cusine; //TODO make this similar to ingredients where you can only add valid stuff
    public String cooking_time;
    public String total_time;
    public String prep_time;
    public ArrayList<String> ingredients;

    public Integer allergens;

    public ArrayList<String> steps = new ArrayList<>(); //TODO make sure to initialize all the other variables too in order to be able to use them in the methods without causing null pointer exceptions
    private int stepsAmount;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    // protected Firebase db = new Firebase(Context Recipe.this); //probably worng, want it to take the context from the add recipe or display recipe

    public Recipe() {
        stepsAmount = 0;
    }

    //TODO ratings

    public void create_recepie(){

    }

    public Integer getStepsAmount (){
        return this.stepsAmount;
    }

    public void incrementStepsAmount(){
        this.stepsAmount += 1;
    }

    public void setR_name(String name){
        if (name.length()<=40) {
            this.r_name = name;
        }
        // else redirect to same thing with error message but that would violate the keeping different layers seperate principle??
    }


    public String getR_name(){
        return this.r_name;
    }

    public void setAuthor(String author){
        this.author = author;
    }

    public String getAutor(){
        return this.author;
    }

    public void setR_type(String r_type){
        this.r_type = r_type;
    }

    public String getR_type(){
        return this.r_type;
    }

    public void setCooking_time(String cooking_time){
        this.cooking_time = cooking_time;
    }
    public String getCooking_time(){
        return this.cooking_time;
    }

    public void setCusine(String cusine){
        this.cusine = cusine;
    }

    public String getCusine(){
        return this.cusine;
    }

    //TODO maybe add a meal type (cold/ warm/ breakfast/ lunch/dinner/ sallad/ snacks/ light meal)

    public void addIngredient(String r_name, Integer allergens){
        // TODO check ingredient for allergen
        this.ingredients.add(r_name);
        this.updateAllergens(allergens);
    }

    private void updateAllergens(Integer allergens){
        this.allergens = this.allergens | allergens;
    }

    public void addStep(String text){
        this.stepsAmount += 1;
        StringBuilder step = new StringBuilder();
        step.append(text).append(String.valueOf(stepsAmount)).toString();
        if ( step.toString() == ""){
            Log.d("no string", "no step to add");
        }

        this.steps.add(step.toString());
    }


    public void favourite_recipe(String u_id){
        // add r_id to the users favourites list
    }

    public void write_review(){ //TODO only allow after user has cooked recepie??

    }

    public void add_photo(){ //use this to add photo fo finished recepie but also to writing comments and allow for steps

    }

    public void cook_recipe(){} //TODO probably makes more sense to add this function somewhere else

    public void create(){

        // autofill author
        // check that all the mandatory fields have been filled
        // add new entry to the Recipes dB
        // redirect user to the finished recipe page
        // set r_type
       //  if ( this.r_name!=null && this.steps!=null && this.ingredients!=null && this.r_type!=null){
            // the stuff to respective fielsds
            // add author and allergens too
            // access recipies collection
            boolean success;
            CollectionReference recipesRef = db.collection("recipes");

            HashMap<String, Object> recipe = new HashMap<>();
            recipe.put("r_name", this.r_name);
            recipe.put("author", this.author);
            recipe.put("ingredienets", this.ingredients);
            recipe.put("steps", this.steps);
            recipe.put("r_type", this.r_type);
            recipe.put("allergens", this.allergens);
            if (this.cooking_time!=null) {
                recipe.put("cooking_time", this.cooking_time);
            }
            if (this.total_time!=null){
                recipe.put("total_time", this.total_time);
            }
            if (this.cusine!=null){
                recipe.put("cusine", this.cusine);
            }
            recipesRef.add(recipe); //TODO next: all on sucess listener and make sure it dosn't create after each new letter
            //.addOnSuccessListener(documentReference -> {
                //Toast.makeText(context , "recipe sucessfully saved", Toast.LENGTH_LONG) ;}).addOnFailureListener(e->{System.err.println("adding recipe failed");});
            //TODO figure out how to get this to work, can't really have the context here
        }

        // check if cusine has been filled in and add that
        // check if cook_time has been filled in and add
        // check if photos have in any steps been filled in and add that
    }

    //TODO add method to go through


  //  public void getRecipeFromDb(String r_id){
        // access db and fill in all the fileds here

   // }




