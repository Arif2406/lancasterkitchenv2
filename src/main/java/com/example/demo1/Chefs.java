package com.example.demo1;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Chefs {


    @FXML
    private TableView<Chef> table;

    @FXML
    private TableColumn<Chef, String> nameColumn;

    @FXML
    private TableColumn<Chef, String> roleColumn;

    @FXML
    private TableColumn<Chef, String> idColumn;

    @FXML
    private TextField nameField;

    @FXML
    private TextField roleField;

    @FXML
    private Button addButton;

    @FXML
    private Button removeButton;

    @FXML
    private Button homeButton;

    private ObservableList<Chef> chefData = FXCollections.observableArrayList();

    public void initialize() {
        // Initialize the table columns
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        roleColumn.setCellValueFactory(cellData -> cellData.getValue().roleProperty());
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());

        // Populate table with initial data
        loadChefData();
    }

    @FXML
    private void handleAddButtonAction() {
        String name = nameField.getText().trim();
        String role = roleField.getText().trim();

        if (!name.isEmpty() && !role.isEmpty()) {
            String id = generateRandomID();
            try {
                insertChef(name, role, id);
                chefData.add(new Chef(name, role, id));
            } catch (SQLException | ClassNotFoundException ex) {
                showAlert(AlertType.ERROR, "Error", "Error adding chef to database.", ex.getMessage());
            }
        } else {
            showAlert(AlertType.ERROR, "Error", "Name and Role cannot be empty.", null);
        }
    }

    @FXML
    private void handleRemoveButtonAction() {
        Chef selectedChef = table.getSelectionModel().getSelectedItem();
        if (selectedChef != null) {
            try {
                deleteChef(selectedChef.getId());
                chefData.remove(selectedChef);
            } catch (SQLException | ClassNotFoundException ex) {
                showAlert(AlertType.ERROR, "Error", "Error removing chef from database.", ex.getMessage());
            }
        } else {
            showAlert(AlertType.ERROR, "Error", "Please select a chef to remove.", null);
        }
    }

    @FXML
    private void handleHomeButtonAction() {
        // Implement logic to return to the main menu
    }

    private void loadChefData() {
        // Fetch data from database and populate chefData list
        try {
            Connection connection = DatabaseUtil.connectToDatabase();
            String query = "SELECT * FROM chefs";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");
                String role = rs.getString("role");
                String id = rs.getString("id");
                chefData.add(new Chef(name, role, id));
            }
        } catch (SQLException | ClassNotFoundException ex) {
            showAlert(AlertType.ERROR, "Error", "Error loading chefs from database.", ex.getMessage());
        }
    }

    private String generateRandomID() {
        return String.format("%04d", (int) (Math.random() * 10000));
    }

    private void insertChef(String name, String role, String id) throws SQLException, ClassNotFoundException {
        try (Connection conn = DatabaseUtil.connectToDatabase()) {
            String sql = "INSERT INTO chefs (name, role, id) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, name);
                stmt.setString(2, role);
                stmt.setString(3, id);
                stmt.executeUpdate();
            }
        }
    }

    private void deleteChef(String id) throws SQLException, ClassNotFoundException {
        try (Connection conn = DatabaseUtil.connectToDatabase()) {
            String sql = "DELETE FROM chefs WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, id);
                stmt.executeUpdate();
            }
        }
    }

    private void showAlert(AlertType alertType, String title, String message, String details) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (details != null) {
            Label label = new Label("Details:");
            TextArea textArea = new TextArea(details);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            VBox vBox = new VBox(label, textArea);
            alert.getDialogPane().setExpandableContent(vBox);
        }
        alert.showAndWait();
    }
}
