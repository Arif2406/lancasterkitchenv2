package com.example.demo1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

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
                // Load main page scene
                Stage stage = (Stage) usernameField.getScene().getWindow();
                Parent root = FXMLLoader.load(getClass().getResource("MainPage.fxml"));
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Address Book");
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Show error message or clear fields
            usernameField.clear();
            passwordField.clear();
        }
    }
}