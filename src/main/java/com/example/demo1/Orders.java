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

public class Orders {
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
    public static class Dish {
        int dishId;  // Dish ID to reference in the database
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
                stmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("SQL Error: " + e.getMessage());
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }


    }


    public static class Order {
        private int id;
        private Map<String, List<Dish>> dishesByCourse = new HashMap<>();

        public Order(int id) {
            this.id = id;
        }

        public void addDish(int dishId, String dishName, int quantity, String course, int status, int orderId) {
            dishesByCourse.putIfAbsent(course, new ArrayList<>());
            dishesByCourse.get(course).add(new Dish(dishId, dishName, quantity, status, orderId));
        }


        public Map<String, List<Dish>> getDishesByCourse() {
            return dishesByCourse;
        }

        public int getId() {
            return id;
        }


    }

    @FXML
    public void HandleviewTodayOrders(ActionEvent event) {
        navigateToPage("ViewAllOrdersToday.fxml", "ViewAllOrdersToday",event);

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
        navigateToPage("SupplierStock.fxml", "Stock", event);
    }

    @FXML
    private void handleStockButtonClick(ActionEvent event) {
        navigateToPage("Supplier.fxml", "Supplier", event);
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
