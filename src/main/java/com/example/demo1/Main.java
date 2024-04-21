package com.example.demo1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Class for loading and displaying the primary stage and scene.
 */
public class Main extends Application {

    /**
     * The main entry point of the JavaFX Application
     *
     * @param primaryStage The primary stage where the application scene can be set.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
        primaryStage.setTitle("Login");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    /**
     * Launches the application by calling the launch method
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        launch(args);
    }
}
