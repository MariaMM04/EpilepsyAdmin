package org.example.JDBC.securitydb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.example.entities_securitydb.Role;
import org.example.entities_securitydb.User; // Import User class


/**
 * Class responsible for JDBC operations related to the Users table.
 * Used by SecurityManager.
 */
public class UserJDBC {

    private static Connection connection;

    public UserJDBC(Connection connection) throws SQLException {
        this.connection = connection;
    }

    /**
     * Inserts new user in the Users table
     */
    public boolean insertUser(User user) {
        String sql = "INSERT INTO Users (email, password) VALUES (?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPassword());
            ps.executeUpdate();
            System.out.println("User inserted successfully: " + user.getEmail());
            return true;
        } catch (SQLException e) {
            System.err.println("Error inserting user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Search user by email
     */
    public User findUserByEmail(String email) {
        String sql = "SELECT * FROM Users WHERE email = ?";
        User user = null;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                user = new User(
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getBoolean("active"));
                System.out.println("User found: " + email);
            } else {
                System.out.println("No user found with email: " + email);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Error finding user: " + e.getMessage());
        }

        return user;
    }

    /**
     * Retrieves all the users from the table
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM Users";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                users.add(new User(
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getBoolean("active")));
            }
            System.out.println("Retrieved " + users.size() + " users.");

        } catch (SQLException e) {
            System.err.println("Error retrieving users: " + e.getMessage());
        }

        return users;
    }

    /**
     * Deletes user by email
     */
    public boolean updateUserActiveStatus(String email, boolean active) {
        String sql = "UPDATE users SET active = ? WHERE email = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setString(2, email);
            int affected = ps.executeUpdate();

            if (affected > 0) {
                System.out.println("🟡 User " + (active ? "activated" : "deactivated") + ": " + email);
                return true;
            } else {
                System.out.println("⚠️ No user found to update: " + email);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error updating user active status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Inserts a user in the DB.
     * Returns true if the user is inserted.
     * returns false if the user couldn't be inserted
     */

    public boolean register(String email, String password, boolean active) {

        if (email.isBlank() || email == null || password.isBlank() || password == null || active == true) { // If the email or password are empty do not create
            return false;
        } else if (isUser(email)) {
            return false;
        } else try {
            {
                insertUser(new User(email, password,true));
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }


    /**
     * Retrieves a user from the DB with a given name and password.
     * If the user doesn't exist or the password doesn't match, returns null
     */

    // DEVOLVER USER ENTERO
    //TODO: comprobar que la contraseña proporcionada coincide con la de la base de datos
    public User login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return null; // invalid input
        }
        String sql = ("SELECT * FROM users WHERE email = ?"); // By only fetching the email we can check if it is unique or not
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            // Encriptar contraseña passwordEncrypted = bla bla
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User(rs.getString("email"), rs.getString("password"), rs.getBoolean("active"));
                u.setRoleId(rs.getInt("role_id"));
                return u;
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return null;
    }

    /**
     * Checks if a user exists by a given email
     */
    public boolean isUser(String email) {
        if (email == null || email.isBlank()){
            return false;// invalid input
        }

        String sql = ("SELECT * FROM users WHERE email = ? ");
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next(); //returns true if rs.next() exists
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * Changes the password of a given user
     */
    public boolean changePassword(User u, String password) {
        if (u==null || u.getId() <= 0 || password == null || password.isBlank()){
            return false;
        }
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setString(1, password);
            ps.setInt(2, u.getId());
            // to check if one row has been changed
            int row = ps.executeUpdate();
            return row == 1;
        } catch (SQLException e) {
            throw new RuntimeException(e); // return false
        }
    }
}

