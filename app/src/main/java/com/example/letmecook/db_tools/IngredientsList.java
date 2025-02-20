package com.example.letmecook.db_tools;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Map;

public class IngredientsList {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    public IngredientsList(){}

//    public ArrayList getIngreedients(){
//        DocumentReference ingredientsRef = db.document("ingreedients");
//        DocumentSnapshot ingredientsSnapshot = ingredientsRef.get().getResult();
//        Map<String, Object> ingred_doc = ingredientsSnapshot.getData();
//        for (Map.Entry<String, Object> entry : ingred_doc.entrySet()) {
//            System.out.println("db entry" + entry.getValue());
//            // TODO continue from here and get the name of the recipe and put it in a map with ID
//
//        }
//    }

//    public static void main(String[] args){
//        ArrayList ingredients = this.getIngreedients();
//    }

}

