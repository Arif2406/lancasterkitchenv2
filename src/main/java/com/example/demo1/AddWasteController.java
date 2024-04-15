package com.example.demo1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AddWasteController {

    @FXML
    private ComboBox<String> ingredientComboBox;
    @FXML
    private TextField quantityField;
    @FXML
    private TextField unitField;
    @FXML
    private DatePicker dateField;
    @FXML
    private TextField reasonField;

    @FXML
    private void initialize() {
        System.out.println("Initializing controller...");
        populateIngredientComboBox();
        ingredientComboBox.setOnAction(this::updateUnitField);
    }

    private void populateIngredientComboBox() {
        System.out.println("Populating ingredient combo box...");
        String query = "SELECT Name FROM in2033t02Ingredient";
        try (Connection connection = DatabaseUtil.connectToDatabase();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ingredientComboBox.getItems().add(rs.getString("Name"));
                System.out.println("Added ingredient: " + rs.getString("Name"));
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("Error loading ingredients from database: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load ingredients from database.", e.getMessage());
            e.printStackTrace(); // Consider logging this properly in a real application
        }
    }

    private void updateUnitField(ActionEvent event) {
        System.out.println("Updating unit field...");
        String selectedIngredient = ingredientComboBox.getValue();
        if (selectedIngredient != null) {
            try (Connection connection = DatabaseUtil.connectToDatabase();
                 PreparedStatement stmt = connection.prepareStatement("SELECT Unit FROM in2033t02Ingredient WHERE Name = ?")) {
                stmt.setString(1, selectedIngredient);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    unitField.setText(rs.getString("Unit"));
                    System.out.println("Set unit field to: " + rs.getString("Unit"));
                }
            } catch (SQLException | ClassNotFoundException e) {
                System.out.println("Error retrieving unit from database: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to retrieve unit.", e.getMessage());
            }
        }
    }

    @FXML
    private void submit(ActionEvent event) {
        System.out.println("Submitting waste entry...");
        String ingredientName = ingredientComboBox.getValue();
        String quantity = quantityField.getText();
        String unit = unitField.getText();
        LocalDate date = dateField.getValue();
        String reason = reasonField.getText();

        System.out.println("Data collected: Ingredient=" + ingredientName + ", Quantity=" + quantity + ", Unit=" + unit + ", Date=" + date + ", Reason=" + reason);

        if (ingredientName == null || ingredientName.isEmpty() || quantity.isEmpty() || unit.isEmpty() || date == null || reason.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields correctly.", null);
            return;
        }

        Date formattedDate = Date.valueOf(date.format(DateTimeFormatter.ISO_LOCAL_DATE));

        try (Connection connection = DatabaseUtil.connectToDatabase();
             PreparedStatement stmt = connection.prepareStatement("INSERT INTO in2033t02Waste_Log (Ingredient_ID, Quantity_Wasted, Unit, Date_of_Waste, Reason) VALUES ((SELECT Ingredient_ID FROM in2033t02Ingredient WHERE Name = ?), ?, ?, ?, ?)")) {
            stmt.setString(1, ingredientName);
            stmt.setString(2, quantity);
            stmt.setString(3, unit);
            stmt.setDate(4, formattedDate);
            stmt.setString(5, reason);
            int affectedRows = stmt.executeUpdate();
            System.out.println("Database update executed, affected rows: " + affectedRows);
            if (affectedRows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Waste entry added successfully!", null);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add waste entry.", null);
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("Database error while inserting waste entry: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage(), null);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
