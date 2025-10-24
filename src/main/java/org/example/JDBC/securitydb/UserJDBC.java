package org.example.JDBC.securitydb;

import org.example.entities_securitydb.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles JDBC operations for the Users table.
 * Only 'active' status can be modified (logical activation/deactivation).
 */
public class UserJDBC {

    private final Connection connection;

    public UserJDBC(Connection connection) {
        this.connection = connection;
    }

    /**
     * Inserts a new user into the database.
     */
    public boolean insertUser(User user) {
        String sql = "INSERT INTO users (email, password, active, role_id) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPassword());
            ps.setBoolean(3, user.isActive());
            ps.setInt(4, user.getRoleId());
            ps.executeUpdate();
            System.out.println("User inserted: " + user.getEmail());
            return true;
        } catch (SQLException e) {
            System.err.println("Error inserting user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves users by email.
     */
    public User findUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        User user = null;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                user = extractUserFromResultSet(rs);
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
     * Retrieves all users
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }

            System.out.println("Retrieved " + users.size() + " users.");

        } catch (SQLException e) {
            System.err.println("Error retrieving users: " + e.getMessage());
        }

        return users;
    }

    /**
     * Updates de active status of the user
     */
    public boolean updateUserActiveStatus(String email, boolean active) {
        String sql = "UPDATE users SET active = ? WHERE email = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setString(2, email);
            int affected = ps.executeUpdate();

            if (affected > 0) {
                System.out.println("User " + (active ? "activated" : "deactivated") + ": " + email);
                return true;
            } else {
                System.out.println("No user found to update: " + email);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error updating user active status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Helper method. Creates a User object from the current ResultSet row.
     */
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getBoolean("active"),
                rs.getInt("role_id")
        );
    }
}


