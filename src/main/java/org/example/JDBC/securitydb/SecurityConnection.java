package org.example.JDBC.securitydb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for connecting to the "securitydb" database.
 * Used within SecurityManager.
 */
public class SecurityConnection {

    private static final String URL = "jdbc:sqlite:src/main/java/org/example/DataBases/Securitydb.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Error loading SQLite driver for securitydb", e);
        }
    }

    /**
     * Returns an active connection to the SQLite security database.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}
