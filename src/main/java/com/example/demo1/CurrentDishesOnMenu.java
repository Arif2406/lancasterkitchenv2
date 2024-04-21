package com.example.demo1;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.swing.*;

/**
 * Controller class for the current dishes.
 */
public class CurrentDishesOnMenu {


    /**
     * TableColumn for displaying the name of the recipe.
     */
    @FXML
    private TableColumn<Recipe, String> nameColumn;

    /**
     * ListView for displaying the list of dishes.
     */
    @FXML
    private ListView<String> dishList;

    /**
     * Label for displaying the name of the dish.
     */
    @FXML
    private Label nameLabel;

    /**
     * Label for displaying the course of the dish.
     */
    @FXML
    private Label courseLabel;

    /**
     * Label for displaying the status of the dish.
     */
    @FXML
    private Label statusLabel;

    /**
     * Label for displaying the id of the creator of the dish.
     */
    @FXML
    private Label chefLabel;

    /**
     * TextArea for displaying the description of the dish.
     */
    @FXML
    private TextArea descriptionArea;

    /**
     * TextArea for displaying the steps of the dish.
     */
    @FXML
    private TextArea stepsTextArea;

    /**
     * Label for displaying the name of the recipe.
     */
    @FXML
    private Label rnameLabel;

    /**
     * Label for displaying the description of the recipe.
     */
    @FXML
    private TextArea rdescriptionArea;

    /**
     * Label for displaying the steps of the recipe.
     */
    @FXML
    private TextArea rstepsTextArea;

    /**
     * TableView for displaying recipes.
     */
    @FXML
    private TableView<Recipe> recipeTable;

    /**
     * Initialises the controller, populating table with dishes from database.
     */
    public void initialize() {

        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));

        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));


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
        recipeTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {

                fetchAndDisplayRecipeDescription(newValue.getRecipeID());
            }
        });
    }

    /**
     * Populates the dishes list from the database.
     *
     * @throws SQLException if a SQL exception occurs
     * @throws ClassNotFoundException if the database driver class is not found
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
     * Opens the information of the selected dish.
     *
     * @param selectedDish the name of the selected dish
     * @throws SQLException if a SQL exception occurs
     * @throws ClassNotFoundException if the database driver class is not found
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
     * Fetches and displays the recipe description.
     *
     * @param recipeID the ID of the recipe
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
            showAlert(AlertType.ERROR, "Error", "Error fetching recipe details.", ex.getMessage());
        }
    }

    /**
     * Fetches and displays the recipe steps.
     *
     * @param recipeID the ID of the recipe
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
            showAlert(AlertType.ERROR, "Error", "Error fetching recipe steps.", ex.getMessage());
        }
    }

    /**
     * Fetches and displays the recipe names for the selected dish.
     *
     * @param selectedDish the name of the selected dish
     * @throws SQLException if a SQL exception occurs
     * @throws ClassNotFoundException if the database driver class is not found
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
     * Takes you back to the main page when back button is clicked.
     */
    @FXML
    private void handleBackButtonClick(ActionEvent event) {navigateToPage("MainPage.fxml", "Home", event);}

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

