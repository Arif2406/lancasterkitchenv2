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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
 * Controller class for the dishes page.
 */
public class Dishes {

    /**
     * TableColumn for displaying the name of the recipe.
     */
    @FXML
    private TableColumn<Recipe, String> nameColumn;

    /**
     * ImageView for displaying the image of the dish.
     */
    @FXML
    private ImageView dishImageView;

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
     * TextArea for displaying the ingredients of the dish.
     */
    @FXML
    private TextArea ingredientsTextArea;

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
     * Initialises the controller class, populates grid with orders from database.
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
     * Populates the dishes list from the database.
     *
     * @throws SQLException if a SQL exception occurs
     * @throws ClassNotFoundException if the database driver class is not found
     */
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

    /**
     * Opens the information of the selected dish.
     *
     * @param selectedDish the name of the selected dish
     * @throws SQLException if a SQL exception occurs
     * @throws ClassNotFoundException if the database driver class is not found
     */
    private void openDishInformation(String selectedDish) throws SQLException, ClassNotFoundException {
        Connection connection = DatabaseUtil.connectToDatabase();
        String query = "SELECT d.Course, d.Status, d.Description, d.Chef_Creator_ID, s.Step_Description, Picture_URL " +
                "FROM in2033t02Dish d " +
                "LEFT JOIN in2033t02Dish_Steps s ON d.Dish_ID = s.Dish_ID " +
                "WHERE d.Name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, selectedDish);
            try (ResultSet rs = stmt.executeQuery()) {
                StringBuilder stepDescriptions = new StringBuilder();
                while (rs.next()) {
                    nameLabel.setText("Name: " + selectedDish);
                    courseLabel.setText("Course: " + rs.getString("Course"));
                    statusLabel.setText("Status: " + rs.getString("Status"));
                    descriptionArea.setText(rs.getString("Description"));
                    chefLabel.setText("Chef ID: " + rs.getString("Chef_Creator_ID"));
                    stepDescriptions.append(rs.getString("Step_Description")).append("\n");

                    String Picture_URL = rs.getString("Picture_URL");
                    if (Picture_URL != null && !Picture_URL.isEmpty()) {
                        Image image = new Image(Picture_URL, true);
                        dishImageView.setImage(image);
                    }
                }
                stepsTextArea.setText(stepDescriptions.toString());

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

                        rdescriptionArea.setText(description);
                        recipeTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                            if (newValue != null) {
                                fetchAndDisplayRecipeDescription(newValue.getRecipeID());
                                fetchAndDisplayIngredients(newValue.getRecipeID());
                            }
                        });


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

                    rstepsTextArea.setText(steps.toString());
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error fetching recipe steps.", ex.getMessage());
        }
    }

    /**
     * Fetches and displays the recipe steps.
     *
     * @param recipeID the ID of the recipe
     */
    private void fetchAndDisplayIngredients(int recipeID) {
        try (Connection connection = DatabaseUtil.connectToDatabase()) {
            String query = """
            SELECT i.Name, ri.Quantity, ri.Unit
            FROM in2033t02Ingredient i
            JOIN in2033t02Recipe_Ingredients ri ON i.Ingredient_ID = ri.Ingredient_ID
            WHERE ri.Recipe_ID = ?
            """;
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, recipeID);
                ResultSet rs = stmt.executeQuery();
                StringBuilder ingredientDetails = new StringBuilder();
                while (rs.next()) {
                    ingredientDetails.append(rs.getString("Name"))
                            .append(" - Quantity: ")
                            .append(rs.getDouble("Quantity"))
                            .append(" ")
                            .append(rs.getString("Unit"))
                            .append("\n");
                }
                ingredientsTextArea.setText(ingredientDetails.toString());
            }
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error fetching ingredients.", ex.getMessage());
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
     * Takes you to the add new dishes page when add new dish button is clicked.
     */
    @FXML
    private void handleAddNewDish(ActionEvent event) {
        navigateToPage("AddNewDish.fxml", "addNewDishButton", event);
    }

    /**
     * Takes you to the view pending dishes page when pending dishes button is clicked.
     */
    @FXML
    private void handleViewPendingDishes(ActionEvent event) {
        navigateToPage("PendingDishes.fxml", "viewPendingDishesButton", event);
    }

    /**
     * Takes you to the view all dishes page when all dishes button is clicked.
     */
    @FXML
    private void handleViewAllDishes(ActionEvent event) {
        navigateToPage("AllDishes.fxml", "viewAllDishesButton", event);
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
    private void handleChefsButtonClick(ActionEvent event) {navigateToPage("Chefs.fxml", "Chefs", event);}

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
        navigateToPage("Home.fxml", "Menus", event);
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
    private void handleSupplierButtonClick(ActionEvent event) { navigateToPage("SupplierStock.fxml", "Supplier", event);}

    /**
     * Takes you to the stock page when stock button is clicked.
     */
    @FXML
    private void handleStockButtonClick(ActionEvent event) {
        navigateToPage("CurrentStock.fxml", "Stock", event);
    }

    /**
     * Takes you to the new dishes page when add new dish button is clicked.
     */
    @FXML
    private void handleNewDishButtonClick(ActionEvent event) {navigateToPage("AddNewDish.fxml", "Home", event);}

    /**
     * Takes you to the new recipe page when add new recipe button is clicked.
     */
    @FXML
    private void handleNewRecipeButtonClick(ActionEvent event) {navigateToPage("AddNewRecipe.fxml", "Home", event);}

    /**
     * Takes you to the new menu page when add new menu button is clicked.
     */
    @FXML
    private void handleNewMenuButtonClick(ActionEvent event) {navigateToPage("AddNewMenu.fxml", "Home", event);}

    /**
     * Takes you to the home page when stock button is clicked.
     */
    @FXML
    private void handleHomeButtonClick(ActionEvent event) {navigateToPage("MainPage.fxml", "Home", event);}

}
