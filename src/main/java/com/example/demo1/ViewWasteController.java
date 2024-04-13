package com.example.demo1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ViewWasteController {


    @FXML
    private TableView<Object[]> table;


    @FXML
    private TableColumn<Object[], String> dateColumn;

    @FXML
    private TableColumn<Object[], String> ingredientColumn;

    @FXML
    private TableColumn<Object[], String> quantityColumn;

    @FXML
    private TableColumn<Object[], String> unitColumn;

    @FXML
    private TableColumn<Object[], String> reasonColumn;

    @FXML
    private void handleChefsButtonClick(ActionEvent event) {
        navigateToPage("Chefs.fxml", "Chefs", event);
    }

    @FXML
    private void handleWasteButtonClick(ActionEvent event) {
        navigateToPage("Waste.fxml", "Waste", event);
    }

    @FXML
    private void handleMenusButtonClick(ActionEvent event) {
        navigateToPage("Menus.fxml", "Menus", event);
    }

    @FXML
    private void handleOrdersButtonClick(ActionEvent event) {
        navigateToPage("Orders.fxml", "Orders", event);
    }

    @FXML
    private void handleDishesButtonClick(ActionEvent event) {
        navigateToPage("Dishes.fxml", "Dishes", event);
    }

    @FXML
    private void handleStockButtonClick(ActionEvent event) {
        navigateToPage("SupplierStock.fxml", "Stock", event);
    }


    public void initialize() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        ingredientColumn.setCellValueFactory(new PropertyValueFactory<>("ingredient"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
        reasonColumn.setCellValueFactory(new PropertyValueFactory<>("reason"));

        loadWasteData();
    }

    private void loadWasteData() {
        try {
            Connection connection = DatabaseUtil.connectToDatabase();
            String query = "SELECT w.Date_of_Waste, i.Name, w.Quantity_Wasted, w.Unit, w.Reason FROM in2033t02Waste_Log w INNER JOIN in2033t02Ingredient i ON w.Ingredient_ID = i.Ingredient_ID";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            table.getItems().clear();

            while (rs.next()) {
                String dateOfWaste = rs.getString("Date_of_Waste");
                String ingredientName = rs.getString("Name");
                String quantityWasted = rs.getString("Quantity_Wasted");
                String unit = rs.getString("Unit");
                String reason = rs.getString("Reason");
                Object[] row = {dateOfWaste, ingredientName, quantityWasted, unit, reason};
                table.getItems().add(row);
            }
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Error loading waste data from database.", ex.getMessage());
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

    @FXML
    private void addWaste() {
        // Implement addWaste method logic here
    }

    @FXML
    private void returnToMainMenu() {
        // Implement returnToMainMenu method logic here
    }


    private void navigateToPage(String fxmlFile, String title, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
            Stage mainStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            mainStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
