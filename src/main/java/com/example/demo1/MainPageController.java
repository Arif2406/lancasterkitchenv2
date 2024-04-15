package com.example.demo1;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainPageController {

    @FXML
    private GridPane ordersGrid;
    private List<Order> orders = new ArrayList<>();
    @FXML
    private VBox pendingColumn, inProgressColumn, finishedColumn;

    public void initialize() {
        try {
            populateOrdersFromDatabase();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        initializeStatusColumns();
        refreshOrdersGrid();
    }

    private void initializeStatusColumns() {
        pendingColumn = new VBox(10);
        inProgressColumn = new VBox(10);
        finishedColumn = new VBox(10);
        ordersGrid.add(pendingColumn, 0, 0);
        ordersGrid.add(inProgressColumn, 1, 0);
        ordersGrid.add(finishedColumn, 2, 0);
        pendingColumn.getChildren().add(new Label("Pending"));
        inProgressColumn.getChildren().add(new Label("In Progress"));
        finishedColumn.getChildren().add(new Label("Finished"));
    }

    private void populateOrdersFromDatabase() throws SQLException, ClassNotFoundException {
        Connection connection = DatabaseUtil.connectToDatabase();
        String query = "SELECT o.Order_ID, d.Dish_ID, d.Name AS DishName, d.Course AS Course, od.Dish_Quantity AS Quantity, od.Status AS Status FROM in2033t02Orders o JOIN in2033t02Order_Dishes od ON o.Order_ID = od.Order_ID JOIN in2033t02Dish d ON od.Dish_ID = d.Dish_ID;";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            Map<Integer, Order> orderMap = new HashMap<>();
            while (rs.next()) {
                int orderId = rs.getInt("Order_ID");
                int dishId = rs.getInt("Dish_ID");
                String dishName = rs.getString("DishName");
                String course = rs.getString("Course");
                int quantity = rs.getInt("Quantity");
                int status = rs.getInt("Status");
                Order order = orderMap.computeIfAbsent(orderId, Order::new);
                order.addDish(new Dish(dishId, dishName, quantity, status, orderId), course);
            }
            orders.addAll(orderMap.values());
        } finally {
            if (connection != null) connection.close();
        }
    }


    private void refreshOrdersGrid() {
        Platform.runLater(() -> {
            pendingColumn.getChildren().clear();
            inProgressColumn.getChildren().clear();
            finishedColumn.getChildren().clear();
            orders.forEach(order -> {
                order.getDishesByCourse().forEach((course, dishes) -> {
                    VBox column = switch (dishes.get(0).status) {
                        case 0 -> pendingColumn;
                        case 1 -> inProgressColumn;
                        case 2 -> finishedColumn;
                        default -> throw new IllegalStateException("Unexpected status: " + dishes.get(0).status);
                    };
                    column.getChildren().add(createCourseBox(order, course, dishes));
                });
            });
        });
    }

    private VBox createCourseBox(Order order, String course, List<Dish> dishes) {
        VBox courseBox = new VBox(5);
        courseBox.setStyle("-fx-border-color: black; -fx-border-width: 1px; -fx-padding: 5;");
        Label courseLabel = new Label("Order ID: " + order.getId() + " - Course: " + course);
        courseLabel.setStyle("-fx-font-weight: bold; -fx-underline: true;");
        courseBox.getChildren().add(courseLabel);
        dishes.forEach(dish -> {
            Label dishLabel = new Label(dish.quantity + " x " + dish.name);
            courseBox.getChildren().add(dishLabel);
        });
        Button statusButton = new Button(getStatusAsString(dishes.get(0).status));
        statusButton.setOnAction(e -> {
            dishes.forEach(Dish::advanceStatus);
            refreshOrdersGrid();
        });
        courseBox.getChildren().add(statusButton);
        return courseBox;
    }

    private static String getStatusAsString(int status) {
        return switch (status) {
            case 0 -> "Pending";
            case 1 -> "In Progress";
            case 2 -> "Finished";
            default -> "Unknown";
        };
    }

    private static int getNextStatus(int currentStatus) {
        return (currentStatus + 1) % 3;  // Cycles through 0, 1, 2
    }





    public void viewAllOrders(ActionEvent actionEvent) {
    }

    private static class Order {
        private int id;
        private Map<String, List<Dish>> dishesByCourse = new HashMap<>();

        public Order(int id) {
            this.id = id;
        }

        public void addDish(Dish dish, String course) {
            dishesByCourse.computeIfAbsent(course, k -> new ArrayList<>()).add(dish);
        }

        public Map<String, List<Dish>> getDishesByCourse() {
            return dishesByCourse;
        }

        public int getId() {
            return id;
        }
    }

    private static class Dish {
        int dishId;
        String name;
        int quantity;
        int status;
        int orderId;

        Dish(int dishId, String name, int quantity, int status, int orderId) {
            this.dishId = dishId;
            this.name = name;
            this.quantity = quantity;
            this.status = status;
            this.orderId = orderId;
        }

        void advanceStatus() {
            status = (status + 1) % 3; // Cycles through 0, 1, 2
            updateStatusInDatabase();
        }

        void updateStatusInDatabase() {
            String query = "UPDATE in2033t02Order_Dishes SET Status = ? WHERE Order_ID = ? AND Dish_ID = ?";
            try (Connection connection = DatabaseUtil.connectToDatabase();
                 PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, status);
                stmt.setInt(2, orderId);
                stmt.setInt(3, dishId);
                int affectedRows = stmt.executeUpdate();

                if (status == 2 && affectedRows > 0) { // Check if the status is 'Finished' and update was successful
                    updateIngredientStockLevels(connection, quantity); // Pass the quantity of the dish
                }
            } catch (SQLException e) {
                System.err.println("SQL Error: " + e.getMessage());
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        void updateIngredientStockLevels(Connection connection, int quantity) throws SQLException {
            String recipeQuery = "SELECT Recipe_ID FROM in2033t02Dish_Recipes WHERE Dish_ID = ?";
            try (PreparedStatement recipeStmt = connection.prepareStatement(recipeQuery)) {
                recipeStmt.setInt(1, dishId);
                ResultSet recipeRs = recipeStmt.executeQuery();
                while (recipeRs.next()) {
                    int recipeId = recipeRs.getInt("Recipe_ID");
                    updateStockForRecipe(connection, recipeId, quantity);
                }
            }
        }

        void updateStockForRecipe(Connection connection, int recipeId, int dishQuantity) throws SQLException {
            String ingredientQuery = "SELECT Ingredient_ID, Quantity FROM in2033t02Recipe_Ingredients WHERE Recipe_ID = ?";
            try (PreparedStatement ingredientStmt = connection.prepareStatement(ingredientQuery)) {
                ingredientStmt.setInt(1, recipeId);
                ResultSet ingredientRs = ingredientStmt.executeQuery();
                while (ingredientRs.next()) {
                    int ingredientId = ingredientRs.getInt("Ingredient_ID");
                    double requiredQuantity = ingredientRs.getDouble("Quantity");
                    decrementIngredientStock(connection, ingredientId, requiredQuantity, dishQuantity);
                }
            }
        }

        void decrementIngredientStock(Connection connection, int ingredientId, double quantityPerRecipe, int dishQuantity) throws SQLException {
            double totalQuantity = quantityPerRecipe * dishQuantity;
            String updateStockQuery = "UPDATE in2033t02Ingredient SET Stock_Level = Stock_Level - ? WHERE Ingredient_ID = ?";
            try (PreparedStatement updateStockStmt = connection.prepareStatement(updateStockQuery)) {
                updateStockStmt.setDouble(1, totalQuantity);
                updateStockStmt.setInt(2, ingredientId);
                updateStockStmt.executeUpdate();

                // Check the new stock level
                String checkStockQuery = "SELECT Stock_Level FROM in2033t02Ingredient WHERE Ingredient_ID = ?";
                try (PreparedStatement checkStockStmt = connection.prepareStatement(checkStockQuery)) {
                    checkStockStmt.setInt(1, ingredientId);
                    ResultSet stockRs = checkStockStmt.executeQuery();
                    if (stockRs.next() && stockRs.getDouble("Stock_Level") <= 0) {
                        List<String> affectedDishes = getAffectedDishes(connection, ingredientId);
                        Platform.runLater(() -> showWarningPopup(affectedDishes));
                    }
                }
            }
        }

        List<String> getAffectedDishes(Connection connection, int ingredientId) throws SQLException {
            List<String> dishes = new ArrayList<>();
            // Adjusted query to properly join tables based on your schema
            String query = "SELECT DISTINCT d.Name FROM in2033t02Dish d " +
                    "JOIN in2033t02Dish_Recipes dr ON d.Dish_ID = dr.Dish_ID " +
                    "JOIN in2033t02Recipe_Ingredients ri ON dr.Recipe_ID = ri.Recipe_ID " +
                    "WHERE ri.Ingredient_ID = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, ingredientId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    dishes.add(rs.getString("Name"));
                }
            }
            return dishes;
        }



        void showWarningPopup(List<String> dishes) {
            if (!dishes.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Ingredient Depleted");
                alert.setHeaderText("Some dishes are now unavailable");
                alert.setContentText("The following dishes cannot be served due to insufficient ingredients:\n" + String.join(", ", dishes));
                alert.showAndWait();
            }
        }

    }

        @FXML
        private Label usernameLabel; // Label to display the username

        private String currentUser; // Variable to store the current user's username

        // Method to set the current user's username
        public void setUsername(String username) {
            this.currentUser = username;
            if (usernameLabel != null) {
                usernameLabel.setText("Logged in as: " + username);
            }
        }

        @FXML
        private void handleHomeButtonClick(ActionEvent event) {
            navigateToPage("MainPage.fxml", "Home", event);
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
            if (!"headchef".equals(currentUser)&& !"souschef".equals(currentUser)){
                Alert alert = new Alert (AlertType.ERROR);
                alert.setTitle("Permission Denied");
                alert.setHeaderText(null);
                alert.setContentText("Not enough permissions to access this page.");
                alert.showAndWait();
            } else{
                navigateToPage("Menus.fxml", "Menu", event);
            }
            
            

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
        private void handleNewDishButtonClick(ActionEvent event) {
            navigateToPage("AddNewDish.fxml", "Home", event);
        }

        @FXML
        private void handleNewRecipeButtonClick(ActionEvent event) {
            navigateToPage("AddNewRecipe.fxml", "AddNewRecipe", event);
        }

        @FXML
        private void handleNewMenuButtonClick(ActionEvent event) {
            navigateToPage("AddNewMenu.fxml", "Home", event);
        }


    private void navigateToPage(String fxmlFile, String title, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);
            // Maximize instead of full screen
            stage.setMaximized(true);

            stage.show();

            // Close the current (main) stage after opening the new one
            Stage mainStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            mainStage.close();

            // Optional: Smooth transition for showing the stage
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), scene.getRoot());
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

        } catch (IOException e) {
            // Better error handling
            System.err.println("Failed to load the FXML file: " + fxmlFile);
            e.printStackTrace();
        }
    }

}
