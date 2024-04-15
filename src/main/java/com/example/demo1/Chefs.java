package com.example.demo1;

import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Chefs {

    @FXML
    private TableView<Object[]> table;

    @FXML
    private TableColumn<Object[], String> nameColumn;

    @FXML
    private TableColumn<Object[], String> roleColumn;

    @FXML
    private TableColumn<Object[], String> idColumn;

    @FXML
    private TextField nameField;

    @FXML
    private TextField roleField;

    public void initialize() {
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[0].toString()));
        roleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[1].toString()));


        loadChefData();
    }

    private void loadChefData() {
        table.getItems().clear();  // Clear existing data
        try {
            Connection connection = DatabaseUtil.connectToDatabase();
            String query = "SELECT Name, Role FROM in2033t02Chef";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = new Object[2];
                row[0] = rs.getString("Name");
                row[1] = rs.getString("Role");
                table.getItems().add(row);
            }
        } catch (SQLException | ClassNotFoundException ex) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error loading chefs from database.", ex.getMessage());
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
    private void handleNewRecipeButtonClick(ActionEvent event) {navigateToPage("MainPage.fxml", "Home", event);}

    @FXML
    private void handleNewMenuButtonClick(ActionEvent event) {navigateToPage("MainPage.fxml", "Home", event);}

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



    @FXML
    private void handleAddChefButtonClick(ActionEvent event) {
        try {
            // Create a new Stage (window)
            Stage stage = new Stage();
            stage.setTitle("Add New Chef");

            // Form elements
            Label nameLabel = new Label("Name:");
            TextField nameTextField = new TextField();
            Label usernameLabel = new Label("Username:");
            TextField usernameTextField = new TextField();
            Label passwordLabel = new Label("Password:");
            PasswordField passwordField = new PasswordField();
            Label roleLabel = new Label("Role:");
            ComboBox<String> roleComboBox = new ComboBox<>();
            roleComboBox.getItems().addAll("Head Chef", "Sous Chef", "Line Chef");
            roleComboBox.getSelectionModel().selectFirst();  // Selects the first role by default
            Button submitButton = new Button("Submit");

            // Submit button action
            submitButton.setOnAction(e -> {
                String name = nameTextField.getText();
                String username = usernameTextField.getText();
                String password = passwordField.getText();
                String role = roleComboBox.getValue(); // Get selected role from ComboBox
                // Call method to insert data into database
                insertChefData(name, username, password, role);
                stage.close(); // Close the form window after submission
            });

            // Layout the form
            VBox layout = new VBox(10);
            layout.setPadding(new Insets(10));
            layout.getChildren().addAll(nameLabel, nameTextField, usernameLabel, usernameTextField, passwordLabel, passwordField, roleLabel, roleComboBox, submitButton);

            // Display the stage
            Scene scene = new Scene(layout, 300, 300);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertChefData(String name, String username, String password, String role) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseUtil.connectToDatabase();
            String sql = "INSERT INTO in2033t02Chef (Name, Username, Password, Role) VALUES (?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, username);
            pstmt.setString(3, password);
            pstmt.setString(4, role);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "New chef added successfully.", null);
                loadChefData();  // Refresh the chef list
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add new chef.", null);
            }
        } catch (SQLException | ClassNotFoundException ex) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error inserting chef data into the database.", ex.getMessage());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
    @FXML
    private void handleRemoveChefButtonClick(ActionEvent event) {
        Object[] selectedChef = table.getSelectionModel().getSelectedItem();
        if (selectedChef == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "No Chef Selected", "Please select a chef in the table.");
            return;
        }

        String chefName = selectedChef[0].toString(); // Assuming the first column is the name

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseUtil.connectToDatabase();
            String sql = "DELETE FROM in2033t02Chef WHERE Name = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, chefName);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Chef Removed", "The chef has been successfully removed.");
                loadChefData();  // Refresh the chef list
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to Remove Chef", "No chef was removed. Please check your selection.");
            }
        } catch (SQLException | ClassNotFoundException ex) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error removing chef from the database.", ex.getMessage());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}
