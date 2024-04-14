package com.example.demo1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class Menus {
    @FXML
    private Button viewMenuButton;
    private Button BackButton;

    @FXML
    private void Menus(ActionEvent event) {
        // Implement the action for viewing current dishes
        try {
            BorderPane root = (BorderPane) viewMenuButton.getScene().getRoot();
            BorderPane pane = FXMLLoader.load(getClass().getResource("Menus.fxml"));
            root.setCenter(pane);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error loading.", e.getMessage());
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