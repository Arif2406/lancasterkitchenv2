package com.example.demo1;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseUtil {


    public static Connection connectToDatabase() throws SQLException, ClassNotFoundException {
        String driverClassName = "com.mysql.cj.jdbc.Driver"; // Replace with the actual driver class name
        String url = "jdbc:mysql://smcse-stuproj00.city.ac.uk:3306/in2033t02";
        String username = "in2033t02_a";
        String password = "b3NV3PntAcI";

        try {
            Class.forName(driverClassName);
            Connection connection = DriverManager.getConnection(url, username, password);
            return connection;
        } catch (ClassNotFoundException e) {
            System.err.println("Error: Driver class not found: " + e.getMessage());
            throw e; // Re-throw the exception for handling in the calling code
        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
            throw e; // Re-throw the exception for handling in the calling code
        }
    }


}