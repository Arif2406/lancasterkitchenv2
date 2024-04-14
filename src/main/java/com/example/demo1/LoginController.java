package com.example.demo1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

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
    private Label timeLabel;

    public void initialize() {
        // Update time label every second
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String timeText = timeFormatter.format(now);
            String dateText = dateFormatter.format(now);
            timeLabel.setText("Time: " + timeText + "   Date: " + dateText);
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
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
                Rectangle2D screenBounds = Screen.getPrimary().getBounds();
                Stage stage = (Stage) usernameField.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setFullScreen(true);
                stage.setFullScreenExitHint("");
                stage.setX(screenBounds.getMinX());
                stage.setY(screenBounds.getMinY());
                stage.setWidth(screenBounds.getWidth());
                stage.setHeight(screenBounds.getHeight());
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