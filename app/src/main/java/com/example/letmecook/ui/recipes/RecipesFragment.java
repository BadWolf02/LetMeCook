package com.example.letmecook.ui.recipes;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle; //Passes data to the fragment and restores its state after config changes
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater; // handles the XML layout file into a View object
import android.view.View;
import android.view.ViewGroup; // View object that gets displayed by the fragment
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider; //managing ViewModelProvide

import com.example.letmecook.R;
import com.example.letmecook.models.Recipe;
import com.example.letmecook.WebScrapingActivity;
import com.example.letmecook.databinding.FragmentRecipesBinding;
import com.example.letmecook.db_tools.SearchDB;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RecipesFragment extends Fragment {

    private FragmentRecipesBinding binding; // binding object allows interaction with views
    public Recipe recipe = new Recipe();

 //   private SearchDB db = new SearchDB();

    private final ArrayList<Object> steps = new ArrayList<>();

    private final ArrayList<Integer> steps_list = new ArrayList<>();

    HashMap<String, HashMap<String, String>> ingredients_names_list = new HashMap<>();

    private int next_step_index = 5; // TODO change this to be fetched dynamically

    private ImageView recipeImageView;

    private ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        if (recipeImageView != null) {
                            recipeImageView.setImageURI(imageUri);
                        } else {
                            Toast.makeText(getContext(), "Error: ImageView not initialized!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to select image", Toast.LENGTH_SHORT).show();
                    }
                }
            });



    @SuppressLint({"ClickableViewAccessibility", "DefaultLocale"})
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRecipesBinding.inflate(inflater, container, false);
        RecipesViewModel recipesViewModel =
                new ViewModelProvider(this).get(RecipesViewModel.class);

        final TextView textView = binding.textRecipes;
        recipesViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // add the default first edit text box id to the list of step ids
        // steps_list.add("EditText_add_step1");

        recipeImageView = binding.recipeImageView;

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            recipeImageView.setImageURI(imageUri);
                            uploadImageToFirebase(imageUri);
                        } else {
                            Toast.makeText(getContext(), "Failed to select image", Toast.LENGTH_SHORT).show();
                        }
                    }
                    recipeImageView.setVisibility(View.VISIBLE);
                });

        View root = binding.getRoot();


    binding.scrapeRecipesButton.setOnClickListener(v -> scrapeRecipes());
    binding.selectImageButton.setOnClickListener(v -> openGallery());

        recipesViewModel = new ViewModelProvider(this).get(RecipesViewModel.class);

        /**
         * handle user entering recipe name
         */
        Log.d("on Create View", "reached part before edit text");
        EditText edit_r_name = binding.editTextRecipeName;
        edit_r_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d("recipe name", "");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("recipe name", s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("recipe name", "name is "+s.toString());
                String name = edit_r_name.getText().toString();
                Log.d("recipe name", "name is "+ name);
                recipe.setR_name(s.toString());
            }
        });




        //TODO do this next, adding steps working and saving however its saving the actual objects not the text from the objects so change that

        // add recipe step
        // EditText stp1 = binding.EditTextAddStep1;
        int stp1_id = R.id.EditText_add_step1;
        steps_list.add(stp1_id);
        Button add_step_btn = binding.addStepBtn;
        add_step_btn.setOnClickListener(v -> {
                    // public void add_recipe_step(){
                    // change the entered text to a text box??
                    // add a new step text field
                    // allow the user to somehow still eddit the submitted step
                    EditText add_step_text_field = getView().findViewById(R.id.EditText_add_step1); // may have to bind this since it is called from inside oncreate
                    String add_step_text = add_step_text_field.getText().toString();
                    if ( add_step_text.trim() != "") {
                        Log.d("empty string", add_step_text);
                        steps.add(add_step_text);
            }
                    Log.d("empty string", add_step_text); //TODO remove this


                    // add this to db either save straight to a map here or to the recipe cals
                    // dynamically create another edit text
                    int step_id = View.generateViewId();
                    // dynamically create new recipe step box
                    EditText add_step_box = new EditText(getContext());
                    add_step_box.setId(step_id); //TODO
                    add_step_box.setHint("add Step");
                    ScrollView scrollView = getView().findViewById(R.id.scrollView);
                    LinearLayout layout = scrollView.findViewById(R.id.layout); // .layout //TODO figure out what needs to be here instead of dynamic layout? linear maybe
                    LinearLayout.LayoutParams editbox_params = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    add_step_box.setLayoutParams(editbox_params);
                    // steps.add(add_step_box);
                    steps_list.add(step_id);
                    // TODO add the index (starting at 0 of where we want to add the new edit view -> see if I can update this dynamically too depending on what is above
                    layout.addView(add_step_box, next_step_index);
                    next_step_index ++;
                    Log.e(TAG, "trying to add field");
                    // increment step_no when saved
                    recipe.incrementStepsAmount();

                    // TODO get it to autoscroll -> actually autoscroll is fine, it is just uner the bottom nav bar so that needs to be fixed in the activity
                });

        // }

