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

/**
 * Controller class for the chefs page.
 */
public class Chefs {

    /**
     * TableView for displaying chef data.
     */
    @FXML
    private TableView<Object[]> table;

    /**
     * TableColumn for displaying chef names.
     */
    @FXML
    private TableColumn<Object[], String> nameColumn;

    /**
     * TableColumn for displaying chef roles.
     */
    @FXML
    private TableColumn<Object[], String> roleColumn;

    /**
     * TableColumn for displaying chef IDs.
     */
    @FXML
    private TableColumn<Object[], String> idColumn;

    /**
     * TextField for entering chef name.
     */
    @FXML
    private TextField nameField;

    /**
     * TextField for entering chef role.
     */
    @FXML
    private TextField roleField;

    /**
     * Initialises the controller, populating table with chefs from database.
     */
    public void initialize() {
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[0].toString()));
        roleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[1].toString()));


        loadChefData();
    }

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
     * Sets the username of the currently logged-in user.
     * @param username The username of the logged-in user.
     */
    public void setUsername(String username) {
        this.currentUser = username;
        if (usernameLabel != null) {
            usernameLabel.setText("Logged in as: " + username);
        }
    }

    /**
     * Loads chef data from the database and populates the TableView.
     */
    private void loadChefData() {
        table.getItems().clear();
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

    /**
     * Displays an alert.
     *
     * @param alertType The type of alert
     * @param title     The title of the alert
     * @param message   The message of the alert
     * @param details   The details of the alert
     */
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

    /**
     * Takes you to the chefs page when chefs button is clicked.
     */
    @FXML
    private void handleChefsButtonClick(ActionEvent event) {navigateToPage("Chefs.fxml", "Chefs", event);}

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
        navigateToPage("Home.fxml", "Menus", event);
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
    private void handleSupplierButtonClick(ActionEvent event) { navigateToPage("SupplierStock.fxml", "Supplier", event);}

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


    /**
     * Handles the button click event for adding a new chef.
     * @param event The ActionEvent triggered by the button click.
     */
    @FXML
    private void handleAddChefButtonClick(ActionEvent event) {
        if (!"headchef".equals(currentUser) && !"souschef".equals(currentUser)) {
            showAlert(Alert.AlertType.ERROR, "Permission Denied", "Not enough permissions to add a chef.", null);
            return;
        }




        try {

            Stage stage = new Stage();
            stage.setTitle("Add New Chef");


            Label nameLabel = new Label("Name:");
            TextField nameTextField = new TextField();
            Label usernameLabel = new Label("Username:");
            TextField usernameTextField = new TextField();
            Label passwordLabel = new Label("Password:");
            PasswordField passwordField = new PasswordField();
            Label roleLabel = new Label("Role:");
            ComboBox<String> roleComboBox = new ComboBox<>();
            roleComboBox.getItems().addAll("Head Chef", "Sous Chef", "Line Chef");
            roleComboBox.getSelectionModel().selectFirst();
            Button submitButton = new Button("Submit");

            submitButton.setOnAction(e -> {
                String name = nameTextField.getText();
                String username = usernameTextField.getText();
                String password = passwordField.getText();
                String role = roleComboBox.getValue();

                insertChefData(name, username, password, role);
                stage.close();
            });


            VBox layout = new VBox(10);
            layout.setPadding(new Insets(10));
            layout.getChildren().addAll(nameLabel, nameTextField, usernameLabel, usernameTextField, passwordLabel, passwordField, roleLabel, roleComboBox, submitButton);


            Scene scene = new Scene(layout, 300, 300);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Inserts chef data into the database.
     * @param name      The name of the chef.
     * @param username  The username of the chef.
     * @param password  The password of the chef.
     * @param role      The role of the chef.
     */
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
                loadChefData();
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

    /**
     * Handles the button click event for removing a new chef.
     * @param event The ActionEvent triggered by the button click.
     */
    @FXML
    private void handleRemoveChefButtonClick(ActionEvent event) {
        if (!"headchef".equals(currentUser) && !"souschef".equals(currentUser)) {
            showAlert(Alert.AlertType.ERROR, "Permission Denied", "Not enough permissions to remove a chef.", null);
            return;
        }




    Object[] selectedChef = table.getSelectionModel().getSelectedItem();
        if (selectedChef == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "No Chef Selected", "Please select a chef in the table.");
            return;
        }

        String chefName = selectedChef[0].toString();

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
                loadChefData();
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
