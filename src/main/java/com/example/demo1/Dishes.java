package com.example.demo1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;

import java.awt.*;
import java.io.IOException;

public class Dishes {

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
        try {
            BorderPane root = (BorderPane) viewCurrentDishesButton.getScene().getRoot();
            BorderPane pane = FXMLLoader.load(getClass().getResource("dishList.fxml"));
            root.setCenter(pane);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error loading Current Dishes on Menu.", e.getMessage());
        }
    }


    // Add action methods for other buttons as needed

    private void showAlert(Alert.AlertType alertType, String title, String message, String details) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
