package com.example.demo1;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
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
 * Controller class for the orders page.
 */
public class Orders {

    /**
     * Label for displaying the username of the currently logged in user.
     */
    @FXML
    private Label usernameLabel;

    /**
     * String containing the username of the currently logged in user.
     */
    private String currentUser;

    /**
     * Sets the current user's username and updates the UI to display it.
     *
     * @param username The username of the user who has logged in.
     */
    public void setUsername(String username) {
        this.currentUser = username;
        if (usernameLabel != null) {
            usernameLabel.setText("Logged in as: " + username);
        }
    }

    /**
     * Grid pane for displaying orders.
     */
    @FXML
    private GridPane ordersGrid;

    /**
     * List containing the orders.
     */
    private List<Order> orders = new ArrayList<>();

    /**
     * VBoxes for the table columns.
     */
    @FXML
    private VBox pendingColumn, inProgressColumn, finishedColumn;

    /**
     * Initialises controller and populates the grid with orders data.
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
     * Sets up the columns in the UI for different order statuses.
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
     * Retrieves order data from the database and organises it by status.
     * @throws SQLException If a database access error occurs or this method is called on a closed connection.
     * @throws ClassNotFoundException If the database driver is not found.
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
                int quantity = rs.getInt("Quantity");
                String course = rs.getString("Course");
                int status = rs.getInt("Status");
                Order order = orderMap.computeIfAbsent(orderId, Order::new);
                order.addDish(dishId, dishName, quantity, course, status, orderId);
            }
            orders.addAll(orderMap.values());
        } finally {
            if (connection != null) connection.close();
        }
    }


    /**
     * Refreshes the orders display grid according to their status.
     */
    private void refreshOrdersGrid() {
        Platform.runLater(() -> {
            pendingColumn.getChildren().clear();
            inProgressColumn.getChildren().clear();
            finishedColumn.getChildren().clear();
            orders.forEach(order -> {
                order.getDishesByCourse().values().forEach(dishes -> dishes.forEach(dish -> {
                    VBox column = switch (dish.status) {
                        case 0 -> pendingColumn;
                        case 1 -> inProgressColumn;
                        case 2 -> finishedColumn;
                        default -> throw new IllegalStateException("Unexpected status: " + dish.status);
                    };
                    column.getChildren().add(createOrderBox(order, dish));
                }));
            });
        });
    }

    /**
     * Creates a VBox for the order, displaying its details and a status button to change its status.
     * @param order The order to be displayed.
     * @param dish  The specific dish within the order to be displayed.
     * @return VBox The visual component containing the order details.
     */
    private VBox createOrderBox(Order order, Dish dish) {
        VBox dishBox = new VBox(5);
        dishBox.setStyle("-fx-border-color: black; -fx-border-width: 1px; -fx-padding: 5;");
        Label orderIdLabel = new Label("Order ID: " + order.getId() + " - Dish: " + dish.name);
        orderIdLabel.setStyle("-fx-font-weight: bold; -fx-underline: true;");
        dishBox.getChildren().add(orderIdLabel);
        Label dishLabel = new Label(dish.quantity + " x " + dish.name);
        dishBox.getChildren().add(dishLabel);
        Button statusButton = new Button(getStatusAsString(dish.status));
        statusButton.setOnAction(e -> {
            dish.advanceStatus();
            refreshOrdersGrid();
        });
        dishBox.getChildren().add(statusButton);
        return dishBox;
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
     * Calculates the next status for an order based on its current status.
     * @param currentStatus The current status of the order.
     * @return int The next status.
     */
    private static int getNextStatus(int currentStatus) {
        return (currentStatus + 1) % 3;
    }

    /**
     * Placeholder method to views all orders from the system.
     * @param actionEvent The event triggered by clicking to view all orders.
     */
    public void viewAllOrders(ActionEvent actionEvent) {
    }

    /**
     * Represents a dish in an order, including its name, quantity, and status.
     */
    public static class Dish {

        /**
         * Integer containing the dish ID.
         */
        int dishId;

        /**
         * String containing the dish name.
         */
        String name;

        /**
         * Integer containing the dish quantity.
         */
        int quantity;

        /**
         * Integer containing the dish status.
         */
        int status;

        /**
         * Integer containing the order ID.
         */
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
         * Advances the status of this dish to the next stage in the preparation process and updates the database.
         */
        void advanceStatus() {
            status = (status + 1) % 3;
            updateStatusInDatabase();
        }

        /**
         * Updates the status of this dish in the database to reflect any changes.
         */
        void updateStatusInDatabase() {
            String query = "UPDATE in2033t02Order_Dishes SET Status = ? WHERE Order_ID = ? AND Dish_ID = ?";
            try (Connection connection = DatabaseUtil.connectToDatabase();
                 PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, status);
                stmt.setInt(2, orderId);
                stmt.setInt(3, dishId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("SQL Error: " + e.getMessage());
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }


    }

    /**
     * Represents an order containing multiple dishes, organised by course.
     */
    public static class Order {

        /**
         * Integer containing the order ID.
         */
        private int id;

        /**
         * A map of the dishes by course.
         */
        private Map<String, List<Dish>> dishesByCourse = new HashMap<>();

        /**
         * Constructs a new Order instance.
         * @param id The unique identifier for the order.
         */
        public Order(int id) {
            this.id = id;
        }

        /**
         * Adds a dish to this order under the specified course category.
         * @param dishId    The dish's ID.
         * @param dishName  The name of the dish.
         * @param quantity  The quantity of the dish.
         * @param course    The course category the dish belongs to.
         * @param status    The initial status of the dish.
         * @param orderId   The ID of the order this dish is part of.
         */
        public void addDish(int dishId, String dishName, int quantity, String course, int status, int orderId) {
            dishesByCourse.putIfAbsent(course, new ArrayList<>());
            dishesByCourse.get(course).add(new Dish(dishId, dishName, quantity, status, orderId));
        }

        /**
         * Returns a map of dishes categorized by their course.
         * @return A map with course names as keys and lists of dishes as values.
         */
        public Map<String, List<Dish>> getDishesByCourse() {
            return dishesByCourse;
        }

        /**
         * Returns the unique identifier of the order.
         * @return The order's ID.
         */
        public int getId() {
            return id;
        }


    }

    /**
     * Handles the action of viewing all orders for the current day.
     * @param event The action event triggered by the user interaction.
     */
    @FXML
    public void HandleViewTodayOrders(ActionEvent event) {
        navigateToPage("ViewAllOrdersToday.fxml", "ViewAllOrdersToday",event);

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
        navigateToPage("SupplierStock.fxml", "Stock", event);
    }


    /**
     * Takes you to the stock page when stock button is clicked.
     */
    @FXML
    private void handleStockButtonClick(ActionEvent event) {
        navigateToPage("Supplier.fxml", "Supplier", event);
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
