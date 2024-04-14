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

public class Menus {

    @FXML
    private Button addNewMenu;

    @FXML
    private Button viewCurrentMenu;

    @FXML
    private Button viewPreviousMenus;


    // Add action methods for other buttons as needed

    private void showAlert(Alert.AlertType alertType, String title, String message, String details) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}