package com.example.demo1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        populateIngredientComboBox();
        ingredientComboBox.setOnAction(this::updateUnitField);
    }

    private void populateIngredientComboBox() {
        try (Connection connection = DatabaseUtil.connectToDatabase();
             PreparedStatement stmt = connection.prepareStatement("SELECT Name FROM in2033t02Ingredient")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ingredientComboBox.getItems().add(rs.getString("Name"));
            }
        } catch (SQLException | ClassNotFoundException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load ingredients.", e.getMessage());
        }
    }

    private void updateUnitField(ActionEvent event) {
        String selectedIngredient = ingredientComboBox.getValue();
        if (selectedIngredient != null) {
            try (Connection connection = DatabaseUtil.connectToDatabase();
                 PreparedStatement stmt = connection.prepareStatement("SELECT Unit FROM in2033t02Ingredient WHERE Name = ?")) {
                stmt.setString(1, selectedIngredient);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    unitField.setText(rs.getString("Unit"));
                }
            } catch (SQLException | ClassNotFoundException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to retrieve unit.", e.getMessage());
            }
        }
    }

    @FXML
    private void submit(ActionEvent event) {
        String ingredientName = ingredientComboBox.getValue();
        String quantity = quantityField.getText();
        String unit = unitField.getText();
        LocalDate date = dateField.getValue();
        String reason = reasonField.getText();

        if (ingredientName == null || ingredientName.isEmpty() || quantity.isEmpty() || unit.isEmpty() || date == null || reason.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields correctly.", null);
            return;
        }

        String formattedDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE);

        try (Connection connection = DatabaseUtil.connectToDatabase();
             PreparedStatement stmt = connection.prepareStatement("INSERT INTO in2033t02Waste_Log (Ingredient_ID, Quantity_Wasted, Unit, Date_of_Waste, Reason) VALUES ((SELECT Ingredient_ID FROM in2033t02Ingredient WHERE Name = ?), ?, ?, ?, ?)")) {
            stmt.setString(1, ingredientName);
            stmt.setString(2, quantity);
            stmt.setString(3, unit);
            stmt.setString(4, formattedDate);
            stmt.setString(5, reason);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Waste entry added successfully!", null);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add waste entry.", null);
            }
        } catch (SQLException | ClassNotFoundException e) {
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
