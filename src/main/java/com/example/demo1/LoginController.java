package com.example.demo1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private Map<String, String> validUsers = new HashMap<>();

    public LoginController() {
        // Define valid usernames and passwords
        validUsers.put("headchef", "pass");
        validUsers.put("souschef", "pass");
        validUsers.put("user", "pass");
    }

    @FXML
    protected void handleLoginButton(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Check if the username and password are correct
        if (validUsers.containsKey(username) && validUsers.get(username).equals(password)) {
            try {
                // Load main page scene with FXMLLoader to access its controller
                FXMLLoader loader = new FXMLLoader(getClass().getResource("MainPage.fxml"));
                Parent root = loader.load();

                // Get the MainPageController from the loader
                MainPageController controller = loader.getController();

                // Set the username on the MainPageController
                controller.setUsername(username);

                // Setting up the scene and stage
                Stage stage = (Stage) usernameField.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Address Book");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Show an error message if credentials are not recognized
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Login Error");
            alert.setHeaderText(null);
            alert.setContentText("Credentials not recognized.");
            alert.showAndWait();

            // Optionally, clear the fields after showing the alert
            usernameField.clear();
            passwordField.clear();
        }
    }
}