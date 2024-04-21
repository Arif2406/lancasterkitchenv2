package com.example.demo1;

import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Controller class for the pending dishes page.
 */
public class PendingDishesController {

    @FXML
    private TableColumn<Recipe, String> nameColumn;

    @FXML
    private ListView<String> dishList;

    @FXML
    private Label nameLabel;

    @FXML
    private Label courseLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label chefLabel;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextArea stepsTextArea;

    @FXML
    private Label rnameLabel;

    @FXML
    private TextArea rdescriptionArea;


    @FXML
    private TextArea rstepsTextArea;
    @FXML
    private TableView<Recipe> recipeTable;

    /**
     * Initialises the controller class, populating table with pending dishes from database.
     */
    public void initialize() {

        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));

        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));


        try {
            populateDishesList();
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error loading dishes from database.", ex.getMessage());
        }


        dishList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                try {
                    openDishInformation(newValue);

                } catch (ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Error opening dish information.", e.getMessage());
                }
            }
        });
        recipeTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {

                fetchAndDisplayRecipeDescription(newValue.getRecipeID());
            }
        });
    }

    /**
     * Populates the list of dishes with status 'Pending' from the database.
     * @throws SQLException if a database access error occurs.
     * @throws ClassNotFoundException if the database driver is not found.
     */
    private void populateDishesList() throws SQLException, ClassNotFoundException {
        Connection connection = DatabaseUtil.connectToDatabase();

        String query = "SELECT Name FROM in2033t02Dish WHERE Status = 'Pending'";

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            ObservableList<String> dishes = FXCollections.observableArrayList();
            while (rs.next()) {
                dishes.add(rs.getString("Name"));
            }
            dishList.setItems(dishes);
        }
    }

    /**
     * Opens and displays detailed information for the selected dish.
     * @param selectedDish  the dish to display information for.
     * @throws SQLException if a database access error occurs.
     * @throws ClassNotFoundException if the database driver is not found.
     */
    private void openDishInformation(String selectedDish) throws SQLException, ClassNotFoundException {
        Connection connection = DatabaseUtil.connectToDatabase();
        String query = "SELECT d.Course, d.Status, d.Description, d.Chef_Creator_ID, s.Step_Description " +
                "FROM in2033t02Dish d " +
                "LEFT JOIN in2033t02Dish_Steps s ON d.Dish_ID = s.Dish_ID " +
                "WHERE d.Name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, selectedDish);
            try (ResultSet rs = stmt.executeQuery()) {
                StringBuilder stepDescriptions = new StringBuilder();
                while (rs.next()) {
                    nameLabel.setText(selectedDish);
                    courseLabel.setText("Course: " + rs.getString("Course"));
                    statusLabel.setText("Status: " + rs.getString("Status"));
                    descriptionArea.setText("Description: " + rs.getString("Description"));
                    chefLabel.setText("Chef ID: " + rs.getString("Chef_Creator_ID"));
                    stepDescriptions.append(rs.getString("Step_Description")).append("\n");
                }
                stepsTextArea.setText("Steps: \n" + stepDescriptions.toString());


                fetchAndDisplayRecipeNames(selectedDish);
            }
        }
    }

    /**
     * Fetches and displays the description of a recipe given its ID.
     * @param recipeID the ID of the recipe to fetch.
     */
    private void fetchAndDisplayRecipeDescription(int recipeID) {
        try {

            Connection connection = DatabaseUtil.connectToDatabase();
            String query = "SELECT Name, Status, Review_Date, Description FROM in2033t02Recipe WHERE Recipe_ID = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, recipeID);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {

                        String name = rs.getString("Name");
                        String status = rs.getString("Status");
                        String reviewDate = rs.getString("Review_Date");
                        String description = rs.getString("Description");


                        rnameLabel.setText("Name: " + name);

                        rdescriptionArea.setText("Description: " + description);

                        fetchAndDisplayRecipeSteps(recipeID);
                    }
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error fetching recipe details.", ex.getMessage());
        }
    }

    /**
     * Fetches and displays the steps for a recipe given its ID.
     * @param recipeID the ID of the recipe whose steps are to be displayed.
     */
    private void fetchAndDisplayRecipeSteps(int recipeID) {
        try {

            Connection connection = DatabaseUtil.connectToDatabase();
            String query = "SELECT Step_Description FROM in2033t02Recipe_Steps WHERE Recipe_ID = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, recipeID);
                try (ResultSet rs = stmt.executeQuery()) {
                    StringBuilder steps = new StringBuilder();
                    while (rs.next()) {
                        steps.append(rs.getString("Step_Description")).append("\n");
                    }

                    rstepsTextArea.setText("Steps: \n" + steps.toString());
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error fetching recipe steps.", ex.getMessage());
        }
    }

    /**
     * Fetches and displays the names and descriptions of recipes associated with a selected dish.
     *
     * @param selectedDish The name of the dish for which recipes need to be fetched.
     * @throws SQLException If a database access error occurs or this method is called on a closed connection.
     * @throws ClassNotFoundException If the JDBC Driver class is not found.
     */
    private void fetchAndDisplayRecipeNames(String selectedDish) throws SQLException, ClassNotFoundException {
        Connection connection = DatabaseUtil.connectToDatabase();
        String query = "SELECT dr.Recipe_ID, r.Name, r.Description " +
                "FROM in2033t02Dish_Recipes dr " +
                "JOIN in2033t02Recipe r ON dr.Recipe_ID = r.Recipe_ID " +
                "WHERE dr.Dish_ID = (SELECT Dish_ID FROM in2033t02Dish WHERE Name = ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, selectedDish);
            try (ResultSet rs = stmt.executeQuery()) {
                ObservableList<Recipe> recipes = FXCollections.observableArrayList();
                while (rs.next()) {
                    int recipeID = rs.getInt("Recipe_ID");
                    String name = rs.getString("Name");
                    String description = rs.getString("Description");
                    Recipe recipe = new Recipe(recipeID, name, description);
                    recipes.add(recipe);
                }


                System.out.println("Fetched " + recipes.size() + " recipe(s).");


                recipeTable.setItems(recipes);
            }
        }
    }


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

    /**
     * Displays an alert.
     *
     * @param alertType The type of alert
     * @param title     The title of the alert
     * @param message   The message of the alert
     * @param details   The details of the alert
     */
    private void showAlert(Alert.AlertType alertType, String title, String message, String details) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
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
    private void handleHomeButtonClick(ActionEvent event) {
        navigateToPage("MainPage.fxml", "Home", event);
    }

    /**
     * Handles the action triggered by the back button.
     * This method redirects the user to the "Dishes" page by calling the navigateToPage method.
     *
     * @param event The event that triggered this method call, typically from user interaction with the GUI.
     */
    @FXML
    private void backButton(ActionEvent event) {navigateToPage("Dishes.fxml", "Home", event);}

    /**
     * Takes you to the new dishes page when add new dish button is clicked.
     */
    @FXML
    private void handleNewDishButtonClick(ActionEvent event) {navigateToPage("AddNewDish.fxml", "Home", event);}

    /**
     * Takes you to the new recipe page when add new recipe button is clicked.
     */
    @FXML
    private void handleNewRecipeButtonClick(ActionEvent event) {navigateToPage("MainPage.fxml", "Home", event);}

    /**
     * Takes you to the new menu page when add new menu button is clicked.
     */
    @FXML
    private void handleNewMenuButtonClick(ActionEvent event) {navigateToPage("AddNewMenu.fxml", "Home", event);}

}
