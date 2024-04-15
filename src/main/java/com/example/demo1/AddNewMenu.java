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

public class AddNewMenu {

    @FXML
    private DatePicker startDatePicker;
    @FXML
    private VBox firstCourseVBox;
    @FXML
    private VBox secondCourseVBox;
    @FXML
    private VBox thirdCourseVBox;

    private List<ComboBox<String>> firstCourseDishes = new ArrayList<>();
    private List<ComboBox<String>> secondCourseDishes = new ArrayList<>();
    private List<ComboBox<String>> thirdCourseDishes = new ArrayList<>();

    @FXML
    public void initialize() {
        addDishDropdown(firstCourseVBox, firstCourseDishes, "First");
        addDishDropdown(secondCourseVBox, secondCourseDishes, "Second");
        addDishDropdown(thirdCourseVBox, thirdCourseDishes, "Third");
    }

    private void addDishDropdown(VBox vBox, List<ComboBox<String>> dishList, String course) {
        ComboBox<String> comboBox = new ComboBox<>();
        populateDishComboBox(comboBox, course);
        vBox.getChildren().add(comboBox);
        dishList.add(comboBox);
    }

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

    @FXML
    private void addFirstCourseDish() {
        addDishDropdown(firstCourseVBox, firstCourseDishes, "First");
    }

    @FXML
    private void addSecondCourseDish() {
        addDishDropdown(secondCourseVBox, secondCourseDishes, "Second");
    }

    @FXML
    private void addThirdCourseDish() {
        addDishDropdown(thirdCourseVBox, thirdCourseDishes, "Third");
    }

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

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }


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
    private void handleSupplierButtonClick(ActionEvent event) {
        navigateToPage("SupplierStock.fxml", "Supplier", event);
    }

    @FXML
    private void handleStockButtonClick(ActionEvent event) {
        navigateToPage("CurrentStock.fxml", "Stock", event);
    }

    @FXML
    private void handleHomeButtonClick(ActionEvent event) {navigateToPage("MainPage.fxml", "Home", event);}

    @FXML
    private void handleNewDishButtonClick(ActionEvent event) {navigateToPage("AddNewDish.fxml", "Home", event);}

    @FXML
    private void handleNewRecipeButtonClick(ActionEvent event) {navigateToPage("AddNewRecipe.fxml", "AddNewRecipe", event);}


    @FXML
    private void handleNewMenuButtonClick(ActionEvent event) {navigateToPage("AddNewMenu.fxml", "Home", event);}


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
