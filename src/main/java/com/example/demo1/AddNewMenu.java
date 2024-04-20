package com.example.demo1;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller class for adding a new menu to the system.
 */
public class AddNewMenu {

    /**
     * Date picker for selecting the start date of the menu.
     */
    @FXML
    private DatePicker startDatePicker;

    /**
     * VBox for the first course dishes.
     */
    @FXML
    private VBox firstCourseVBox;

    /**
     * VBox for the second course dishes.
     */
    @FXML
    private VBox secondCourseVBox;

    /**
     * VBox for the third course dishes.
     */
    @FXML
    private VBox thirdCourseVBox;

    /**
     * List containing the ComboBoxes for the first course dishes.
     */
    private List<ComboBox<String>> firstCourseDishes = new ArrayList<>();

    /**
     * List containing the ComboBoxes for the second course dishes.
     */
    private List<ComboBox<String>> secondCourseDishes = new ArrayList<>();

    /**
     * List containing the ComboBoxes for the third course dishes.
     */
    private List<ComboBox<String>> thirdCourseDishes = new ArrayList<>();

    /**
     * Initialises the controller.
     */
    @FXML
    public void initialize() {
        addDishDropdown(firstCourseVBox, firstCourseDishes, "First");
        addDishDropdown(secondCourseVBox, secondCourseDishes, "Second");
        addDishDropdown(thirdCourseVBox, thirdCourseDishes, "Third");
    }

    /**
     * Adds a dish dropdown menu to the VBox.
     *
     * @param vBox     The VBox add the dish dropdown menu to.
     * @param dishList The list where the ComboBox for the dish will be added.
     * @param course   The course of the dish.
     */
    private void addDishDropdown(VBox vBox, List<ComboBox<String>> dishList, String course) {
        ComboBox<String> comboBox = new ComboBox<>();
        populateDishComboBox(comboBox, course);
        vBox.getChildren().add(comboBox);
        dishList.add(comboBox);
    }

    /**
     * Populates the ComboBox with dishes for the specified course.
     *
     * @param comboBox The ComboBox to populate.
     * @param course   The course of the dishes.
     */
    private void populateDishComboBox(ComboBox<String> comboBox, String course) {
        try (Connection connection = DatabaseUtil.connectToDatabase();
             PreparedStatement stmt = connection.prepareStatement("SELECT Name FROM in2033t02Dish WHERE Course = ?")) {
            stmt.setString(1, course);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                comboBox.getItems().add(rs.getString("Name"));
            }
        } catch (SQLException | ClassNotFoundException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load dishes for " + course + " course: " + e.getMessage());
        }
    }

    /**
     * Adds a dish dropdown menu for the first course.
     */
    @FXML
    private void addFirstCourseDish() {
        addDishDropdown(firstCourseVBox, firstCourseDishes, "First");
    }

    /**
     * Adds a dish dropdown menu for the second course.
     */
    @FXML
    private void addSecondCourseDish() {
        addDishDropdown(secondCourseVBox, secondCourseDishes, "Second");
    }

    /**
     * Adds a dish dropdown menu for the third course.
     */
    @FXML
    private void addThirdCourseDish() {
        addDishDropdown(thirdCourseVBox, thirdCourseDishes, "Third");
    }

    /**
     * Submits the menu to the database.
     *
     * @param event The event triggered by the submit button.
     */
    @FXML
    private void submitMenu(ActionEvent event) {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = startDate.plusDays(7);
        try (Connection connection = DatabaseUtil.connectToDatabase()) {
            PreparedStatement menuStmt = connection.prepareStatement("INSERT INTO in2033t02Menu (Start_date, End_date, Status) VALUES (?, ?, 'Pending')", PreparedStatement.RETURN_GENERATED_KEYS);
            menuStmt.setDate(1, java.sql.Date.valueOf(startDate));
            menuStmt.setDate(2, java.sql.Date.valueOf(endDate));
            menuStmt.executeUpdate();
            ResultSet generatedKeys = menuStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                long menuId = generatedKeys.getLong(1);
                insertMenuDishes(firstCourseDishes, menuId, connection);
                insertMenuDishes(secondCourseDishes, menuId, connection);
                insertMenuDishes(thirdCourseDishes, menuId, connection);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Menu added successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create menu.");
            }
        } catch (SQLException | ClassNotFoundException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    /**
     * Inserts the selected dishes for the menu into the database.
     *
     * @param dishes        The list of dishes selected for the menu.
     * @param menuId        The ID of the menu in the database.
     * @param connection    The database connection.
     * @throws SQLException If a SQL exception occurs.
     */
    private void insertMenuDishes(List<ComboBox<String>> dishes, long menuId, Connection connection) throws SQLException {
        for (ComboBox<String> dish : dishes) {
            String dishName = dish.getValue();
            PreparedStatement stmt = connection.prepareStatement("SELECT Dish_ID FROM in2033t02Dish WHERE Name = ?");
            stmt.setString(1, dishName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int dishId = rs.getInt("Dish_ID");
                PreparedStatement insertStmt = connection.prepareStatement("INSERT INTO in2033t02Menu_Dishes (Menu_ID, Dish_ID) VALUES (?, ?)");
                insertStmt.setLong(1, menuId);
                insertStmt.setInt(2, dishId);
                insertStmt.executeUpdate();
            }
        }
    }

    /**
     * Displays an alert.
     *
     * @param alertType The type of alert
     * @param title     The title of the alert
     * @param content   The details of the alert
     */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
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
     * Navigates to the relevant FXML page.
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
