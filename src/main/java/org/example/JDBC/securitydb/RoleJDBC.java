package org.example.JDBC.securitydb;

import org.example.entities_securitydb.Role;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles JDBC operations for the Roles table.
 * Used within the SecurityManager.
 */
public class RoleJDBC {

    private final Connection connection;

    public RoleJDBC(Connection connection) {
        this.connection = connection;
    }

    /**
     * Inserts a new role into the database.
     * The role must include the role name and, optionally, the associated user ID.
     */
    public void insertRole(Role role) {
        String sql = "INSERT INTO roles (rolename) VALUES (?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, role.getRolename());
            ps.executeUpdate();
            System.out.println("Role inserted successfully: " + role.getRolename());
        } catch (SQLException e) {
            System.err.println("Error inserting role: " + e.getMessage());
        }
    }

    /**
     * Finds a role by its name.
     * Returns a Role object if found, or null if not found.
     */
    public Role findRoleByName(String rolename) {
        String sql = "SELECT * FROM roles WHERE rolename = ?";
        Role role = null;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, rolename);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                role = extractRoleFromResultSet(rs);
                System.out.println("Role found: " + rolename);
            } else {
                System.out.println("No role found with name: " + rolename);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Error finding role: " + e.getMessage());
        }

        return role;
    }

    /**
     * Finds a role by its name.
     * Returns a Role object if found, or null if not found.
     */
    public Role findRoleByID(int id) {
        String sql = "SELECT * FROM roles WHERE id = ?";
        Role role = null;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                role = extractRoleFromResultSet(rs);
                System.out.println("Role found: " + id);
            } else {
                System.out.println("No role found with ID: " + id);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Error finding role: " + e.getMessage());
        }

        return role;
    }

    /**
     * Retrieves all roles stored in the database.
     * Returns a list of Role objects.
     */
    public List<Role> getAllRoles() {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT * FROM roles";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                roles.add(extractRoleFromResultSet(rs));
            }

            System.out.println("Retrieved " + roles.size() + " roles.");
        } catch (SQLException e) {
            System.err.println("Error retrieving roles: " + e.getMessage());
        }

        return roles;
    }

    /**
     * Deletes a role permanently from the database by its name.
     */
    public void deleteRole(String rolename) {
        String sql = "DELETE FROM roles WHERE rolename = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, rolename);
            int affected = ps.executeUpdate();

            if (affected > 0) {
                System.out.println("Role deleted: " + rolename);
            } else {
                System.out.println("No role found to delete: " + rolename);
            }
        } catch (SQLException e) {
            System.err.println("Error deleting role: " + e.getMessage());
        }
    }

    /**
     * Utility method that converts a ResultSet row into a Role object.
     */
    private Role extractRoleFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String rolename = rs.getString("rolename");
        return new Role(id, rolename);
    }
}
