import javafx.fxml.FXML;

import java.awt.*;
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

    @FXML
    public void handleViewCurrentDishesButton() {
        System.out.println("Current Dishes:");
        for (Dish dish : currentDishes) {
            System.out.println(dish.getName());
        }
    }

    private static void showNewDishFrame() {
        System.out.println("Adding a New Dish:");
        // Prompt the user for dish details (replace with your actual UI interaction if needed)
        String name = getUserInput("Enter dish name: ");
        String ingredients = getUserInput("Enter dish ingredients (comma separated): ");
        currentDishes.add(new Dish(name, ingredients));
        System.out.println("Dish added successfully!");
    }

    private static String getUserInput(String prompt) {
        // Replace this with your actual mechanism to get user input (e.g., Scanner or a UI element)
        return "Sample Input";  // Placeholder for user input
    }

    @FXML
    public void handleAddNewDishButton() {
        showNewDishFrame();
    }

    @FXML
    public void handleViewPendingDishesButton() {
        System.out.println("View pending dishes button clicked.");
        System.out.println("This functionality is not currently implemented.");
    }

    @FXML
    public void handleViewAllDishesButton() {
        handleViewCurrentDishesButton(); // Reuse the method for current dishes as we have no pending dishes yet
    }

    @FXML
    public void handleHomePageButton() {
        System.out.println("Home Page button clicked.");
        // Replace this with your actual logic to return to the main menu
    }

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
