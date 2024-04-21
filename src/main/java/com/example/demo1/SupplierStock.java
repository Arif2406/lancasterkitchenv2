package com.example.demo1;

import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Controller class for the suppliers page.
 */
public class SupplierStock {

    @FXML
    private TableView<ObservableList<String>> stockTable;


    @FXML
    private TableColumn<ObservableList<String>, String> ingredientNameColumn;
    @FXML
    private TableColumn<ObservableList<String>, String> quantityColumn;
    @FXML
    private TableColumn<ObservableList<String>, String> unitColumn;
    @FXML
    private TableColumn<ObservableList<String>, String> deliveryDateColumn;

    /**
     * Initialises the controller, populating table with stock data from database.
     */
    @FXML
    public void initialize() {

        setupColumns();
        loadStockData();
    }

    /**
     * Loads stock data from the database and adds it to the table view.
     */
    private void loadStockData() {
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        String query = "SELECT s.Stock_ID, i.Name, s.Received_Quantity, s.Unit, s.Delivery_Date " +
                "FROM in2033t02Supplier_Stock s JOIN in2033t02Ingredient i ON s.Ingredient_ID = i.Ingredient_ID";
        try (Connection connection = DatabaseUtil.connectToDatabase();
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {


            stockTable.getItems().clear();

            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 2; i <= rs.getMetaData().getColumnCount(); i++) {
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

    /**
     * Configures the table columns that display the data.
     */
    private void setupColumns() {

        ingredientNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(0)));
        quantityColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(1)));
        unitColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(2)));
        deliveryDateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(3)));
    }

    /**
     * Displays an alert.
     *
     * @param alertType The type of alert
     * @param title     The title of the alert
     * @param message   The message of the alert
     * @param details   The details of the alert
     */
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

    /**
     * Takes you to the chefs page when chefs button is clicked.
     */
    @FXML
    private void handleChefsButtonClick(ActionEvent event) {
        navigateToPage("Chefs.fxml", "Chefs", event);
    }

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
        navigateToPage("Menus.fxml", "Menus", event);
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
    private void handleSupplierButtonClick(ActionEvent event) {
        navigateToPage("SupplierStock.fxml", "Supplier", event);
    }

    /**
     * Takes you to the stock page when stock button is clicked.
     */
    @FXML
    private void handleStockButtonClick(ActionEvent event) {
        navigateToPage("CurrentStock.fxml", "Stock", event);
    }

    /**
     * Takes you to the home page when stock button is clicked.
     */
    @FXML
    private void handleHomeButtonClick(ActionEvent event) {
        navigateToPage("MainPage.fxml", "Home", event);
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
    private void handleNewRecipeButtonClick(ActionEvent event) {navigateToPage("AddNewRecipe.fxml", "AddNewRecipe", event);}

    /**
     * Takes you to the new menu page when add new menu button is clicked.
     */
    @FXML
    private void handleNewMenuButtonClick(ActionEvent event) {navigateToPage("AddNewMenu.fxml", "Home", event);}

    /**
     * Navigates to the relevant FXML page when the button is clicked.
     *
     * @param fxmlFile The name of FXML file to navigate to
     * @param title    The title of the page
     * @param event    The action event
     */
    private void navigateToPage(String fxmlFile, String title, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);

            stage.setMaximized(true);

            stage.show();


            Stage mainStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            mainStage.close();


            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), scene.getRoot());
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

        } catch (IOException e) {

            System.err.println("Failed to load the FXML file: " + fxmlFile);
            e.printStackTrace();
        }
    }


}
