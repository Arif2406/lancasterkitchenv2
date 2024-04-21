package com.example.demo1;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller class for adding a new recipe to the system.
 */
public class AddNewRecipe {

    /**
     * Text field for entering the name of the recipe.
     */
    @FXML
    private TextField nameField;

    /**
     * Text field for entering the name of the recipe.
     */
    @FXML
    private TextField descriptionField;

    /**
     * VBox containing fields for adding ingredients.
     */
    @FXML
    private VBox recipeFieldsVBox;

    /**
     * VBox containing fields for adding steps.
     */
    @FXML
    private VBox stepFieldsVBox;

    /**
     * List of ComboBoxes for selecting ingredients.
     */
    @FXML
    private List<ComboBox<String>> ingredientComboBoxes = new ArrayList<>();

    /**
     * List of text fields for entering ingredient quantities.
     */
    @FXML
    private List<TextField> quantityFields = new ArrayList<>();

    /**
     * List of text fields for entering ingredient units.
     */
    @FXML
    private List<TextField> unitFields = new ArrayList<>();

    /**
     * List of text fields for entering recipe steps.
     */
    @FXML
    private List<TextField> stepTextFields = new ArrayList<>();

    /**
     * Initialises the controller, adding the ingredient and step comboboxes.
     */
    @FXML
    private void initialize() {
        addIngredientField();
        addStepField();
    }

    /**
     * Adds a new ingredient field to the UI.
     */
    @FXML
    private void addIngredientField() {
        HBox hbox = new HBox(10);
        ComboBox<String> ingredientComboBox = new ComboBox<>();
        TextField quantityField = new TextField();
        TextField unitField = new TextField();

        ingredientComboBox.setPromptText("Select an Ingredient");
        quantityField.setPromptText("Quantity");
        unitField.setPromptText("Unit");

        populateIngredientComboBox(ingredientComboBox);

        hbox.getChildren().addAll(new Label("Ingredient:"), ingredientComboBox, new Label("Quantity:"), quantityField, new Label("Unit:"), unitField);
        recipeFieldsVBox.getChildren().add(hbox);

        ingredientComboBoxes.add(ingredientComboBox);
        quantityFields.add(quantityField);
        unitFields.add(unitField);
    }

