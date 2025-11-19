package org.example.JDBC.securitydb;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.example.JDBC.securitydb.*;
import org.example.entities_securitydb.Role;
import org.example.entities_securitydb.User;

/**
 * Central manager for the JDBC operations related to the {@code securitydb} database.
 * This class is focused exclusively on the security layer. It defines:
 * <ul>
 *     <li> Opens and maintains a single {@link Connection} to the {@code securitydb} database</li>
 *     <li> Initializes the Data Access Object classes {@link UserJDBC} and {@link RoleJDBC} </li>
 * </ul>
 *
 * @author MariaMM04
 * @author MamenCortes
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

    /**
     * Creates a new {@code SecurityManager}, establishing a connection with the database and initializing
     * the Data Access Objects {@link UserJDBC} and {@link RoleJDBC}.
     * If the connection cannot be established to the database, an error message will print.
     */
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

    /**
     * Closes safely the connection to the {@code securitydb} database if it is open.
     * If it cannot be closed, it will print an error message.
     */
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

