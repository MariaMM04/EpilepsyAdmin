package org.example.JDBC.securitydb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for connecting to the "securitydb" database.
 * Used within SecurityManager.
 */
public class SecurityConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/securitydb?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true";
    private static final String USER = "root";
    private static final String PASSWORD = "teleco2025";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Error loading MySQL driver for securitydb", e);
        }
    }

    /**
     * Returns an active connection to the securitydb database.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
