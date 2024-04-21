package com.example.demo1;

import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Controller class for the menus page.
 */
public class Menus {

    /**
     * Table view for displaying current menu items.
     */
    @FXML
    private TableView<Object[]> currentMenuTable;

    /**
     * Table column for displaying dish IDs.
     */
    @FXML
    private TableColumn<Object[], String> dishIdColumn;

    /**
     * Table column for displaying dish names.
     */
    @FXML
    private TableColumn<Object[], String> nameColumn;

    /**
     * Table column for displaying dish courses.
     */
    @FXML
    private TableColumn<Object[], String> CourseColumn;

    /**
     * Table column for displaying dish statuses.
     */
    @FXML
    private TableColumn<Object[], String> statusColumn;

    /**
     * Initialises the controller class, creating a table and loading menu data.
     */
    public void initialize() {
        dishIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[0].toString()));
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[1].toString()));
        CourseColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[2].toString()));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[3].toString()));

        loadMenuData();
    }

    /**
     * Loads the menu data from the database and adds "In Use" dishes to the current menu table.
     */
    private void loadMenuData() {
        try {
            Connection connection = DatabaseUtil.connectToDatabase();
            System.out.println("Connecting to database...");


            String query = "SELECT Dish_ID, Name, Course, Status FROM in2033t02Dish WHERE Status = 'In use'";
            System.out.println("SQL Query: " + query);

            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = new Object[4];
                for (int i = 1; i <= 4; i++) {
                    row[i - 1] = rs.getString(i);
                }
                currentMenuTable.getItems().add(row);
            }
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Error loading menu data from database.", ex.getMessage());
        }
    }

    /**
     * Displays an alert.
     *
     * @param alertType The type of alert
     * @param title     The title of the alert
     * @param message   The message of the alert
     * @param details   The details of the alert
     */
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


    /**
     * Takes you to the chefs page when chefs button is clicked.
     */
    @FXML
    private void handleChefsButtonClick(ActionEvent event) {
        navigateToPage("Chefs.fxml", "Chefs", event);
    }

    /**
     * Takes you to the waste page when waste button is clicked.
     */
    @FXML
    private void handleWasteButtonClick(ActionEvent event) {
        navigateToPage("Waste.fxml", "Waste", event);
    }

    /**
     * Takes you to the menus page when menus button is clicked.
     */
    @FXML
    private void handleMenusButtonClick(ActionEvent event) {
        navigateToPage("Menus.fxml", "Menus", event);
    }

    /**
     * Takes you to the orders/home page when orders button is clicked.
     */
    @FXML
    private void handleOrdersButtonClick(ActionEvent event) {
        navigateToPage("Orders.fxml", "Orders", event);
    }

    /**
     * Takes you to the dishes page when dishes button is clicked.
     */
    @FXML
    private void handleDishesButtonClick(ActionEvent event) {
        navigateToPage("Dishes.fxml", "Dishes", event);
    }

    /**
     * Takes you to the supplier page when supplier button is clicked.
     */
    @FXML
    private void handleSupplierButtonClick(ActionEvent event) {
        navigateToPage("SupplierStock.fxml", "Supplier", event);
    }

    /**
     * Takes you to the stock page when stock button is clicked.
     */
    @FXML
    private void handleStockButtonClick(ActionEvent event) {
        navigateToPage("CurrentStock.fxml", "Stock", event);
    }

    /**
     * Takes you to the home page when stock button is clicked.
     */
    @FXML
    private void handleHomeButtonClick(ActionEvent event) {navigateToPage("MainPage.fxml", "Home", event);}

    /**
     * Takes you to the new dishes page when add new dish button is clicked.
     */
    @FXML
    private void handleNewDishButtonClick(ActionEvent event) {navigateToPage("AddNewDish.fxml", "Home", event);}

    /**
     * Takes you to the new recipe page when add new recipe button is clicked.
     */
    @FXML
    private void handleNewRecipeButtonClick(ActionEvent event) {navigateToPage("AddNewRecipe.fxml", "AddNewRecipe", event);}

    /**
     * Takes you to the new menu page when add new menu button is clicked.
     */
    @FXML
    private void handleNewMenuButtonClick(ActionEvent event) {navigateToPage("AddNewMenu.fxml", "Home", event);}

    /**
     * Navigates to the relevant FXML page when the button is clicked.
     *
     * @param fxmlFile The name of FXML file to navigate to
     * @param title    The title of the page
     * @param event    The action event
     */
    private void navigateToPage(String fxmlFile, String title, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);

            stage.setMaximized(true);

            stage.show();


            Stage mainStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            mainStage.close();


            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), scene.getRoot());
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

        } catch (IOException e) {

            System.err.println("Failed to load the FXML file: " + fxmlFile);
            e.printStackTrace();
        }
    }

}