//
//        // add allergens //TODO contionue here next, its not working properly
//        Log.e("ingredients", "reached on create");
//        MultiAutoCompleteTextView add_ingredients = binding.addIngreedientsMultiAtotComplete;
//        ArrayList<Object> ingredientsList = new ArrayList<>();
//        db.getIngredients(new SearchDB.IngredientsCallback() {
//            @Override
//            public void onIngredientsLoaded(Object[] ingredients) {
//                ingredientsList.clear();
//                ingredientsList.addAll();
//                ingredients_adapter.notifyDataSetChanged();
//            }
//        });
//        add_ingredients.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d("ingredients", ingredientsList.toString());
//                ArrayAdapter<Object> ingredients_adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, ingredientsList);
//                add_ingredients.setAdapter(ingredients_adapter);
//                add_ingredients.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
//            }
//        });


        // get cuisine from selection and set it as cuisine of the recipe
        Spinner cuisine_spinner = binding.cuisineDropdown;
        cuisine_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id){
                String cuisine = parent.getItemAtPosition(pos).toString();
                recipe.setcuisine(cuisine);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent){

            }
        });
        //set Spinner for cuisine
        ArrayAdapter<CharSequence> cuisine_adapter = ArrayAdapter.createFromResource(this.requireContext(), R.array.cuisine_dropdown, android.R.layout.simple_spinner_dropdown_item);
        cuisine_adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        Log.d("spinner", "set dropdown view");
        // Spinner cuisine_spinner = getView().findViewById(R.id.cuisine_dropdown); //TODO causing error, null pointer exception, try binding instead of get view by id
        Log.d("spinner", "got spinner view by id");
        cuisine_spinner.setAdapter(cuisine_adapter);
        Log.d("spinner", "set up adapter");


        // dropdown for meal Type
        Spinner meal_type_dropdown_trigger = binding.mealTypeDropdownTrigger;
        //TODO add list and selectiontracker
        String[] meal_types = {"breakfast", "lunch", "dinner", "warm meal", "snack", "cold meal", "starter", "desert", "salad", "soup"}; //list of meal types
        boolean[] selected_meal_type_tracker = new boolean[meal_types.length]; //track which items are selected
        ArrayList<String> selected_meal_types = new ArrayList<>();
        selected_meal_types.add("please select meal type");
        //add onClickListener and handle event
        ArrayAdapter<String> meal_type_adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, selected_meal_types);
        meal_type_dropdown_trigger.setAdapter(meal_type_adapter);
