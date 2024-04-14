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
    private void handleChefsButtonClick(ActionEvent event) {
        // Implement the action for handling chefs button click
        // For now, let's just print a message to the console
        System.out.println("Chefs button clicked!");
    }

    @FXML
    private void handleWasteButtonClick(ActionEvent event) {
        // Implement the action for handling waste button click
        // For now, let's just print a message to the console
        System.out.println("Waste button clicked!");
    }

    @FXML
    private void handleMenusButtonClick(ActionEvent event) {
        // Implement the action for handling menus button click
        // For now, let's just print a message to the console
        System.out.println("Menus button clicked!");
    }

    @FXML
    private void handleOrdersButtonClick(ActionEvent event) {
        // Implement the action for handling orders button click
        // For now, let's just print a message to the console
        System.out.println("Orders button clicked!");
    }

    @FXML
    private void handleDishesButtonClick(ActionEvent event) {
        // Implement the action for handling dishes button click
        // For now, let's just print a message to the console
        System.out.println("Dishes button clicked!");
    }

    @FXML
    private void handleStockButtonClick(ActionEvent event) {
        // Implement the action for handling stock button click
        // For now, let's just print a message to the console
        System.out.println("Stock button clicked!");
    }

    @FXML
    private void handleSupplierButtonClick(ActionEvent event) {
        // Implement the action for handling supplier button click
        // For now, let's just print a message to the console
        System.out.println("Supplier button clicked!");
    }

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
}
