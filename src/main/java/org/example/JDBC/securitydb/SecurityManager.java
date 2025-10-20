package org.example.JDBC.securitydb;

import java.sql.Connection;
import java.sql.SQLException;

// Importa tus clases JDBC cuando las implementes
import org.example.JDBC.securitydb.UserJDBC;
import org.example.JDBC.securitydb.RoleJDBC;

/**
 * Gestiona las operaciones JDBC relacionadas con la base de datos "securitydb".
 * Similar al antiguo ConnectionManager pero modular y específico de seguridad.
 */
public class SecurityManager {

    private Connection connection;

    private UserJDBC userJDBC;
    private RoleJDBC roleJDBC;

    public SecurityManager() {
        try {
            this.connection = SecurityConnection.getConnection();
            System.out.println("Connected to securitydb ✅");

            // Inicializa las clases JDBC de cada entidad
            this.userJDBC = new UserJDBC(connection);
            this.roleJDBC = new RoleJDBC(connection);

        } catch (SQLException e) {
            System.err.println("Error connecting to securitydb: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public UserJDBC getUserJDBC() {
        return userJDBC;
    }

    public RoleJDBC getRoleJDBC() {
        return roleJDBC;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("securitydb connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing securitydb: " + e.getMessage());
        }
    }
}

