package com.example.demo1;

import com.example.demo1.DatabaseUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CurrentDishesOnMenu {

    @FXML
    private ListView<String> dishList;

    @FXML
    private void handleDishSelected() {
        String selectedDish = dishList.getSelectionModel().getSelectedItem();
        if (selectedDish != null) {

            try {
                // Load the DishInformation.fxml file and show the scene
                FXMLLoader loader = new FXMLLoader(getClass().getResource("DishDetails.fxml"));
                Parent root = loader.load();
                DishInformationController controller = loader.getController();
                controller.initData(selectedDish); // Pass the selected dish to the controller
                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Error opening dish information.", e.getMessage());
            }
        }
    }



public void initialize() {
        try {

            populateDishesList();
        } catch (SQLException | ClassNotFoundException ex) {

            ex.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Error loading dishes from database.", ex.getMessage());
        }

        dishList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                try {

                    openDishInformation(newValue);
                } catch (ClassNotFoundException | SQLException e) {
                    e.printStackTrace();

                    showAlert(AlertType.ERROR, "Error", "Error opening dish information.", e.getMessage());
                }
            }
        });
    }

    private void populateDishesList() throws SQLException, ClassNotFoundException {

        Connection connection = DatabaseUtil.connectToDatabase();
        String query = "SELECT Name FROM in2033t02Dish";

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            ObservableList<String> dishes = FXCollections.observableArrayList();
            while (rs.next()) {

                dishes.add(rs.getString("Name"));
            }

            dishList.setItems(dishes);
        }
    }

    private void openDishInformation(String selectedDish) throws SQLException, ClassNotFoundException {

        Connection connection = DatabaseUtil.connectToDatabase();

        String query = "SELECT Dish_ID FROM in2033t02Dish WHERE Name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, selectedDish);
            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    // int dishID = rs.getInt("Dish_ID");
                    //       DishInformation dishInformation = new DishInformation(dishID, selectedDish);
                    //     dishInformation.setVisible(true);
                }
            }
        }
    }

    private void showAlert(AlertType alertType, String title, String message, String details) {

        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (details != null) {
            Label label = new Label("Details:");
            TextArea textArea = new TextArea(details);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            VBox vBox = new VBox(label, textArea);
            alert.getDialogPane().setExpandableContent(vBox);

        }
        alert.showAndWait();

    }

}

