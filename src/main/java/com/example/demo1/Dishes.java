package com.example.demo1;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.util.ArrayList;
import java.util.List;

public class Dishes {

    @FXML
    private Button viewCurrentDishesButton;

    @FXML
    private Button addNewDishButton;

    @FXML
    private Button viewPendingDishesButton;

    @FXML
    private Button viewAllDishesButton;

    @FXML
    private Button homeButton;

    private static List<Dish> currentDishes = new ArrayList<>(); // List to hold current dishes

    @FXML
    public void initialize() {
        // Optional: perform any initialization tasks here (e.g., add some sample dishes)
        currentDishes.add(new Dish("Pizza", "Tomato sauce, cheese, dough"));
        currentDishes.add(new Dish("Pasta", "Pasta, tomato sauce, vegetables"));
    }

    // Other methods...

    public static class Dish {
        private String name;
        private String ingredients;

        public Dish(String name, String ingredients) {
            this.name = name;
            this.ingredients = ingredients;
        }

        public String getName() {
            return name;
        }

        public String getIngredients() {
            return ingredients;
        }
    }
}