//        ArrayAdapter meal_type_adapter = new ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, selected_meal_types);
//        meal_type_adapter.

        meal_type_dropdown_trigger.setOnTouchListener((v, event) ->{

            // prevent multiple dialogs being opened at the same time
            meal_type_dropdown_trigger.setEnabled(false);

            AlertDialog.Builder meal_type_dropdown_builder = new AlertDialog.Builder(requireContext());
            meal_type_dropdown_builder.setTitle("Meal type");

            meal_type_dropdown_builder.setMultiChoiceItems(meal_types, selected_meal_type_tracker, ((dialog, which, isChecked) -> {
                if (isChecked) {
                    selected_meal_types.add(meal_types[which]);
                }
                else {
                    selected_meal_types.remove(meal_types[which]);
                }
            }));
            meal_type_dropdown_builder.setPositiveButton("confirm", ((dialog, which) -> {
                meal_type_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                meal_type_dropdown_trigger.setAdapter(meal_type_adapter);//meal_type_adapter((TextUtils.join(", ", selected_meal_types)));
                recipe.setMealType(selected_meal_types);
                meal_type_dropdown_trigger.setEnabled(true);
                dialog.dismiss();
        }));
            //TODO chane this to unselect and enter none
            meal_type_dropdown_builder.setNegativeButton("clear", (dialog, whick) -> {
                recipe.setMealType(null);
                meal_type_dropdown_trigger.setEnabled(true);
                dialog.dismiss();
            });
            AlertDialog meal_type_dropdown = meal_type_dropdown_builder.create();
            meal_type_dropdown.show();

            return true;
        });


        // total time h and min selection handler
        NumberPicker total_time_h_picker = binding.totalTimeH;
        NumberPicker total_time_min_picker = binding.totalTimeMin;
        Button total_time_clear_btn = binding.totalTimeClearBtn;


        total_time_h_picker.setMinValue(0);
        total_time_h_picker.setMaxValue(100);
        total_time_h_picker.setFormatter(i -> String.format("%02d", i));

        total_time_min_picker.setMinValue(0);
        total_time_min_picker.setMaxValue(59);
        total_time_min_picker.setFormatter(i -> String.format("%02d", i));

        total_time_h_picker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            recipe.setTotal_time_h(newVal);
        });

        total_time_min_picker.setOnValueChangedListener(((picker, oldVal, newVal) -> {
            recipe.setTotal_time_min(newVal);
        }));
        total_time_clear_btn.setOnClickListener(v -> {recipe.clear_total_time();});


        // total time h and min selection handler
        NumberPicker cooking_time_h_picker = binding.cookingTimeH;
        NumberPicker cooking_time_min_picker = binding.cookingTimeMin;
        Button cooking_time_clear_btn = binding.cookingTimeClearBtn;

        cooking_time_h_picker.setMinValue(0);
        cooking_time_h_picker.setMaxValue(100);
        cooking_time_h_picker.setFormatter(i -> String.format("%02d", i));

        cooking_time_min_picker.setMinValue(0);
        cooking_time_min_picker.setMaxValue(59);
        cooking_time_min_picker.setFormatter(i -> String.format("%02d", i));


        cooking_time_h_picker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            recipe.setCooking_time_h(newVal);
        });

        cooking_time_min_picker.setOnValueChangedListener(((picker, oldVal, newVal) -> {
            recipe.setCooking_time_min(newVal);
        }));

        cooking_time_clear_btn.setOnClickListener(v -> {
            recipe.clear_cooking_time();
        });

        Button create_recipe_btn = binding.addRecipeBtn;
        create_recipe_btn.setOnClickListener(v -> {
            Log.d("adding step", "about to add lastep");
            add_steps_to_recipe();
            add_ingredients_to_recipe();
           recipe.create();
        });





        return root;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceStates){
        super.onViewCreated(view, savedInstanceStates);
        Log.e("ingredients", "reached on create");
        MultiAutoCompleteTextView add_ingredients = binding.addIngreedientsMultiAtotComplete;
        SearchDB db = new SearchDB();
        // String[] ingredientsList =
        final ArrayList<Object>[] ingredients_list = new ArrayList[]{new ArrayList<>()};
        db.getIngredientsList(new SearchDB.IngredientsCallback() {
        @Override
            public void onIngredientsLoaded(ArrayList ingredients){
                if (!ingredients.isEmpty()){
                    ArrayAdapter<String> ingredients_adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, ingredients);
                    add_ingredients.setAdapter(ingredients_adapter);
                    add_ingredients.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
                    Log.d("selected ingreedients", ingredients_adapter.toString());
                    ingredients_list[0] = ingredients;
                }
        }
        });
        Log.d("ingreedients", ingredients_list.toString());
        //todo still need to get it to show and add amount

        // detecting when new item has been clicked (and thus selected to list of ingredients)
        add_ingredients.setOnItemClickListener((AdapterView<?> parent, View clicked, int index, long id) -> {
            String selected_item = (String) parent.getItemAtPosition(index);
            AlertDialog.Builder ingredient_details_builder = new AlertDialog.Builder(requireContext());
            ingredient_details_builder.setTitle(selected_item + " details:");


            //TODO create view to hold what is displayed in alert dialog
            LinearLayout ingredient_details_layout = new LinearLayout(requireContext());
            ingredient_details_layout.setOrientation(LinearLayout.VERTICAL);

//            //create Textview detailing ingredient
//            TextView ingredient_name = new TextView(requireContext());
//            ingredient_name.setText(selected_item);
//            //add textView to layout
//            ingredient_details_layout.addView(ingredient_name);


            //create EditText to enter ingredient amount
            EditText ingredient_amount = new EditText(requireContext());
            ingredient_amount.setHint("enter amount of " + selected_item);
            ingredient_amount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            // add Edit text to layout
            ingredient_details_layout.addView(ingredient_amount);

            //TODO add spinner for amount type and delete the functions that used to do that
            Spinner amount_type = new Spinner(requireContext());
            final String[] amount = new String[1];
            amount_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id){
                    amount[0] = parent.getItemAtPosition(pos).toString();
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent){
                    amount_type.setPrompt("amount");
                    amount[0] ="grams";
                }
            });

            Log.d("ingredients", "1");
            String[] amount_types_list = {"grams", "kilograms", "ml", "liters", "tsp", "Tbsp", "cups", "ounces", "pounds", "piece"};
            ArrayAdapter<String> amount_adapter = new ArrayAdapter<>(requireContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, amount_types_list);
            amount_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            amount_type.setAdapter(amount_adapter);
            amount_type.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            ingredient_details_layout.addView(amount_type);
            // put view in alert dialog

            ingredient_details_builder.setView(ingredient_details_layout);

            ingredient_details_builder.setPositiveButton("Confirm", (dialog, which) -> {
                HashMap<String, String> details = new HashMap<>();
                details.put("amount", ingredient_amount.getText().toString());
                details.put("amount_type", amount[0]);
                Log.d("ingredient",  ingredient_amount.getText().toString());
                Log.d("details", amount[0]);
                ingredients_names_list.put(selected_item, details);
            });
            //  ingredient_details_builder.setNegativeButton("Cancel", )

            AlertDialog ingredients_dialog = ingredient_details_builder.create();
            ingredient_details_builder.show();

            Log.d("ingredients", "2");
            ingredients_dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,  // Full width
                    ViewGroup.LayoutParams.WRAP_CONTENT  // Adjust height based on content
            );
            Log.d("ingredients", "reached call for add ingredients method");

        });

        //TODO still need to add to db
        //TODO need to update recipe allergens depending on this
        //TODO this technically also lets u type stuff that isn't in our ingredients
    }
  //  add recipe step
   //  :( this won't work if there has been nothing entered and I try to do a toString it causes a null pointer exception
    // nvm causes a null pointer exception anyway
    // probs have to move this to above return root but if I do that I would have to move it all to after the create() mcall making it really messy
    public void add_steps_to_recipe(){
        Log.d("adding step", "add last step method reached");
        if (!steps_list.isEmpty()) {
            for (int step_id : steps_list) {
                EditText stp = getView().findViewById(step_id);
                Editable stp_text = stp.getText();
                if (stp_text != null && stp_text.length() > 0) {
                    recipe.addStep(step_id, stp_text.toString());
                }
            }
        }
    }

    public void add_ingredients_to_recipe(){
        List<String> current_ingredients_list =  Arrays.asList(binding.addIngreedientsMultiAtotComplete.getText().toString().split(", "));
    //    Log.d("ingredients currently selected", current_ingredients.getClass().toString());
    //    String[] current_ingredients_list = current_ingredients.replaceAll(",$", "").split(",\\s*");
        Log.d("ingredients currently selected", current_ingredients_list.toString());
        for (String ingredient : current_ingredients_list){
            Log.d("ingredients individula", ingredient);
            HashMap<String, String> details = this.ingredients_names_list.get(ingredient); //TODO causeing crash
            Log.d("ingredients details", this.ingredients_names_list.get(ingredient).toString()); // details.toString());
            Log.d("adding ingredient for ", ingredient);
            recipe.addIngredient(ingredient, details);
        }

        //TODO here next: check what the format is and turn to array
    }
