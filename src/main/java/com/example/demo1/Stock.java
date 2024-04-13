package com.example.demo1;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Stock {

    @FXML
    private TableView<ObservableList<String>> stockTable;

    // Explicitly declaring columns
    @FXML
    private TableColumn<ObservableList<String>, String> ingredientNameColumn;
    @FXML
    private TableColumn<ObservableList<String>, String> quantityColumn;
    @FXML
    private TableColumn<ObservableList<String>, String> unitColumn;
    @FXML
    private TableColumn<ObservableList<String>, String> deliveryDateColumn;

    @FXML
    public void initialize() {
        // You might want to set up columns only once and not re-add them
        setupColumns();
        loadStockData();
    }

    private void loadStockData() {
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        String query = "SELECT s.Stock_ID, i.Name, s.Received_Quantity, s.Unit, s.Delivery_Date " +
                "FROM in2033t02Supplier_Stock s JOIN in2033t02Ingredient i ON s.Ingredient_ID = i.Ingredient_ID";
        try (Connection connection = DatabaseUtil.connectToDatabase();
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            // Clear previous data
            stockTable.getItems().clear();

            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 2; i <= rs.getMetaData().getColumnCount(); i++) {  // Start from 2 to skip Stock_ID
                    row.add(rs.getString(i));
                }
                data.add(row);
            }
            stockTable.setItems(data);
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error loading stock data from database.", ex.getMessage());
        }
    }

    private void setupColumns() {
        // Assuming columns are properly initialized in FXML and do not need to be added dynamically
        ingredientNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(0)));
        quantityColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(1)));
        unitColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(2)));
        deliveryDateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(3)));
    }


    private void showAlert(Alert.AlertType alertType, String title, String message, String details) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (details != null && !details.isEmpty()) {
            Label label = new Label("Details:");
            TextArea textArea = new TextArea(details);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            VBox vBox = new VBox(label, textArea);
            alert.getDialogPane().setExpandableContent(vBox);
        }
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
    private void handleStockButtonClick(ActionEvent event) {
        navigateToPage("SupplierStock.fxml", "Stock", event);
    }

    private void navigateToPage(String fxmlFile, String title, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
            Stage mainStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            mainStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
