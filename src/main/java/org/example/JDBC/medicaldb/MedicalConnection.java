package org.example.JDBC.medicaldb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for connecting to the "medicaldb" database.
 * Used within MedicalManager.
 */
public class MedicalConnection {

    // Ruta relativa al archivo dentro de tu proyecto
    private static final String URL = "jdbc:sqlite:src/main/java/org/example/DataBases/Medicaldb_try.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Error loading SQLite driver for medicaldb", e);
        }
    }

    /**
     * Returns an active connection to the SQLite medical database.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}

