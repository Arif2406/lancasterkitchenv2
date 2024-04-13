package com.example.demo1;
import com.example.demo1.DatabaseUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.Alert;

import javax.swing.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;


public class DishInformationController {

    @FXML
    private Label dishNameLabel;

    @FXML
    private TextArea dishDescriptionTextArea;

    @FXML
    private ImageView dishPictureLabel;

    @FXML
    private Label dishCourseLabel;

    @FXML
    private ListView<String> recipeList;

    @FXML
    private TextArea dishStepsTextArea;
    @FXML
    private Label dishStatusLabel;
    @FXML
    private TextArea dishStatusTextArea;


    public void initData(String dishName) {
        dishNameLabel.setText(dishName);
        // You can populate other fields with information about the selected dish
    }
    private DefaultListModel<String> recipeModel;

    private int dishID;

    public void initialize(int dishID, String dishName) {
        System.out.println("Initializing DishInformationController"); // Print statement for debugging
        this.dishID = dishID;
        try {
            displayDishInformation(dishName);
            populateRecipeList(dishID);
            populateDishSteps(dishID);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace(); // Print stack trace for debugging
            // Display an error message to the user
            showAlert(Alert.AlertType.ERROR, "Error", "Error loading dish information.", e.getMessage());
        }
    }

    private void displayDishInformation(String dishName) throws SQLException, ClassNotFoundException {
        Connection connection = DatabaseUtil.connectToDatabase();
        String query = "SELECT d.Description, d.Picture_URL, d.Status, d.Course, GROUP_CONCAT(DISTINCT r.Name) AS Recipes " +
                "FROM in2033t02Dish d " +
                "LEFT JOIN in2033t02Dish_Recipes dr ON d.Dish_ID = dr.Dish_ID " +
                "LEFT JOIN in2033t02Recipe r ON dr.Recipe_ID = r.Recipe_ID " +
                "WHERE d.Name = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, dishName);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String description = resultSet.getString("Description");
                String pictureURL = resultSet.getString("Picture_URL");
                String status = resultSet.getString("Status");
                String course = resultSet.getString("Course");
                String recipes = resultSet.getString("Recipes");

                // Update all relevant UI elements
                System.out.println("Description: " + description);
                System.out.println("Picture URL: " + pictureURL);
                System.out.println("Status: " + status);
                System.out.println("Course: " + course);
                System.out.println("Recipes: " + recipes);

                dishNameLabel.setText("Dish Name: " + dishName);
                dishDescriptionTextArea.setText("Description: " + description);
                dishCourseLabel.setText("Course: " + course);
                dishStatusLabel.setText("Status: " + status);

                try {
                    // Load image from URL and set it to the ImageView
                    Image image = new Image(new URL(pictureURL).toExternalForm(), true);
                    dishPictureLabel.setImage(image);
                } catch (MalformedURLException e) {
                    // If the URL is invalid, handle the exception or show an error message
                    // dishPictureLabel.setText("Image not available");
                    e.printStackTrace();
                }

                // Display recipes
                if (recipes != null && !recipes.isEmpty()) {
                    String[] recipeNames = recipes.split(",");
                    recipeList.getItems().addAll(recipeNames);
                }
            } else {
                // If no data is found for the given dish name, you might want to handle this case
                // For example, display an error message or clear the UI elements
                System.out.println("No data found for dish: " + dishName);
            }

            preparedStatement.close();
        } finally {
            connection.close();
        }
    }


    private void populateRecipeList(int dishID) throws SQLException, ClassNotFoundException {
        Connection connection = DatabaseUtil.connectToDatabase();
        String query = "SELECT Recipe.Name FROM in2033t02Recipe Recipe JOIN in2033t02Dish_Recipes DR ON Recipe.Recipe_ID = DR.Recipe_ID WHERE DR.Dish_ID = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setInt(1, dishID);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String recipeName = resultSet.getString("Name");
                recipeList.getItems().add(recipeName);
            }

            preparedStatement.close();
        } finally {
            connection.close();
        }
    }

    private void populateDishSteps(int dishID) throws SQLException, ClassNotFoundException {
        Connection connection = DatabaseUtil.connectToDatabase();
        String query = "SELECT Step_Number, Step_Description FROM in2033t02Dish_Steps WHERE Dish_ID = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setInt(1, dishID);
            ResultSet resultSet = preparedStatement.executeQuery();
            StringBuilder stepsBuilder = new StringBuilder();

            while (resultSet.next()) {
                int stepNumber = resultSet.getInt("Step_Number");
                String stepDescription = resultSet.getString("Step_Description");
                stepsBuilder.append("Step ").append(stepNumber).append(": ").append(stepDescription).append("\n");
            }

            dishStepsTextArea.setText(stepsBuilder.toString());

            preparedStatement.close();
        } finally {
            connection.close();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message, String details) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (details != null && !details.isEmpty()) {
            TextArea textArea = new TextArea(details);
            alert.getDialogPane().setExpandableContent(textArea);
        }
        alert.showAndWait();
    }
}