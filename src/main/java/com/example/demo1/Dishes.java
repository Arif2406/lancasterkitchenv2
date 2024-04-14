package com.example.demo1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class Dishes {

    @FXML
    private Button chefsButton;

    @FXML
    private Button wasteButton;

    @FXML
    private Button menusButton;

    @FXML
    private Button ordersButton;

    @FXML
    private Button dishesButton;

    @FXML
    private Button stockButton;

    @FXML
    private Button supplierButton;

    @FXML
    private Button viewCurrentDishesButton;

    @FXML
    private Button addNewDishButton;

    @FXML
    private Button viewPendingDishesButton;

    @FXML
    private Button viewAllDishesButton;

    @FXML
    private Button homeButton;


    @FXML
    private void handleViewCurrentDishes(ActionEvent event) {
        // Implement the action for viewing current dishes

        navigateToPage("dishList.fxml", "Dishes", event);
    }





    @FXML
    private void handleAddNewDish(ActionEvent event) {
        // Implement the action for adding a new dish
        // Add your implementation here
        navigateToPage("AddNewDish.fxml", "AddNewDish", event);

    }

    @FXML
    private void handleViewPendingDishes(ActionEvent event) {
        // Implement the action for viewing pending dishes
        // Add your implementation here
    }

    @FXML
    private void handleViewAllDishes(ActionEvent event) {
        // Implement the action for viewing all dishes
        // Add your implementation here
    }

    @FXML
    private void handleHomeButton(ActionEvent event) {
        // Implement the action for handling home button click
        // Add your implementation here
    }

    // Add any additional methods and fields as needed
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
    private void showAlert(Alert.AlertType alertType, String title, String message, String details) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
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

}
