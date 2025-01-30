package com.example.letmecook;

import org.junit.Test;

//TODO add import to recipe??

public class RecipeTest {

    @Test
    void testAddToRecipeDB(){
        Recipe r = new Recipe();
        r.setR_name("poridge");
        r.setAuthor("test");
        r.setR_type("private");
        r.addStep("heat up the milk in a pot");
        r.addStep("when the milk is boiling add oats");
        r.addStep("stir constantly until the mixture starts to thicken up");
        r.addStep("once the mixture has reached the desired thickness take of the heat and optionally add cinamon and/or sugar");
        r.addStep("serve while hot");
        r.addIngredient("oats", 0);
        r.addIngredient("milk", 0);
        r.addIngredient("cinamon and sugar / honey", 0);
        r.create();


    }

}
