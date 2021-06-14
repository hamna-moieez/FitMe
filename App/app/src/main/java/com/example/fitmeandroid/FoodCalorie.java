package com.example.fitmeandroid;

public class FoodCalorie {

    String food_name, food_calorie;

    public FoodCalorie() {

    }

    public FoodCalorie(String food, String calorie){
        this.food_name = food;
        this.food_calorie = calorie;
    }

    public String getFood_name() {
        return food_name;
    }

    public void setFood_name(String food_name) {
        this.food_name = food_name;
    }

    public String getFood_calorie() {
        return food_calorie;
    }

    public void setFood_calorie(String food_calorie) {
        this.food_calorie = food_calorie;
    }

}
