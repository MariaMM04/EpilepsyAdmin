package org.example.JDBC.medicaldb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for connecting to the "medicaldb" database.
 * Used within MedicalManager.
 */
public class MedicalConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/medicaldb?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true";
    private static final String USER = "root";
    private static final String PASSWORD = "teleco2025";

    static {
        try {
            // Clean JDBC driver loading (not always necessary, but good practice)
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Error loading MySQL driver for medicaldb", e);
        }
    }

    /**
     * Returns an active connection to the medicaldb database.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

