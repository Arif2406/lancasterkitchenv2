package com.example.demo1;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Class for interacting with the database.
 */
public class DatabaseUtil {

    /**
     * Connects to the database.
     *
     * @return A Connection object representing the database connection.
     * @throws SQLException If an error occurs while connecting to the database.
     * @throws ClassNotFoundException If the JDBC driver class is not found.
     */
    public static Connection connectToDatabase() throws SQLException, ClassNotFoundException {
        String driverClassName = "com.mysql.cj.jdbc.Driver";
        String url = "jdbc:mysql://smcse-stuproj00.city.ac.uk:3306/in2033t02";
        String username = "in2033t02_a";
        String password = "b3NV3PntAcI";

        try {
            Class.forName(driverClassName);
            Connection connection = DriverManager.getConnection(url, username, password);
            System.out.println("Connecting to database...");
            return connection;
        } catch (ClassNotFoundException e) {
            System.err.println("Error: Driver class not found: " + e.getMessage());
            throw e;
        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
            throw e;
        }
    }


}