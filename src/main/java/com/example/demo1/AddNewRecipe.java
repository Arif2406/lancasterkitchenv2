package com.example.demo1;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AddNewRecipe {

    @FXML
    private TextField nameField;
    @FXML
    private TextField descriptionField;
    @FXML
    private VBox recipeFieldsVBox;
    @FXML
    private VBox stepFieldsVBox;

    private List<ComboBox<String>> ingredientComboBoxes = new ArrayList<>();
    private List<TextField> quantityFields = new ArrayList<>();
    private List<TextField> unitFields = new ArrayList<>();
    private List<TextField> stepTextFields = new ArrayList<>();

    @FXML
    private void initialize() {
        addIngredientField();
        addStepField();
    }

    @FXML
    private void addIngredientField() {
        HBox hbox = new HBox(10);
        ComboBox<String> ingredientComboBox = new ComboBox<>();
        TextField quantityField = new TextField();
        TextField unitField = new TextField();

        ingredientComboBox.setPromptText("Select an Ingredient");
        quantityField.setPromptText("Quantity");
        unitField.setPromptText("Unit");

        populateIngredientComboBox(ingredientComboBox);

        hbox.getChildren().addAll(new Label("Ingredient:"), ingredientComboBox, new Label("Quantity:"), quantityField, new Label("Unit:"), unitField);
        recipeFieldsVBox.getChildren().add(hbox);

        ingredientComboBoxes.add(ingredientComboBox);
        quantityFields.add(quantityField);
        unitFields.add(unitField);
    }

    private void populateIngredientComboBox(ComboBox<String> comboBox) {
        try (Connection connection = DatabaseUtil.connectToDatabase();
             PreparedStatement stmt = connection.prepareStatement("SELECT Name FROM in2033t02Ingredient");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                comboBox.getItems().add(rs.getString("Name"));
            }
        } catch (SQLException | ClassNotFoundException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load ingredients.", e.getMessage());
        }
    }

    @FXML
    private void addStepField() {
        TextField stepTextField = new TextField();
        stepTextField.setPromptText("Describe the step");
        stepFieldsVBox.getChildren().add(stepTextField);
        stepTextFields.add(stepTextField);
    }

    @FXML
    private void submit() {
        String name = nameField.getText();
        String description = descriptionField.getText();

        if (name.isEmpty() || description.isEmpty() || !validateIngredientSelection() || !validateStepFields()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields and select an ingredient for each step.", null);
            return;
        }

        try {
            Connection connection = DatabaseUtil.connectToDatabase() ;
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO in2033t02Recipes (name, description) VALUES (?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);

            stmt.setString(1, name);
            stmt.setString(2, description);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                showAlert(Alert.AlertType.ERROR, "Error", "Creating recipe failed, no rows affected.", null);
                return;
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long recipeId = generatedKeys.getLong(1);
                    updateIngredientsAndSteps(recipeId, connection);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Recipe added successfully!", null);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Creating recipe failed, no ID obtained.", null);
                }
            }
        }  catch (SQLException | ClassNotFoundException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage(), null);
            e.printStackTrace();
        }
    }

    private void updateIngredientsAndSteps(long recipeId, Connection connection) throws SQLException {
        for (int i = 0; i < ingredientComboBoxes.size(); i++) {
            String ingredientName = ingredientComboBoxes.get(i).getValue();
            String quantity = quantityFields.get(i).getText();
            String unit = unitFields.get(i).getText();
            int ingredientId = getIngredientIdByName(ingredientName, connection);

            if (ingredientId != -1 && !quantity.isEmpty() && !unit.isEmpty()) {
                PreparedStatement stmt = connection.prepareStatement("INSERT INTO in2033t02Recipe_Ingredients (recipe_id, ingredient_id, quantity, unit) VALUES (?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);

                stmt.setLong(1, recipeId);
                stmt.setInt(2, ingredientId);
                stmt.setString(3, quantity);
                stmt.setString(4, unit);
                stmt.executeUpdate();

            }
        }

        for (int i = 0; i < stepTextFields.size(); i++) {
            String step = stepTextFields.get(i).getText();
            if (!step.isEmpty()) {
                PreparedStatement stmt = connection.prepareStatement("INSERT INTO in2033t02Recipe_Steps (recipe_id, step_number, step_description) VALUES (?, ?, ?)");

                stmt.setLong(1, recipeId);
                stmt.setInt(2, i + 1);
                stmt.setString(3, step);
                stmt.executeUpdate();

            }
        }
    }

    private int getIngredientIdByName(String ingredientName, Connection connection) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT Ingredient_ID FROM in2033t02Ingredient WHERE Name = ?");

        stmt.setString(1, ingredientName);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("Ingredient_ID");
            }
            return -1;  // Not found
        }
        catch (SQLException e) {
            e.printStackTrace();
            return -1;  // Error case
        }
    }

    private boolean validateIngredientSelection() {
        return ingredientComboBoxes.stream().noneMatch(combo -> combo.getValue() == null || combo.getValue().isEmpty());
    }

    private boolean validateStepFields() {
        return stepTextFields.stream().noneMatch(text -> text.getText().isEmpty());
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
