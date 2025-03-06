package com.example.letmecook;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//import com.example.letmecook.tools.Firebase;
import com.example.letmecook.db_tools.SearchDB;
import com.google.firebase.firestore.*;



public class Recipe {

    //TODO use not null for required fields??

    public String r_name; //get this from a field
    public String r_id; //autogenerate by firebase db, make sure to add this to a list of IDs that the user has, so they can view their own recepies
    public String author; // username or official/ the site we scraped it from
    public String r_type; // can be public, private or official
    public String cusine; //TODO make this similar to ingredients where you can only add valid stuff
    public Integer cooking_time_h;
    public Integer cooking_time_min;
    public Integer total_time_h;

    public Integer total_time_min;
    HashMap<String, Integer> timings = new HashMap<>();
    // public Integer  prep_time;
    public HashMap<String, Object> ingredients = new HashMap<>();

    public ArrayList<String> mealType;

    public ArrayList<String> allergens = new ArrayList<String>();

    public HashMap<String, String> steps = new HashMap<>(); //TODO make sure to initialize all the other variables too in order to be able to use them in the methods without causing null pointer exceptions
    private int stepsAmount;

    SearchDB search_db = new SearchDB();

    FirebaseFirestore db = FirebaseFirestore.getInstance();


    // protected Firebase db = new Firebase(Context Recipe.this); //probably worng, want it to take the context from the add recipe or display recipe

    public Recipe() {
        stepsAmount = 0;
    }

    //TODO ratings

    public void create_recepie(){ }

    public void setMealType(ArrayList<String> mealTypes){
        this.mealType = mealTypes;
    }

    public ArrayList getMealType(){
        return this.mealType;
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

    public void setCooking_time_h(Integer cooking_time){
        this.cooking_time_h = cooking_time;
    }
    public void setCooking_time_min(Integer cooking_time){
        this.cooking_time_min = cooking_time;
    }
    public Integer getCooking_time_h(){
        return this.cooking_time_h;
    }
    public Integer getCooking_time_min(){
        return this.cooking_time_min;
    }

    public void setTotal_time_h(int cooking_time){
        this.total_time_h = cooking_time;
    }
    public void setTotal_time_min(int cooking_time){
        this.total_time_min = cooking_time;
    }
    public Integer getTotal_time_h(){
        return this.total_time_h;
    }
    public Integer getTotal_time_min(){
        return this.total_time_min;
    }


    public void setCusine(String cusine){
        this.cusine = cusine;
    }

    public String getCusine(){
        return this.cusine;
    }

    //TODO maybe add a meal type (cold/ warm/ breakfast/ lunch/dinner/ sallad/ snacks/ light meal)

    public void addIngredient(String i_name, HashMap details){
        Log.d("ingredients in recipe class", i_name + details.toString());
        this.ingredients.put(i_name, details);

        search_db.getIngredientDocumentAllergens( i_name, i_allergens -> {
            Log.d("ferched allergens for "+i_name, i_allergens.toString());
            if (!i_allergens.isEmpty() && i_allergens!= null){
                updateAllergens(i_allergens);
            }
});
        //TODO fetch allergens and update it here, still have to change add allergens
        //this.updateAllergens(allergen);
    }

    private void updateAllergens(List<String> i_allergens) {
        for (String allergen : i_allergens) {
            if (!this.allergens.contains(allergen)) {
                this.allergens.add(allergen);
                Log.d("allergens list: ", this.allergens.toString());
            }
        }
    }

    public void addStep(Integer id, String text){
       this.stepsAmount += 1; //TODO take this and use it to hashmap instead of adding new thing to array
        this.steps.put(id.toString(), text.toString());
    }


    public void favourite_recipe(String u_id){
        // add r_id to the users favourites list
    }

    public void write_review(){ //TODO only allow after user has cooked recepie??

    }

    public void add_photo(){ //use this to add photo fo finished recepie but also to writing comments and allow for steps

    }

    public void cook_recipe(){} //TODO probably makes more sense to add this function somewhere else

    public void clear_total_time(){ //TODO do this next, have to add on click listener that calls these
        this.total_time_h = null;
        this.total_time_min = null;
    }
    public void clear_cooking_time(){
        this.total_time_h = null;
        this.total_time_min = null;
    }

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

            CollectionReference recipesRef = db.collection("recipes");

            HashMap<String, Object> recipe = new HashMap<>();
            recipe.put("r_name", this.r_name);
            // recipe.put("author", this.author);
            recipe.put("ingredienets", this.ingredients);
            Log.d("adding allergens to hashmap for recipies", this.allergens.toString()); //  this is currently a empty list
            recipe.put("allergens", this.allergens);
            recipe.put("steps", this.steps);
            // recipe.put("r_type", this.r_type);
        
            if (this.cusine!=null){
                recipe.put("cusine", this.cusine);
            }
            if (getMealType()!=null){
                recipe.put("meal type", this.mealType);
            }

            //check if the total cooking time has been set
            if ((this.cooking_time_min != null) | (this.cooking_time_h != null) | (this.total_time_h != null) | (this.total_time_min != null)){

                if (this.cooking_time_h != null) {
                    this.timings.put("cooking_time_min", this.cooking_time_min);
                }
                if(this.cooking_time_min != null){
                    this.timings.put("cooking_time_min", this.cooking_time_min);
                }
                if (this.total_time_min != null){
                    this.timings.put("total_time_min", this.total_time_min);
                }
                if (this.total_time_h != null){
                    this.timings.put("total_time_h", this.total_time_h);
                }
            }



            recipesRef.add(recipe);
                //Toast.makeText(context , "recipe sucessfully saved", Toast.LENGTH_LONG) ;}).addOnFailureListener(e->{System.err.println("adding recipe failed");});
            //TODO figure out how to get this to work, can't really have the context here
    };


        // check if cusine has been filled in and add that
        // check if cook_time has been filled in and add
        // check if photos have in any steps been filled in and add that
    }

    //TODO add method to go through


  //  public void getRecipeFromDb(String r_id){
        // access db and fill in all the fileds here

   // }

