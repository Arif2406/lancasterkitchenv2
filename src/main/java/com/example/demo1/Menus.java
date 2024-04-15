package com.example.demo1;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Menus {

    @FXML
    private TableView<Object[]> currentMenuTable;

    @FXML
    private TableColumn<Object[], String> dishIdColumn;

    @FXML
    private TableColumn<Object[], String> nameColumn;

    @FXML
    private TableColumn<Object[], String> courseColumn;

    @FXML
    private TableColumn<Object[], String> statusColumn;

    @FXML
    private TextField dishIdField;

    @FXML
    private TextField nameField;

    @FXML
    private TextField courseField;

    @FXML
    private TextField statusField;

    public void initialize() {
        dishIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[0].toString()));
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[0].toString()));
        courseColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[1].toString()));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[1].toString()));


        loadMenuData();
    }

    private void loadMenuData() {
        try {
            Connection connection = DatabaseUtil.connectToDatabase();
            System.out.println("Connecting to database...");

            // retrieve data from database
            // Adjusted query to select only the non-sensitive data
            String query = "SELECT Dish_ID, Name, Course, Status FROM in2033t02Dish";
            System.out.println("SQL Query: " + query);

            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            // These indexes start at 0 because when you store the results in the Object array, you start with index 0
            dishIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[0].toString()));
            nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[1].toString()));
            courseColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[2].toString()));
            statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[3].toString()));

            // Remove the ID column from the display, as it is now not being queried
            dishIdColumn.setVisible(false);

            while (rs.next()) {
                // Adjusted to fit the new query, now only two columns
                Object[] row = new Object[2];
                for (int i = 1; i <= 2; i++) {
                    row[i - 1] = rs.getString(i);
                }
                currentMenuTable.getItems().add(row);
            }
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Error loading chefs from database.", ex.getMessage());
        }
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
    private void handleNewRecipeButtonClick(ActionEvent event) {navigateToPage("MainPage.fxml", "Home", event);}

    @FXML
    private void handleNewMenuButtonClick(ActionEvent event) {navigateToPage("MainPage.fxml", "Home", event);}

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
            // Close the current (main) stage after opening the new one
            Stage mainStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            mainStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

