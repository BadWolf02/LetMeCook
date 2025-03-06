package com.example.letmecook.models;

public class Ingredient {
    private String name;
    private String amount;  //define grams or litres etc

    public Ingredient(String name, String amount) {
        this.name = name;
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}