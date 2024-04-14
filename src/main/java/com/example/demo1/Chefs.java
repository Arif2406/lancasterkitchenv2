package com.example.demo1;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Chefs {

    @FXML
    private TableView<Object[]> table;

    @FXML
    private TableColumn<Object[], String> nameColumn;

    @FXML
    private TableColumn<Object[], String> roleColumn;

    @FXML
    private TableColumn<Object[], String> idColumn;

    @FXML
    private TextField nameField;

    @FXML
    private TextField roleField;

    public void initialize() {
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[0].toString()));
        roleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[1].toString()));


        loadChefData();
    }

    private void loadChefData() {
        try {
            Connection connection = DatabaseUtil.connectToDatabase();
            System.out.println("Connecting to database...");

            // Adjusted query to select only the non-sensitive data
            String query = "SELECT Name, Role FROM in2033t02Chef";
            System.out.println("SQL Query: " + query);

            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            // Assuming that the first column is Chef_Name and the second column is Chef_Role
            // These indexes start at 0 because when you store the results in the Object array, you start with index 0
            nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[0].toString()));
            roleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[1].toString()));

            // Remove the ID column from the display, as it is now not being queried
            idColumn.setVisible(false);

            while (rs.next()) {
                // Adjusted to fit the new query, now only two columns
                Object[] row = new Object[2];
                for (int i = 1; i <= 2; i++) {
                    row[i - 1] = rs.getString(i);
                }
                table.getItems().add(row);
            }
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Error loading chefs from database.", ex.getMessage());
        }
    }


    // Event handler methods for menu buttons
    @FXML
    private void handleChefsButtonClick() {
        // Handle Chefs button click
    }

    @FXML
    private void handleWasteButtonClick() {
        // Handle Waste button click
    }

    @FXML
    private void handleMenusButtonClick() {
        // Handle Menus button click
    }

    @FXML
    private void handleOrdersButtonClick() {
        // Handle Orders button click
    }

    @FXML
    private void handleDishesButtonClick() {
        // Handle Dishes button click
    }

    @FXML
    private void handleStockButtonClick() {
        // Handle Stock button click
    }

    private void showAlert(AlertType alertType, String title, String message, String details) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (details != null) {
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
}
