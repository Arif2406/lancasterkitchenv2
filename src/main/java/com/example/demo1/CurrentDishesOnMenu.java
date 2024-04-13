package com.example.demo1;

import com.example.demo1.DatabaseUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CurrentDishesOnMenu {

    @FXML
    private ListView<String> dishList;

    public void initialize() {
        try {
            System.out.println("1");

            populateDishesList();
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("2");

            ex.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Error loading dishes from database.", ex.getMessage());
        }

        dishList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                try {
                    System.out.println("3");

                    openDishInformation(newValue);
                } catch (ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                    System.out.println("4");

                    showAlert(AlertType.ERROR, "Error", "Error opening dish information.", e.getMessage());
                }
            }
        });
    }

    private void populateDishesList() throws SQLException, ClassNotFoundException {
        System.out.println("is it here");

        Connection connection = DatabaseUtil.connectToDatabase();
        String query = "SELECT Name FROM in2033t02Dish";
        System.out.println("is it here two");

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            System.out.println("no?");

            ObservableList<String> dishes = FXCollections.observableArrayList();
            while (rs.next()) {
                System.out.println("ok");

                dishes.add(rs.getString("Name"));
            }
            System.out.println("wow");

            dishList.setItems(dishes);
        }
    }

    private void openDishInformation(String selectedDish) throws SQLException, ClassNotFoundException {
        System.out.println("wow");

        Connection connection = DatabaseUtil.connectToDatabase();
        System.out.println("b");

        String query = "SELECT Dish_ID FROM in2033t02Dish WHERE Name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            System.out.println("g");

            stmt.setString(1, selectedDish);
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("s");

                if (rs.next()) {
                    System.out.println("e");

                    // int dishID = rs.getInt("Dish_ID");
                    //       DishInformation dishInformation = new DishInformation(dishID, selectedDish);
                    //     dishInformation.setVisible(true);
                }
            }
        }
    }

    private void showAlert(AlertType alertType, String title, String message, String details) {
        System.out.println("d");

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
            System.out.println("b");

        }
        alert.showAndWait();
        System.out.println("r");

    }
}

