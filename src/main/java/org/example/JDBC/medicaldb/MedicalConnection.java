package org.example.JDBC.medicaldb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * The {@code MedicalConnection} class centralizes the creation of JDBC connections to the {@code medicaldb}
 * database. This utility class will be invoked by {@code MedicalManager} to retrieve medical information (patients,
 * doctors, reports, signals...) from the database. The class encapsulates:
 * <ul>
 *     <li> Defines the JDBC URL pointing to the {@code Medicaldb_try.db} file, which is a path file to the database</li>
 * </ul>
 *
 * @author MariaMM04
 * @author MamenCortes
 */
public class MedicalConnection {

    // Ruta relativa al archivo dentro de tu proyecto
    private static final String URL = "jdbc:sqlite:src/main/java/org/example/DataBases/Medicaldb.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Error loading SQLite driver for medicaldb", e);
        }
    }

    /**
     * Returns an active {@code Connection} instance to the SQLite medical database {@code medicaldb}
     *
     * @return  an active {@link Connection} to the medical database
     * @throws SQLException if a database access error occurs or the URL is invalid
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}

