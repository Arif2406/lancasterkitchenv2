package com.example.demo1;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
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
    private GridPane ordersGrid;
    private List<Order> orders = new ArrayList<>();
    @FXML
    private VBox pendingColumn, inProgressColumn, finishedColumn;

    public void initialize() {
        try {
            populateOrdersFromDatabase();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();  // Log error, you might want to show this in a user alert
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
        String query = "SELECT o.Order_ID, d.Name AS DishName, d.Course AS Course, od.Dish_Quantity AS Quantity, o.Comment AS Comment FROM in2033t02Orders o JOIN in2033t02Order_Dishes od ON o.Order_ID = od.Order_ID JOIN in2033t02Dish d ON od.Dish_ID = d.Dish_ID WHERE o.Status = 0;" ;
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            Map<Integer, Order> orderMap = new HashMap<>();
            while (rs.next()) {
                try {
                    int orderId = rs.getInt("Order_ID");
                    String dishName = rs.getString("DishName");
                    int quantity = rs.getInt("Quantity");
                    Order order = orderMap.computeIfAbsent(orderId, Order::new);
                    order.addDish(dishName, quantity);
                } catch (SQLException e) {
                    System.err.println("Error processing result set: " + e.getMessage());
                }
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
                VBox column = switch (order.getStatus()) {
                    case "Pending" -> pendingColumn;
                    case "In Progress" -> inProgressColumn;
                    case "Finished" -> finishedColumn;
                    default -> throw new IllegalStateException("Unexpected status: " + order.getStatus());
                };
                column.getChildren().add(createOrderBox(order));
            });
        });
    }

    private VBox createOrderBox(Order order) {
        VBox orderBox = new VBox();
        orderBox.setStyle("-fx-border-color: black; -fx-border-width: 1px; -fx-padding: 5;");
        orderBox.getChildren().add(new Label("Order ID: " + order.getId()));
        order.getDishes().forEach((dish, quantity) ->
                orderBox.getChildren().add(new Label(quantity + " x " + dish)));
        Button statusButton = new Button(order.getStatus());
        statusButton.setOnAction(e -> {
            order.advanceStatus();
            refreshOrdersGrid();
        });
        orderBox.getChildren().add(statusButton);
        return orderBox;
    }

    public void viewTodayOrders(ActionEvent actionEvent) {
    }

    public void viewAllOrders(ActionEvent actionEvent) {
    }

    private static class Order {
        private int id;
        private Map<String, Integer> dishes = new HashMap<>();
        private String status = "Pending";

        public Order(int id) {
            this.id = id;
        }

        public void addDish(String dishName, int quantity) {
            dishes.put(dishName, quantity);
        }

        public void advanceStatus() {
            switch (status) {
                case "Pending" -> status = "In Progress";
                case "In Progress" -> status = "Finished";
                case "Finished" -> System.out.println("Order is already finished");
            }
        }

        public String getStatus() {
            return status;
        }

        public int getId() {
            return id;
        }

        public Map<String, Integer> getDishes() {
            return dishes;
        }
    }
}
