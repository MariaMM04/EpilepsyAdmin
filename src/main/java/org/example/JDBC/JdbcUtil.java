package org.example.JDBC;

import java.sql.Connection;
import java.sql.SQLException;

import org.example.JDBC.medicaldb.MedicalConnection;
import org.example.JDBC.securitydb.SecurityConnection;

/**
 * Utility class providing access to both database connections.
 * Replaces the old JpaUtil in a pure JDBC context.
 */
public class JdbcUtil {

    public static Connection getMedicalConnection() throws SQLException {
        return MedicalConnection.getConnection();
    }

    public static Connection getSecurityConnection() throws SQLException {
        return SecurityConnection.getConnection();
    }
}

