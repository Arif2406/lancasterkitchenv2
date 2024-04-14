package com.example.demo1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

public class MainPageController {

    @FXML
    private Label usernameLabel; // Label to display the username

    private String currentUser; // Variable to store the current user's username

    // Method to set the current user's username
    public void setUsername(String username) {
        this.currentUser = username;
        if (usernameLabel != null) {
            usernameLabel.setText("Logged in as: " + username);
        }
    }

    @FXML
    private void handleHomeButtonClick(ActionEvent event) {navigateToPage("MainPage.fxml", "Home", event);}

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
        // Assuming 'currentUserRole' holds the role of the logged-in user
        if (!"headchef".equals(currentUser) && !"souschef".equals(currentUser)) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Permission Denied");
            alert.setHeaderText(null);
            alert.setContentText("Not enough permissions to access this page.");
            alert.showAndWait();
        } else {
            navigateToPage("Menus.fxml", "Menus", event);
        }
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
        System.out.println("Stock button clicked.");
        navigateToPage("CurrentStock.fxml", "Stock", event);
    }


    @FXML
    private void handleNewDishButtonClick(ActionEvent event) {navigateToPage("AddNewDish.fxml", "Home", event);}

    @FXML
    private void handleNewRecipeButtonClick(ActionEvent event) {navigateToPage("AddNewRecipe.fxml", "AddNewRecipe", event);}

    @FXML
    private void handleNewMenuButtonClick(ActionEvent event) {navigateToPage("MainPage.fxml", "Home", event);}


    private void navigateToPage(String fxmlFile, String title, ActionEvent event) {
        System.out.println("Attempting to navigate to page: " + fxmlFile);
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        try {
            URL url = getClass().getResource(fxmlFile);
            if (url == null) {
                System.out.println("Resource not found: " + fxmlFile);
                return; // Early return if resource is not found
            }
            FXMLLoader loader = new FXMLLoader(url);
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
            System.out.println("Navigation successful.");
        } catch (Exception e) {
            System.out.println("Failed to load the FXML file: " + fxmlFile);
            e.printStackTrace();
        }
    }


}
