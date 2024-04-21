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

/**
 * Controller for the main page of the application that handles navigation and orders.
 */
public class MainPageController {

    @FXML
    private GridPane ordersGrid;
    private List<Order> orders = new ArrayList<>();
    @FXML
    private VBox pendingColumn, inProgressColumn, finishedColumn;

    /**
     * Initialises the controller class, populates grid with orders from database.
     */
    public void initialize() {
        try {
            populateOrdersFromDatabase();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        initializeStatusColumns();
        refreshOrdersGrid();
    }

    /**
     * Initialises the columns for the pending, in-progress, and finished orders within the orders UI.
     */
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

    /**
     * Retrieves and populates the order data from the database into the application.
     * @throws SQLException If a database access error occurs.
     * @throws ClassNotFoundException If the JDBC driver class is not found.
     */
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

    /**
     * Refreshes the orders displayed on the GridPane based on their current status.
     */
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

    /**
     * Creates a VBox containing order details and includes a button to update the status of the dishes in the course.
     * @param order The order containing the course.
     * @param course The name of the course.
     * @param dishes A list of dishes within that course.
     * @return A VBox with the course and dish details.
     */
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

    /**
     * Converts the status code of an order to a string.
     * @param status The status code.
     * @return The string representation of the status.
     */
    private static String getStatusAsString(int status) {
        return switch (status) {
            case 0 -> "Pending";
            case 1 -> "In Progress";
            case 2 -> "Finished";
            default -> "Unknown";
        };
    }

    /**
     * Determines the next status code for an order based on its current status.
     * @param currentStatus The current status code of the order.
     * @return The next status code.
     */
    private static int getNextStatus(int currentStatus) {
        return (currentStatus + 1) % 3;
    }


    /**
     * Placeholder method to views all orders from the system.
     *
     * @param actionEvent The event that triggered this method.
     */
    public void viewAllOrders(ActionEvent actionEvent) {
    }

    /**
     * Represents an order with associated dishes categorised by course.
     */
    private static class Order {
        private int id;
        private Map<String, List<Dish>> dishesByCourse = new HashMap<>();

        /**
         * Constructs an Order object with a given ID.
         *
         * @param id The unique identifier for the order.
         */
        public Order(int id) {
            this.id = id;
        }

        /**
         * Adds a dish to the order under the specified course.
         *
         * @param dish   The dish to be added.
         * @param course The course under which the dish is categorized.
         */
        public void addDish(Dish dish, String course) {
            dishesByCourse.computeIfAbsent(course, k -> new ArrayList<>()).add(dish);
        }

        /**
         * Retrieves the mapping of courses to dishes for this order.
         *
         * @return A map where each key is a course name and the value is a list of dishes.
         */
        public Map<String, List<Dish>> getDishesByCourse() {
            return dishesByCourse;
        }

        /**
         * Retrieves the order ID.
         *
         * @return The ID of the order.
         */
        public int getId() {
            return id;
        }
    }

    /**
     * Represents a dish in an order, including its name, quantity, and status.
     */
    private static class Dish {
        int dishId;
        String name;
        int quantity;
        int status;
        int orderId;

        /**
         * Constructs a Dish instance with the specified details.
         *
         * @param dishId   The unique identifier for the dish.
         * @param name     The name of the dish.
         * @param quantity The quantity ordered.
         * @param status   The current status of the dish (e.g., pending, in progress, finished).
         * @param orderId  The ID of the order this dish belongs to.
         */
        Dish(int dishId, String name, int quantity, int status, int orderId) {
            this.dishId = dishId;
            this.name = name;
            this.quantity = quantity;
            this.status = status;
            this.orderId = orderId;
        }

        /**
         * Advances the status of the dish and updates the database record.
         */
        void advanceStatus() {
            status = (status + 1) % 3;
            updateStatusInDatabase();
        }

        /**
         * Updates the status of the dish in the database.
         */
        void updateStatusInDatabase() {
            String query = "UPDATE in2033t02Order_Dishes SET Status = ? WHERE Order_ID = ? AND Dish_ID = ?";
            try (Connection connection = DatabaseUtil.connectToDatabase();
                 PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, status);
                stmt.setInt(2, orderId);
                stmt.setInt(3, dishId);
                int affectedRows = stmt.executeUpdate();

                if (status == 2 && affectedRows > 0) {
                    updateIngredientStockLevels(connection, quantity);
                }
            } catch (SQLException e) {
                System.err.println("SQL Error: " + e.getMessage());
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Updates the stock levels for ingredients based on the dish quantity.
         *
         * @param connection    A database connection.
         * @param quantity      The quantity of the dish prepared.
         * @throws SQLException If an SQL error occurs during the update.
         */
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

        /**
         * Updates the stock for a specific recipe.
         *
         * @param connection    A database connection.
         * @param recipeId      The ID of the recipe.
         * @param dishQuantity  The quantity of the dish prepared.
         * @throws SQLException If an SQL error occurs during the update.
         */
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

        /**
         * Decrements the stock level for an ingredient.
         *
         * @param connection      A database connection.
         * @param ingredientId    The ID of the ingredient to decrement.
         * @param quantityPerRecipe The quantity of the ingredient used per recipe.
         * @param dishQuantity    The quantity of the dish prepared.
         * @throws SQLException If an SQL error occurs during the update.
         */
        void decrementIngredientStock(Connection connection, int ingredientId, double quantityPerRecipe, int dishQuantity) throws SQLException {
            double totalQuantity = quantityPerRecipe * dishQuantity;
            String updateStockQuery = "UPDATE in2033t02Ingredient SET Stock_Level = Stock_Level - ? WHERE Ingredient_ID = ?";
            try (PreparedStatement updateStockStmt = connection.prepareStatement(updateStockQuery)) {
                updateStockStmt.setDouble(1, totalQuantity);
                updateStockStmt.setInt(2, ingredientId);
                updateStockStmt.executeUpdate();


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

        /**
         * Retrieves a list of dishes that are affected by the use of a specific ingredient.
         *
         * @param connection   The database connection to be used for the query.
         * @param ingredientId The unique identifier of the ingredient that has been depleted.
         * @return A list of strings, where each string is the name of an affected dish.
         * @throws SQLException If there is an error performing the database query.
         */
        List<String> getAffectedDishes(Connection connection, int ingredientId) throws SQLException {
            List<String> dishes = new ArrayList<>();

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


        /**
         * Displays a warning popup listing the dishes that cannot be served due to insufficient ingredients.
         *
         * @param dishes A list of dish names that have become unavailable.
         */
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
        private Label usernameLabel;

        private String currentUser;

    /**
     * Sets the username of the current user and updates the username label on the UI.
     *
     * @param username The username of the current user.
     */
        public void setUsername(String username) {
            this.currentUser = username;
            if (usernameLabel != null) {
                usernameLabel.setText("Logged in as: " + username);
            }
        }

    /**
     * Takes you to the home page when stock button is clicked.
     */
        @FXML
        private void handleHomeButtonClick(ActionEvent event) {
            navigateToPage("MainPage.fxml", "Home", event);
        }

    /**
     * Takes you to the chefs page when chefs button is clicked.
     */
    @FXML
    private void handleChefsButtonClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Chefs.fxml"));
            Scene scene = new Scene(loader.load());
            Chefs chefsController = loader.getController();
            chefsController.setUsername(currentUser);

            Stage stage = new Stage();
            stage.setTitle("Chefs");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();


            Stage mainStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            mainStage.close();
        } catch (IOException e) {
            System.err.println("Failed to load the FXML file: Chefs.fxml");
            e.printStackTrace();
        }
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
            if (!"headchef".equals(currentUser) && !"souschef".equals(currentUser)){
                Alert alert = new Alert (Alert.AlertType.ERROR);
                alert.setTitle("Permission Denied");
                alert.setHeaderText(null);
                alert.setContentText("Not enough permissions to access this page.");
                alert.showAndWait();
            } else{
                navigateToPage("Menus.fxml", "Menu", event);
            }
            
            

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
     * Takes you to the new dishes page when add new dish button is clicked.
     */
        @FXML
        private void handleNewDishButtonClick(ActionEvent event) {
            navigateToPage("AddNewDish.fxml", "Home", event);
        }

    /**
     * Takes you to the new recipe page when add new recipe button is clicked.
     */
        @FXML
        private void handleNewRecipeButtonClick(ActionEvent event) {
            navigateToPage("AddNewRecipe.fxml", "AddNewRecipe", event);
        }

    /**
     * Takes you to the new menu page when add new menu button is clicked.
     */
        @FXML
        private void handleNewMenuButtonClick(ActionEvent event) {
            navigateToPage("AddNewMenu.fxml", "Home", event);
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

}