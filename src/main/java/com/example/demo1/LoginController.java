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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller class that handles user authentication for the application.
 */
public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private Map<String, String> validUsers = new HashMap<>();

    /**
     * Constructor for the controller, loads valid user credentials.
     */
    public LoginController() {
        loadValidUsers();
    }

    /**
     * Loads valid usernames and passwords from the database and stores them in a map.
     */
    private void loadValidUsers() {
        try {
            Connection connection = DatabaseUtil.connectToDatabase();
            System.out.println("Connecting to database...");

            String query = "SELECT Username, Password FROM in2033t02Chef";
            System.out.println("SQL Query: " + query);

            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String username = rs.getString("Username");
                String password = rs.getString("Password");
                validUsers.put(username, password);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert(AlertType.ERROR, "Database Error", "Error loading user credentials from database.", ex.getMessage());
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            showAlert(AlertType.ERROR, "Driver Not Found", "The database driver was not found.", ex.getMessage());
        }
    }


    @FXML
    private Label timeLabel;

    /**
     * Initialises the controller, including logging in and displaying time and date
     */
    public void initialize() {
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

    /**
     * Handles the login action and authentication.
     *
     * @param event The action event from the login button click.
     */
    @FXML
    protected void handleLoginButton(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (validUsers.containsKey(username) && validUsers.get(username).equals(password)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("MainPage.fxml"));
                Parent root = loader.load();
                MainPageController controller = loader.getController();
                controller.setUsername(username);
                setUpStage(root);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showErrorAlert();
            usernameField.clear();
            passwordField.clear();
        }
    }

    /**
     * Sets up the main stage and scene for the application after successful login.
     *
     * @param root The root parent for the scene.
     */
    private void setUpStage(Parent root) {
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
        stage.setTitle("Lancaster Kitchen");
        stage.show();
    }

    /**
     * Displays an error alert for unrecognised credentials.
     */
    private void showErrorAlert() {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setHeaderText(null);
        alert.setContentText("Credentials not recognised.");
        alert.showAndWait();
    }

    /**
     * Displays an alert.
     *
     * @param alertType The type of alert to display.
     * @param title     The title of the alert.
     * @param header    The header text of the alert.
     * @param content   The content text of the alert.
     */
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}