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
    private String selectedDish;

    // Setter method for selected dish
    public void setSelectedDish(String selectedDish) {
        this.selectedDish = selectedDish;
    }

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

    private int dishID;

    public DishInformationController(int dishID, String selectedDish) {
    }

    public void initialize() {
        // Initialize the controller here
        System.out.println("Initializing DishInformationController");
        try {
            System.out.println("4");
            displayDishInformation(selectedDish); // Use selectedDish here
            populateRecipeList(dishID);
            populateDishSteps(dishID);
        } catch (SQLException | ClassNotFoundException e) {
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
        System.out.println("3");

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
                System.out.println("4");

                dishNameLabel.setText("Dish Name: " + dishName);
                dishDescriptionTextArea.setText("Description: " + description);
                dishCourseLabel.setText("Course: " + course);
                dishStatusLabel.setText("Status: " + status);

                try {
                    Image image = new Image(new URL(pictureURL).toExternalForm(), true);
                    dishPictureLabel.setImage(image);
                } catch (MalformedURLException e) {
             //       dishPictureLabel.setText("Image not available");
                }

                if (recipes != null && !recipes.isEmpty()) {
                    String[] recipeNames = recipes.split(",");
                    recipeList.getItems().addAll(recipeNames);
                }
            } else {
                dishNameLabel.setText("No data found for this dish");
            }

            preparedStatement.close();
        } finally {
            connection.close();
        }
    }
    public void initData(String dishName) {
        dishNameLabel.setText(dishName);

    }
    private void populateRecipeList(int dishID) throws SQLException, ClassNotFoundException {
        Connection connection = DatabaseUtil.connectToDatabase();
        String query = "SELECT Recipe.Name FROM in2033t02Recipe Recipe JOIN in2033t02Dish_Recipes DR ON Recipe.Recipe_ID = DR.Recipe_ID WHERE DR.Dish_ID = ?";
        System.out.println("2");

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setInt(1, dishID);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String recipeName = resultSet.getString("Name");
                recipeList.getItems().add(recipeName);
            }

            System.out.println("5");

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

            while (resultSet.next())  {
                int stepNumber = resultSet.getInt("Step_Number");
            String stepDescription = resultSet.getString("Step_Description");
            stepsBuilder.append("Step ").append(stepNumber).append(": ").append(stepDescription).append("\n");
        }
            System.out.println("8");


            dishStepsTextArea.setText(stepsBuilder.toString());

        preparedStatement.close();
    } finally {
        connection.close();
    }
}
    public void setVisible(boolean isVisible) {
        dishNameLabel.setVisible(isVisible);
        dishDescriptionTextArea.setVisible(isVisible);
        dishPictureLabel.setVisible(isVisible);
        dishCourseLabel.setVisible(isVisible);
        recipeList.setVisible(isVisible);
        dishStepsTextArea.setVisible(isVisible);
        dishStatusLabel.setVisible(isVisible);
        dishStatusTextArea.setVisible(isVisible);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message, String details) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        System.out.println("5");

        alert.setHeaderText(null);
        alert.setContentText(message);
        if (details != null && !details.isEmpty()) {
            TextArea textArea = new TextArea(details);
            alert.getDialogPane().setExpandableContent(textArea);
        }
        alert.showAndWait();
    }
}