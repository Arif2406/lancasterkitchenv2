package com.example.demo1;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class Orders {

    @FXML
    private GridPane ordersGrid;

    private List<Order> orders = new ArrayList<>();
    @FXML
    private VBox pendingColumn;
    @FXML
    private VBox inProgressColumn;
    @FXML
    private VBox finishedColumn;

    public void viewTodayOrders() {
        System.out.println("Showing all orders for today...");
    }

    public void viewAllOrders() {
        System.out.println("Showing all orders total...");
    }

    public void initialize() {
        createSampleOrders();
        initializeStatusColumns();
        refreshOrdersGrid();
    }

    private void initializeStatusColumns() {
        pendingColumn = new VBox();
        pendingColumn.setSpacing(10);
        ordersGrid.add(pendingColumn, 0, 0);
        pendingColumn.getChildren().add(new Label("Pending"));

        inProgressColumn = new VBox();
        inProgressColumn.setSpacing(10);
        ordersGrid.add(inProgressColumn, 1, 0);
        inProgressColumn.getChildren().add(new Label("In Progress"));

        finishedColumn = new VBox();
        finishedColumn.setSpacing(10);
        ordersGrid.add(finishedColumn, 2, 0);
        finishedColumn.getChildren().add(new Label("Finished"));
    }

    public void createSampleOrders() {
        addOrder(new String[]{"Pizza"}, new int[]{2});
        addOrder(new String[]{"Burger", "Fries"}, new int[]{1, 3});
        addOrder(new String[]{"Salad", "Pasta"}, new int[]{1, 2});
    }

    private void addOrder(String[] dishes, int[] quantities) {
        Order order = new Order(dishes, quantities);
        orders.add(order);
    }

    private void refreshOrdersGrid() {
        // Clear all columns
        pendingColumn.getChildren().clear();
        inProgressColumn.getChildren().clear();
        finishedColumn.getChildren().clear();

        // Populate columns with orders
        for (Order order : orders) {
            VBox column;
            switch (order.getStatus()) {
                case "Pending":
                    column = pendingColumn;
                    break;
                case "In Progress":
                    column = inProgressColumn;
                    break;
                case "Finished":
                    column = finishedColumn;
                    break;
                default:
                    column = null;
            }
            if (column != null) {
                column.getChildren().add(createOrderBox(order));
            }
        }
    }

    private VBox createOrderBox(Order order) {
        VBox orderBox = new VBox();
        orderBox.setStyle("-fx-border-color: black; -fx-border-width: 1px; -fx-padding: 5;");
        orderBox.getChildren().add(new Label("Order ID: " + order.getId()));
        for (int i = 0; i < order.getDishes().length; i++) {
            orderBox.getChildren().add(new Label(order.getQuantities()[i] + " x " + order.getDishes()[i]));
        }
        Button statusButton = new Button(order.getStatus());
        statusButton.setOnAction(e -> {
            order.advanceStatus();
            refreshOrdersGrid();
        });
        orderBox.getChildren().add(statusButton);
        return orderBox;
    }

    private static class Order {
        private static int orderIdCounter = 1;
        private int id;
        private String[] dishes;
        private int[] quantities;
        private String status = "Pending"; // Default to Pending

        public Order(String[] dishes, int[] quantities) {
            this.id = orderIdCounter++;
            this.dishes = dishes;
            this.quantities = quantities;
        }

        public void advanceStatus() {
            switch (status) {
                case "Pending":
                    status = "In Progress";
                    break;
                case "In Progress":
                    status = "Finished";
                    break;
                case "Finished":
                    // Do nothing, as it can't advance beyond "Finished"
                    break;
                default:
                    // Invalid status
                    break;
            }
        }

        public String getStatus() {
            return status;
        }

        public int getId() {
            return id;
        }

        public String[] getDishes() {
            return dishes;
        }

        public int[] getQuantities() {
            return quantities;
        }
    }
}




   