package com.example.demo1;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AddNewDish {

    @FXML
    private TextField nameField;

    @FXML
    private TextField descriptionField;

    @FXML
    private ComboBox<String> courseDropdown;

    @FXML
    private VBox recipeFieldsVBox;

    @FXML
    private VBox stepFieldsVBox;

    private List<ComboBox<String>> recipeComboBoxes = new ArrayList<>();
    private List<TextField> stepTextFields = new ArrayList<>();

    @FXML
    private void initialize() {
        courseDropdown.getItems().addAll("First", "Second", "Third");
        addRecipeField(); // Initially add one recipe field
        addStepField(); // Initially add one step field
    }

    @FXML
    private void submit() {
        String name = nameField.getText();
        String description = descriptionField.getText();
        String course = courseDropdown.getValue();

        if (name.isEmpty() || description.isEmpty() || course == null || !validateRecipeSelection() || !validateStepFields()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields and select a recipe for each step.", null);
            return;
        }

        try {
            Connection connection = DatabaseUtil.connectToDatabase();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO in2033t02Dish (Name, Description, Course, Status, Chef_Creator_ID) VALUES (?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, name);
            statement.setString(2, description);
            statement.setString(3, course);
            statement.setString(4, "Pending"); // Set status as Pending by default
            statement.setString(5, "1"); // Set Chef_Creator_ID as 1 for now

            statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            int dishId = -1;
            if (generatedKeys.next()) {
                dishId = generatedKeys.getInt(1);
            }
            generatedKeys.close();
            statement.close();

            if (dishId != -1) {
                for (ComboBox<String> comboBox : recipeComboBoxes) {
                    String selectedRecipe = comboBox.getValue();
                    int recipeId = getRecipeIdByName(selectedRecipe);
                    if (recipeId != -1) {
                        statement = connection.prepareStatement("INSERT INTO in2033t02Dish_Recipes (Dish_ID, Recipe_ID) VALUES (?, ?)");
                        statement.setInt(1, dishId);
                        statement.setInt(2, recipeId);
                        statement.executeUpdate();
                    }
                }

                for (int i = 0; i < stepTextFields.size(); i++) {
                    TextField textField = stepTextFields.get(i);
                    String stepDescription = textField.getText();
                    int stepNumber = i + 1;
                    statement = connection.prepareStatement("INSERT INTO in2033t02Dish_Steps (Dish_ID, Step_Description, Step_Number) VALUES (?, ?, ?)");
                    statement.setInt(1, dishId);
                    statement.setString(2, stepDescription);
                    statement.setInt(3, stepNumber);

                    statement.executeUpdate();
                    statement.close();
                }

                showAlert(Alert.AlertType.INFORMATION, "Success", "Dish added successfully.", null);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to retrieve dish ID.", null);
            }

            connection.close();
        } catch (SQLException | ClassNotFoundException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Database error.", e.getMessage());
        }
    }


    private int getRecipeIdByName(String name) {
        try {
            Connection connection = DatabaseUtil.connectToDatabase();
            PreparedStatement statement = connection.prepareStatement("SELECT Recipe_ID FROM in2033t02Recipe WHERE Name = ?");
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            int recipeId = -1;
            if (resultSet.next()) {
                recipeId = resultSet.getInt("Recipe_ID");
            }
            resultSet.close();
            statement.close();
            connection.close();
            return recipeId;
        } catch (SQLException | ClassNotFoundException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Database error.", e.getMessage());
            return -1;
        }
    }

    private boolean validateRecipeSelection() {
        for (ComboBox<String> comboBox : recipeComboBoxes) {
            if (comboBox.getValue() == null || comboBox.getValue().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean validateStepFields() {
        for (TextField textField : stepTextFields) {
            if (textField.getText() == null || textField.getText().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @FXML
    private void addRecipeField() {
        ComboBox<String> recipeComboBox = new ComboBox<>();
        try {
            Connection connection = DatabaseUtil.connectToDatabase();
            PreparedStatement statement = connection.prepareStatement("SELECT Name FROM in2033t02Recipe");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                recipeComboBox.getItems().add(resultSet.getString("Name"));
            }
            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException | ClassNotFoundException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error loading recipes from database.", e.getMessage());
        }
        recipeFieldsVBox.getChildren().add(recipeComboBox);
        recipeComboBoxes.add(recipeComboBox);
    }

    @FXML
    private void addStepField() {
        TextField stepTextField = new TextField();
        Label stepLabel = new Label("Step " + (stepTextFields.size() + 1) + ": ");
        VBox stepVBox = new VBox(stepLabel, stepTextField);
        stepFieldsVBox.getChildren().add(stepVBox);
        stepTextFields.add(stepTextField);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message, String details) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (details != null && !details.isEmpty()) {
            alert.setContentText(details);
        }
        alert.showAndWait();
    }
}