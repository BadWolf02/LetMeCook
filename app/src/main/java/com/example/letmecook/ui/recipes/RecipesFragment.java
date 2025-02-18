package com.example.letmecook.ui.recipes;

import static android.content.ContentValues.TAG;

import android.nfc.Tag;
import android.os.Bundle; //Passes data to the fragment and restores its state after config changes
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater; // handles the XML layout file into a View object
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup; // View object that gets displayed by the fragment
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull; //Arguments or return values that cannot be null
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment; //base class for the fragment
import androidx.lifecycle.ViewModelProvider; //managing ViewModelProvide

import com.example.letmecook.R;
import com.example.letmecook.Recipe;
import com.example.letmecook.databinding.FragmentRecipesBinding;

import java.util.ArrayList;

public class RecipesFragment extends Fragment {

    private FragmentRecipesBinding binding; // binding object allows interaction with views
    public Recipe recipe = new Recipe();

    private ArrayList<Object> steps = new ArrayList();

    private int next_step_index = 3; // TODO change this to be fetched dynamically


    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRecipesBinding.inflate(inflater, container, false);
        RecipesViewModel recipesViewModel =
                new ViewModelProvider(this).get(RecipesViewModel.class);

        final TextView textView = binding.textRecipes;
        recipesViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);


    View root = binding.getRoot();


        recipesViewModel = new ViewModelProvider(this).get(RecipesViewModel.class);

        /**
         * handle user entering recipe name
         */
        EditText edit_r_name = binding.textViewRecipeName;
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
                recipe.setR_name(s.toString());

            }
        });





        // add recipe step
        Button add_step_btn = binding.addStepBtn;
        add_step_btn.setOnClickListener(v -> {
                    // public void add_recipe_step(){
                    // change the entered text to a text box??
                    // add a new step text field
                    // allow the user to somehow still eddit the submitted step
                    EditText add_step_text_field = getView().findViewById(R.id.EditText_add_step1); // may have to bind this since it is called from inside oncreate
                    String add_step_text = add_step_text_field.getText().toString();
                    if ( add_step_text.trim() != "") {
                        Log.d("empty string",  add_step_text.toString());
                        steps.add(add_step_text.toString());
            }
                    Log.d("empty string", add_step_text); //TODO remove this


                    // add this to db either save straight to a map here or to the recipie cals
                    // dynamically create another edit text
                    Integer step_no = recipe.getStepsAmount() + 1;
                    //TODO dynamically create this here
                    EditText add_step_box = new EditText(getContext());
                    add_step_box.setId(step_no); //TODO
                    add_step_box.setHint("add Step");
                    ScrollView scrollView = getView().findViewById(R.id.scrollView);
                    LinearLayout layout = scrollView.findViewById(R.id.layout); // .layout //TODO figure out what needs to be here instead of dynamic layout? linear maybe
                    LinearLayout.LayoutParams editbox_params = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    add_step_box.setLayoutParams(editbox_params);
                    steps.add(add_step_box);
                    // TODO add the index (starting at 0 of where we want to add the new edit view -> see if I can update this dynamically too depending on what is above
                    layout.addView(add_step_box, next_step_index);
                    next_step_index ++;
                    Log.e(TAG, "trying to add field");
                    // increment step_no when saved
                    recipe.incrementStepsAmount();

                    // TODO get it to autoscroll
                });

        // }



        // get cusine from selection and set it as cusine of the recipe
        Spinner cusine_spinner = binding.cusineDropdown;
        cusine_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id){
                String cusine = parent.getItemAtPosition(pos).toString();
                recipe.setCusine(cusine);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent){

            }
        });
        //set Spinner for cusine
        ArrayAdapter<CharSequence> cusine_adapter = ArrayAdapter.createFromResource(this.requireContext(), R.array.cusine_dropdown, android.R.layout.simple_spinner_dropdown_item);
        cusine_adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        Log.d("spinner", "set dropdown view");
        // Spinner cusine_spinner = getView().findViewById(R.id.cusine_dropdown); //TODO causing error, null pointer exception, try binding instead of get view by id
        Log.d("spinner", "got spinner view by id");
        cusine_spinner.setAdapter(cusine_adapter);
        Log.d("spinner", "set up adapter");




        // dropdown for meal Type
        ListView meal_type_dropdown_trigger = binding.mealTypeDropdownTrigger;
        //TODO add list and selectiontracker
        String[] meal_types = {"breackfast", "lunch", "dinner", "warm meal", "snack", "cold meal", "starter", "desert", "salad", "soup"}; //list of meal types
        boolean[] selected_meal_type_tracker = new boolean[meal_types.length]; //track which items are selected
        ArrayList<String> selected_meal_types = new ArrayList<>();
        selected_meal_types.add("none");
        //add onClickListener and handle event
        ArrayAdapter meal_type_adapter = new ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, selected_meal_types);
        meal_type_dropdown_trigger.setAdapter(meal_type_adapter);
//        ArrayAdapter meal_type_adapter = new ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, selected_meal_types);
//        meal_type_adapter.

        meal_type_dropdown_trigger.setOnTouchListener((v, event) ->{

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
            meal_type_dropdown_builder.setPositiveButton("select", ((dialog, which) -> {
                meal_type_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                meal_type_dropdown_trigger.setAdapter(meal_type_adapter);//meal_type_adapter((TextUtils.join(", ", selected_meal_types)));
                recipe.setMealType(selected_meal_types);
        }));
            //TODO chane this to unselect and enter none
            meal_type_dropdown_builder.setNegativeButton("No type", (dialog, whick) -> {
                recipe.setMealType(null);
                dialog.dismiss();
            });
            AlertDialog meal_type_dropdown = meal_type_dropdown_builder.create();
            meal_type_dropdown.show();





            return true;
        });


        // total time h selection handler
        NumberPicker number_picker_h = binding.totalTimeH;
        NumberPicker number_picker_min = binding.totalTimeMin;

        number_picker_h.setMinValue(0);
        number_picker_h.setMaxValue(100);
        number_picker_h.setFormatter(i -> String.format("%02d", i));

        number_picker_min.setMinValue(0);
        number_picker_min.setMaxValue(59);
        number_picker_min.setFormatter(i -> String.format("%02d", i));

        number_picker_h.setOnValueChangedListener((picker, oldVal, newVal) -> {
            recipe.setTotal_time_h(newVal);//TODO continue here next
        });

        number_picker_min.setOnValueChangedListener(((picker, oldVal, newVal) -> {
            recipe.setTotal_time_min(newVal);
        }));




        Button create_recipe_btn = binding.addRecipeBtn;
        create_recipe_btn.setOnClickListener(v -> {
            Log.d("adding step", "about to add lastep");
            // add_last_step_to_recipe();
           recipe.create();
        });




        return root;
    }

    //add recipe step
    // :( this won't work if there has been nothing entered and I try to do a toString it causes a null pointer exception
//    public void add_last_step_to_recipe(){
//        Log.d("adding step", "add last step method reached");
//        //check that this actually has the right amount of steps
//        EditText last_step = getView().findViewById(next_step_index); //TODO do this next
//       //  for (int i=0; i<=recipe.getStepsAmount(); i++){
//        if (last_step.toString() != ""){
//            recipe.addStep(last_step.toString());
//            Log.d("adding steps to recipe", "last step: "+last_step.toString());
//        }
//        else {
//            Log.d("adding steps to recipe", "last step was empty");
//        }
//    }


    public void set_r_name(){
        EditText r_name_field = getView().findViewById(R.id.set_recipe_name_btn_a);
        String r_name = r_name_field.getText().toString();
        recipe.setR_name(r_name);
    }



@Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}