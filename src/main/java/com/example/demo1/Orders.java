package com.example.demo1;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Orders {

private Button OrdersButton;

    @FXML
    private void Order(ActionEvent event) {
        // Implement the action for viewing current dishes
        try {
            BorderPane root = (BorderPane) OrdersButton.getScene().getRoot();
            BorderPane pane = FXMLLoader.load(getClass().getResource("Orders.fxml"));
            root.setCenter(pane);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error loading.", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType error, String error1, String s, String message) {
    }


    @FXML
    private TableView<ObservableList<String>> ordersTable;

    @FXML
    private TableColumn<ObservableList<String>, String> dishNameColumn;
    @FXML
    private TableColumn<ObservableList<String>, String> quantityColumn;
    @FXML
    private TableColumn<ObservableList<String>, String> commentsColumn;
    @FXML
    private TableColumn<ObservableList<String>, String> orderNumberColumn;

    @FXML
    private Button updateStockButton;  // Button to trigger stock update

    public void initialize() {
        setupColumns();
        loadOrderData();
        setupButtonActions();
    }

    private void setupColumns() {
        dishNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(1)));
        quantityColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(2)));
        commentsColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(3)));
        orderNumberColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(4)));
    }

    private void loadOrderData() {
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        String query = "SELECT o.Order_ID, d.Name, o.Quantity, o.Comments, o.Order_Number " +
                "FROM in2033t02Order o JOIN in2033t02Dish d ON o.Dish_ID = d.Dish_ID";
        try (Connection connection = DatabaseUtil.connectToDatabase();
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    row.add(rs.getString(i));
                }
                data.add(row);
            }
            ordersTable.setItems(data);
        } catch (SQLException | ClassNotFoundException ex) {
            showAlert("Error loading orders from database.", ex.getMessage());
        }
    }

    private void setupButtonActions() {
        updateStockButton.setOnAction(this::updateStock);
    }

    @FXML
    private void processOrder(ActionEvent event) {
        try (Connection connection = DatabaseUtil.connectToDatabase()) {
            // Loop through each order item in the table
            for (ObservableList<String> order : ordersTable.getItems()) {
                int dishID = Integer.parseInt(order.get(0)); // Assuming the first column is the dish ID
                int quantity = Integer.parseInt(order.get(2)); // Assuming the third column is the quantity

                // Update ingredient stock and record the order
                updateIngredientStock(dishID, quantity, connection);
                recordOrder(order, connection);
            }
        } catch (Exception e) {
            showAlert("Error processing order.", e.getMessage());
        }
    }


    @FXML
    private void updateStock(ActionEvent event) {
        try (Connection connection = DatabaseUtil.connectToDatabase()) {
            for (ObservableList<String> order : ordersTable.getItems()) {
                int dishID = Integer.parseInt(order.get(0)); // Assuming the first column is the dish ID
                int quantity = Integer.parseInt(order.get(2)); // Assuming the third column is the quantity
                updateIngredientStock(dishID, quantity, connection);
            }
        } catch (Exception e) {
            showAlert("Error updating stock.", e.getMessage());
        }
    }

    private void updateIngredientStock(int dishID, int quantity, Connection connection) throws SQLException {
        String query = "SELECT r.Ingredient_ID, r.Quantity FROM in2033t02Recipe_Ingredients r " +
                "JOIN in2033t02Dish_Recipes dr ON r.Recipe_ID = dr.Recipe_ID WHERE dr.Dish_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, dishID);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int ingredientID = rs.getInt("Ingredient_ID");
                double requiredQuantity = rs.getDouble("Quantity") * quantity;
                deductStock(ingredientID, requiredQuantity, connection);
            }
        }
    }

    private void deductStock(int ingredientID, double quantity, Connection connection) throws SQLException {
        String updateQuery = "UPDATE in2033t02Ingredient SET Stock_Level = Stock_Level - ? WHERE Ingredient_ID = ?";
        try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
            updateStmt.setDouble(1, quantity);
            updateStmt.setInt(2, ingredientID);
            updateStmt.executeUpdate();
        }
    }


    private void recordOrder(ObservableList<String> order, Connection connection) throws SQLException {
        String insertQuery = "INSERT INTO in2033t02Order (Dish_ID, Quantity, Comments, Order_Number) VALUES (?, ?, ?, ?)";
        try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
            insertStmt.setInt(1, Integer.parseInt(order.get(0))); // Dish_ID
            insertStmt.setInt(2, Integer.parseInt(order.get(2))); // Quantity
            insertStmt.setString(3, order.get(3)); // Comments
            insertStmt.setString(4, order.get(4)); // Order_Number
            insertStmt.executeUpdate();
        }
    }
    private void showAlert(String message, String details) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        TextArea textArea = new TextArea(details);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }


    @FXML
    private void handleChefsButtonClick(ActionEvent event) {
        navigateToPage("Chefs.fxml", "Chefs", event);
    }

    private void navigateToPage(String s, String chefs, ActionEvent event) {
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
        navigateToPage("SupplierStock.fxml", "Stock", event);
    }

    @FXML
    private void handleStockButtonClick(ActionEvent event) {
        navigateToPage("Supplier.fxml", "Supplier", event);
    }

    @FXML
    private void handleHomeButtonClick(ActionEvent event) {
        navigateToPage("MainPage.fxml", "Home", event);
    }

    @FXML
    private void handleNewDishButtonClick(ActionEvent event) {navigateToPage("AddNewDish.fxml", "Home", event);}

    @FXML
    private void handleNewRecipeButtonClick(ActionEvent event) {navigateToPage("MainPage.fxml", "Home", event);}

    @FXML
    private void handleNewMenuButtonClick(ActionEvent event) {navigateToPage("MainPage.fxml", "Home", event);}

}




   