    /**
     * Populates the ingredient ComboBox with data from the database.
     *
     * @param comboBox The ComboBox to populate.
     */
    private void populateIngredientComboBox(ComboBox<String> comboBox) {
        try (Connection connection = DatabaseUtil.connectToDatabase();
             PreparedStatement stmt = connection.prepareStatement("SELECT Name FROM in2033t02Ingredient");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                comboBox.getItems().add(rs.getString("Name"));
            }
        } catch (SQLException | ClassNotFoundException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load ingredients.", e.getMessage());
        }
    }

    /**
     * Adds a new step field to the UI.
     */
    @FXML
    private void addStepField() {
        TextField stepTextField = new TextField();
        stepTextField.setPromptText("Describe the step");
        stepFieldsVBox.getChildren().add(stepTextField);
        stepTextFields.add(stepTextField);
    }

    /**
     * Submits the new recipe to the database.
     */
    @FXML
    private void submit() {
        String name = nameField.getText();
        String description = descriptionField.getText();

        if (name.isEmpty() || description.isEmpty() || !validateIngredientSelection() || !validateStepFields()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields and select an ingredient for each step.", null);
            return;
        }

        try {
            Connection connection = DatabaseUtil.connectToDatabase() ;
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO in2033t02Recipe (Name, Description, Chef_Creator_ID) VALUES (?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);

            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setString(3, "1");
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                showAlert(Alert.AlertType.ERROR, "Error", "Creating recipe failed, no rows affected.", null);
                return;
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long recipeId = generatedKeys.getLong(1);
                    updateIngredientsAndSteps(recipeId, connection);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Recipe added successfully!", null);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Creating recipe failed, no ID obtained.", null);
                }
            }
        }  catch (SQLException | ClassNotFoundException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage(), null);
            e.printStackTrace();
        }
    }

    /**
     * Updates the database with the ingredients and steps of the newly added recipe.
     *
     * @param recipeId      The ID of the new recipe.
     * @param connection    The database connection.
     * @throws SQLException If a database access error occurs.
     */
    private void updateIngredientsAndSteps(long recipeId, Connection connection) throws SQLException {
        for (int i = 0; i < ingredientComboBoxes.size(); i++) {
            String ingredientName = ingredientComboBoxes.get(i).getValue();
            String quantity = quantityFields.get(i).getText();
            String unit = unitFields.get(i).getText();
            int ingredientId = getIngredientIdByName(ingredientName, connection);

            if (ingredientId != -1 && !quantity.isEmpty() && !unit.isEmpty()) {
                PreparedStatement stmt = connection.prepareStatement("INSERT INTO in2033t02Recipe_Ingredients (Recipe_ID, Ingredient_ID, Quantity, Unit) VALUES (?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);

                stmt.setLong(1, recipeId);
                stmt.setInt(2, ingredientId);
                stmt.setString(3, quantity);
                stmt.setString(4, unit);
                stmt.executeUpdate();

            }
        }

        for (int i = 0; i < stepTextFields.size(); i++) {
            String step = stepTextFields.get(i).getText();
            if (!step.isEmpty()) {
                PreparedStatement stmt = connection.prepareStatement("INSERT INTO in2033t02Recipe_Steps (Recipe_ID, Step_Number, Step_Description) VALUES (?, ?, ?)");

                stmt.setLong(1, recipeId);
                stmt.setInt(2, i + 1);
                stmt.setString(3, step);
                stmt.executeUpdate();

            }
        }
    }

    /**
     * Retrieves the ID of an ingredient by its name from the database.
     *
     * @param ingredientName The name of the ingredient.
     * @param connection     The database connection.
     * @return The ID of the ingredient, or -1 if not found.
     * @throws SQLException If a database access error occurs.
     */
    private int getIngredientIdByName(String ingredientName, Connection connection) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT Ingredient_ID FROM in2033t02Ingredient WHERE Name = ?");

        stmt.setString(1, ingredientName);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("Ingredient_ID");
            }
            return -1;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Checks that an ingredient is selected for each step.
     *
     * @return True if an ingredient is selected for each step, otherwise false.
     */
    private boolean validateIngredientSelection() {
        return ingredientComboBoxes.stream().noneMatch(combo -> combo.getValue() == null || combo.getValue().isEmpty());
    }

    /**
     * Checks that each step has a description.
     *
     * @return True if each step has a description, otherwise false.
     */
    private boolean validateStepFields() {
        return stepTextFields.stream().noneMatch(text -> text.getText().isEmpty());
    }

    /**
     * Displays an alert.
     *
     * @param type      The type of alert
     * @param title     The title of the alert
     * @param header    The header of the alert
     * @param content   The details of the alert
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
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
    private void handleWasteButtonClick(ActionEvent event) {navigateToPage("Waste.fxml", "Waste", event);}

    /**
     * Takes you to the menus page when menus button is clicked.
     */
    @FXML
    private void handleMenusButtonClick(ActionEvent event) {navigateToPage("Home.fxml", "Menus", event);}

    /**
     * Takes you to the orders/home page when orders button is clicked.
     */
    @FXML
    private void handleOrdersButtonClick(ActionEvent event) {navigateToPage("Orders.fxml", "Orders", event);}

    /**
     * Takes you to the dishes page when dishes button is clicked.
     */
    @FXML
    private void handleDishesButtonClick(ActionEvent event) {navigateToPage("Dishes.fxml", "Dishes", event);}

    /**
     * Takes you to the supplier page when supplier button is clicked.
     */
    @FXML
    private void handleSupplierButtonClick(ActionEvent event) { navigateToPage("SupplierStock.fxml", "Supplier", event);}

    /**
     * Takes you to the stock page when stock button is clicked.
     */
    @FXML
    private void handleStockButtonClick(ActionEvent event) {navigateToPage("CurrentStock.fxml", "Stock", event);}

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
