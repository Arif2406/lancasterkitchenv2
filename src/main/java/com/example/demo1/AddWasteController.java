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

/**
 * Controller class for adding new waste to the system.
 */
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

    /**
     * Initialises the controller.
     */
    @FXML
    private void initialize() {
        System.out.println("Initializing controller...");
        populateIngredientComboBox();
        ingredientComboBox.setOnAction(this::updateUnitField);
    }

    /**
     * Populates the ingredient combo box with data from the database.
     */
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

    /**
     * Updates the unit field based on the selected ingredient.
     *
     * @param event The ActionEvent triggered by selecting an ingredient.
     */
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

    /**
     * Submits the waste entry to the database.
     *
     * @param event The ActionEvent triggered by the submit button.
     */
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

    /**
     * Retrieves the current stock level of an ingredient from the database.
     *
     * @param ingredientName The name of the ingredient.
     * @param connection The database connection.
     * @return The current stock level of the ingredient.
     * @throws SQLException If a database access error occurs.
     */
    private int getCurrentStock(String ingredientName, Connection connection) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT Stock_Level FROM in2033t02Ingredient WHERE Name = ?");
        stmt.setString(1, ingredientName);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("Stock_Level");
        }
        return 0;
    }

    /**
     * Updates the stock level of an ingredient in the database.
     *
     * @param ingredientName The name of the ingredient.
     * @param newStock The new stock level of the ingredient.
     * @param connection The database connection.
     * @throws SQLException If a database access error occurs.
     */
    private void updateIngredientStock(String ingredientName, int newStock, Connection connection) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("UPDATE in2033t02Ingredient SET Stock_Level = ? WHERE Name = ?");
        stmt.setInt(1, newStock);
        stmt.setString(2, ingredientName);
        stmt.executeUpdate();
    }

    /**
     * Displays an alert.
     *
     * @param type      The type of alert
     * @param title     The title of the alert
     * @param header    The header of the alert
     * @param content   The details of the alert
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Takes you to the chefs page when chefs button is clicked.
     */
    @FXML
    private void handleChefsButtonClick(ActionEvent event) {navigateToPage("Chefs.fxml", "Chefs", event);}

    /**
     * Takes you to the waste page when waste button is clicked.
     */
    @FXML
    private void handleWasteButtonClick(ActionEvent event) {
        navigateToPage("Waste.fxml", "Waste", event);
    }

    /**
     * Takes you to the menus page when menus button is clicked.
     */
    @FXML
    private void handleMenusButtonClick(ActionEvent event) {
        navigateToPage("Home.fxml", "Menus", event);
    }

    /**
     * Takes you to the orders/home page when orders button is clicked.
     */
    @FXML
    private void handleOrdersButtonClick(ActionEvent event) {
        navigateToPage("Orders.fxml", "Orders", event);
    }

    /**
     * Takes you to the dishes page when dishes button is clicked.
     */
    @FXML
    private void handleDishesButtonClick(ActionEvent event) {
        navigateToPage("Dishes.fxml", "Dishes", event);
    }

    /**
     * Takes you to the supplier page when supplier button is clicked.
     */
    @FXML
    private void handleSupplierButtonClick(ActionEvent event) { navigateToPage("SupplierStock.fxml", "Supplier", event);}

    /**
     * Takes you to the stock page when stock button is clicked.
     */
    @FXML
    private void handleStockButtonClick(ActionEvent event) {
        navigateToPage("CurrentStock.fxml", "Stock", event);
    }

    /**
     * Takes you to the new dishes page when add new dish button is clicked.
     */
    @FXML
    private void handleNewDishButtonClick(ActionEvent event) {navigateToPage("AddNewDish.fxml", "Home", event);}

    /**
     * Takes you to the new recipe page when add new recipe button is clicked.
     */
    @FXML
    private void handleNewRecipeButtonClick(ActionEvent event) {navigateToPage("AddNewRecipe.fxml", "Home", event);}

    /**
     * Takes you to the new menu page when add new menu button is clicked.
     */
    @FXML
    private void handleNewMenuButtonClick(ActionEvent event) {navigateToPage("AddNewMenu.fxml", "Home", event);}

    /**
     * Takes you to the home page when stock button is clicked.
     */
    @FXML
    private void handleHomeButtonClick(ActionEvent event) {navigateToPage("MainPage.fxml", "Home", event);}

    /**
     * Takes you to the add new waste page when add waste button is clicked.
     */
    @FXML
    private void handleNewWaste(ActionEvent event) {navigateToPage("AddWaste.fxml", "Home", event);}

    /**
     * Navigates to the relevant FXML page.
     *
     * @param fxmlFile The name of FXML file to navigate to
     * @param title    The title of the page
     * @param event    The action event
     */
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