//
//    public void add_ingredients_details(String ingredient_name, FragmentRecipesBinding binding){
//        AlertDialog.Builder add_ingreedient_builder =  new AlertDialog.Builder(requireContext());
//        LayoutInflater ingredients_inflater = requireActivity().getLayoutInflater();
//        //View ingredient_view_layout = ingredients_inflater.inflate(R.id.contain_ingredient_view);
//        // LayoutInflater inflater = requireActivity().getLayoutInflater();
//        FragmentRecipesBinding ingredient_view = binding.inflate(R.id.ingredient_layout);
//        // create_ingredient_amount_selector(ingredient_name, ingredient_view); //TODO get this up into the same function
//        AlertDialog.Builder ingredinet_dialog_builder = new AlertDialog.Builder(requireContext());
//        //set on close details are fetched and saved to list
//        ingredinet_dialog_builder.setPositiveButton("confirm", (dialog, which) -> {
//            //TODO handle saving name, amount and type to HashMap
//            dialog.dismiss();
//        });
//        ingredinet_dialog_builder.setNegativeButton("reset", (dialog, which) ->{
//            dialog.dismiss();
//        });
//        ingredient_view.addView(ingredient_view);
//
//
//        EditText ingredient_amount = new EditText((requireContext()));
//        ingredient_amount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL); // restructs amount to only allow numbers
//        ingredient_amount.setHint("create_ingredient_amount_selectorenter amount in numbers");
//        ingredinet_dialog_builder.setView(ingredient_amount);
//
//        TextView display_ingredient_name= new TextView((requireContext()));
//        display_ingredient_name.setText(ingredient_name);
//        display_ingredient_name.setPadding(8,8,8,8);
//        display_ingredient_name.setTextColor((int) Long.parseLong("FF000000".substring(1), 16));
//        display_ingredient_name.setBackgroundColor((int) Long.parseLong("#FEFAE0".substring(1), 16));
//        ingredinet_dialog_builder.setView(display_ingredient_name);
//
//        Spinner amount_type_selector = new Spinner(requireContext());
//        String[] amount_types_list = {"grams", "kilograms", "ml", "liters", "tsp", "Tbsp", "cups", "ounces", "pounds"};
//        ArrayAdapter amount_types_adapter = new ArrayAdapter(requireContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, amount_types_list);
//        amount_type_selector.setAdapter(amount_types_adapter);
//        AlertDialog.Builder amount_types_dropdown_builder = new AlertDialog.Builder(requireContext());
//        amount_types_dropdown_builder.setView(amount_type_selector);
//        amount_types_dropdown_builder.setTitle("select unit of amount of" + ingredient_name);
//
//        AlertDialog ingredient_details_alert = ingredinet_dialog_builder.create();
//
//        create_ingredient_amount_selector(ingredient_name, ingredient_view);
//        ingredient_details_alert.setTitle("");
//
//        ingredient_details_alert.show(); //this causes error as the child already has a parent which needs to be removed before calling
//                //TODO add adding that ingredient to list on confirm button and clearing it fo
//
//    }

