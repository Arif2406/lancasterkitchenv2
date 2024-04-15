package com.example.demo1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
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
            e.printStackTrace();
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

        try (Connection connection = DatabaseUtil.connectToDatabase()) {

            int quantityWasted = Integer.parseInt(quantity);
            int currentStock = getCurrentStock(ingredientName, connection);
            int newStock = currentStock - quantityWasted;

            if (newStock < 0) {
                showAlert(Alert.AlertType.ERROR, "Stock Error", "Not enough stock available to record this waste.", null);
                return;
            }

            updateIngredientStock(ingredientName, newStock, connection);


            Date formattedDate = Date.valueOf(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO in2033t02Waste_Log (Ingredient_ID, Quantity_Wasted, Unit, Date_of_Waste, Reason) VALUES ((SELECT Ingredient_ID FROM in2033t02Ingredient WHERE Name = ?), ?, ?, ?, ?)");
            stmt.setString(1, ingredientName);
            stmt.setInt(2, quantityWasted);
            stmt.setString(3, unit);
            stmt.setDate(4, formattedDate);
            stmt.setString(5, reason);
            int affectedRows = stmt.executeUpdate();

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

    private int getCurrentStock(String ingredientName, Connection connection) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT Stock_Level FROM in2033t02Ingredient WHERE Name = ?");
        stmt.setString(1, ingredientName);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("Stock_Level");
        }
        return 0;
    }

    private void updateIngredientStock(String ingredientName, int newStock, Connection connection) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("UPDATE in2033t02Ingredient SET Stock_Level = ? WHERE Name = ?");
        stmt.setInt(1, newStock);
        stmt.setString(2, ingredientName);
        stmt.executeUpdate();
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleChefsButtonClick(ActionEvent event) {
        navigateToPage("Chefs.fxml", "Chefs", event);
    }

    @FXML
    private void handleWasteButtonClick(ActionEvent event) {
        navigateToPage("Waste.fxml", "Waste", event);
    }

    @FXML
    private void handleMenusButtonClick(ActionEvent event) {
        navigateToPage("Menus.fxml", "Menus", event);
    }

    @FXML
    private void handleOrdersButtonClick(ActionEvent event) {
        navigateToPage("Orders.fxml", "Orders", event);
    }

    @FXML
    private void handleDishesButtonClick(ActionEvent event) {
        navigateToPage("Dishes.fxml", "Dishes", event);
    }

    @FXML
    private void handleSupplierButtonClick(ActionEvent event) {
        navigateToPage("SupplierStock.fxml", "Supplier", event);
    }

    @FXML
    private void handleStockButtonClick(ActionEvent event) {
        navigateToPage("CurrentStock.fxml", "Stock", event);
    }

    @FXML
    private void handleHomeButtonClick(ActionEvent event) {navigateToPage("MainPage.fxml", "Home", event);}

    @FXML
    private void handleNewDishButtonClick(ActionEvent event) {navigateToPage("AddNewDish.fxml", "Home", event);}

    @FXML
    private void handleNewRecipeButtonClick(ActionEvent event) {navigateToPage("AddNewRecipe.fxml", "AddNewRecipe", event);}


    @FXML
    private void handleNewMenuButtonClick(ActionEvent event) {navigateToPage("AddNewMenu.fxml", "Home", event);}
    @FXML
    private void handleNewWaste(ActionEvent event) {navigateToPage("AddWaste.fxml", "Home", event);}


    private void navigateToPage(String fxmlFile, String title, ActionEvent event) {
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setX(screenBounds.getMinX());
            stage.setY(screenBounds.getMinY());
            stage.setWidth(screenBounds.getWidth());
            stage.setHeight(screenBounds.getHeight());
            stage.show();

            Stage mainStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            mainStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
