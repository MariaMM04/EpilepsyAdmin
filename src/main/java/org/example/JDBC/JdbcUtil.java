package org.example.JDBC;

import java.sql.Connection;
import java.sql.SQLException;

import org.example.JDBC.medicaldb.MedicalConnection;
import org.example.JDBC.securitydb.SecurityConnection;

/**
 * This {@code JdbcUtil} class is a utility class that provides access to both database connections.
 * It defines a getter to provide a MedicalConnection, and a getter to provide a SecurityConnection.
 *
 * @author MariaMM04
 */
public class JdbcUtil {

    public static Connection getMedicalConnection() throws SQLException {
        return MedicalConnection.getConnection();
    }

    public static Connection getSecurityConnection() throws SQLException {
        return SecurityConnection.getConnection();
    }
}