//    private void create_ingredient_amount_selector(String ingredient_name, View ingredient_view_container){ //TODO changed the background colour here last
//       // ingredient_view.removeAllViews();
//        // add the name of the ingredinet
//        ViewGroup ingredient_view = getView().findViewById(R.id.ingredient_layout);
//        FrameLayout layout = ingredient_view_container.findViewById(R.id.ingredient_layout);
//        layout.removeAllViews();
//        TextView display_ingredient_name= new TextView((requireContext()));
//        display_ingredient_name.setText(ingredient_name);
//        display_ingredient_name.setPadding(8,8,8,8);
//        display_ingredient_name.setTextColor((int) Long.parseLong("FF000000".substring(1), 16));
//        display_ingredient_name.setBackgroundColor((int) Long.parseLong("#FEFAE0".substring(1), 16));
//        TextView display_ingredient_TextView = getView().findViewById(R.id.ingredient_name);
//        // add the edit text for the amount of the ingredient
//        EditText ingredient_amount = new EditText((requireContext()));
//        ingredient_amount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL); // restructs amount to only allow numbers
//        ingredient_amount.setHint("create_ingredient_amount_selectorenter amount in numbers");
//
//        Spinner amount_type_selector = new Spinner(requireContext()); //getView().findViewById(R.id.amount_type_selector);
//        // add spinner for choosing the metric of the ingredient amount
//        //
//        // Spinner amount_type_selector = new Spinner(requireContext());
//
//
//        // istView amount_types = binding.amountType
//        String[] amount_types_list = {"grams", "kilograms", "ml", "liters", "tsp", "Tbsp", "cups", "ounces", "pounds"};
//        ArrayAdapter amount_types_adapter = new ArrayAdapter(requireContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, amount_types_list);
//        amount_type_selector.setAdapter(amount_types_adapter);
//        AlertDialog.Builder amount_types_dropdown_builder = new AlertDialog.Builder(requireContext());
//        // amount_types_dropdown_builder.setView(ingredient_view);
//        amount_types_dropdown_builder.setView(amount_type_selector);
//        amount_types_dropdown_builder.setTitle("select unit of amount of" + ingredient_name);
//        amount_type_selector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int selected_type_index, long id) {
//                Log.d("ingredient selected: ", view.toString());
//                String amount_type = parent.getItemAtPosition(selected_type_index).toString();
//            };
//            @Override
//           public void onNothingSelected(AdapterView<?> parent){
//                Log.d("setting type for the ingredient amount", "no amount type selected");
//            }
//        });
//       // ingredient_view.addView(amount_type_selector);
//        amount_type_selector.setVisibility(View.VISIBLE);
//        ingredient_view.setVisibility(View.VISIBLE);
//    }

//TODO next: so basically if I create the view of the alert dialog and add that to the layout from my xml and then I use the alert dialog view and add spinner view to that


    public void set_r_name(){
        EditText r_name_field = getView().findViewById(R.id.set_recipe_name_btn_a);
        String r_name = r_name_field.getText().toString();
        recipe.setR_name(r_name);
    }



    public void uploadImageToFirebase(Uri imageUri) {
        // Generate a unique filename for the image
        String fileName = "recipeimgs/IMG_" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(fileName);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString(); // Get the URL
                            recipe.setImageUrl(imageUrl);
                        })
                )
                .addOnFailureListener(e ->
                        Log.e("image",  "image upload failed")
                );
    }


@Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void scrapeRecipes(){
        Intent intent = new Intent(requireContext(), WebScrapingActivity.class);
        startActivity(intent);
    }
}