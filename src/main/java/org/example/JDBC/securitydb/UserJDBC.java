package org.example.JDBC.securitydb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.example.entities_securitydb.User;

/**
 * Class responsible for JDBC operations related to the Users table.
 * Used by SecurityManager.
 */
public class UserJDBC {

    private final Connection connection;

    public UserJDBC(Connection connection) {
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
                        rs.getString("password")
                );
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
                        rs.getString("password")
                ));
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
    public boolean deleteUser(String email) {
        String sql = "DELETE FROM Users WHERE email = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            int affected = ps.executeUpdate();

            if (affected > 0) {
                System.out.println("User deleted: " + email);
                return true;
            } else {
                System.out.println("No user found to delete: " + email);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }
}

