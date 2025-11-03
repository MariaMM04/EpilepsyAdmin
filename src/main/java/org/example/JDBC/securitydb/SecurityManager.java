package org.example.JDBC.securitydb;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.example.JDBC.securitydb.*;
import org.example.entities_securitydb.Role;
import org.example.entities_securitydb.User;

/**
 * Gestiona las operaciones JDBC relacionadas con la base de datos "securitydb".
 * Similar al antiguo ConnectionManager pero modular y específico de seguridad.
 */
public class SecurityManager {

    public static void main(String[] args){
        SecurityManager sm = new SecurityManager();
        //User u = new User("test@example.com", "1234", true);
        //sm.userJDBC.insertUser(u);

        //User user1 = new User("admin@example.com", "1234", 3, true);
        //sm.userJDBC.insertUser(user1);
        List<User> users =  sm.userJDBC.getAllUsers();
        for(User user : users){
            System.out.println(user);
        }

        List<Role> roles = sm.roleJDBC.getAllRoles();
        for(Role rol : roles){
            System.out.println(rol);
        }

    }

    private Connection connection;

    private UserJDBC userJDBC;
    private RoleJDBC roleJDBC;

    public SecurityManager() {
        try {
            this.connection = SecurityConnection.getConnection();
            System.out.println("Connected to SQLite securitydb");

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
                System.out.println("SQLite securitydb connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing securitydb: " + e.getMessage());
        }
    }
}

