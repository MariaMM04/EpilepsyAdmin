package org.example.JDBC.securitydb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.example.entities_securitydb.Role;
import org.example.entities_securitydb.User;

/**
 * Class responsible for JDBC operations related to the Roles table.
 * Used by SecurityManager.
 */
public class RoleJDBC {

    private final Connection connection;

    public RoleJDBC(Connection connection) {
        this.connection = connection;
    }

    /**
     * Inserts a new role in the Roles table
     */
    public boolean insertRole(Role role) {
        String sql = "INSERT INTO Roles (rolename, user_id) VALUES (?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, role.getRolename());

            // If the role has an associated user, its ID is used; otherwise, null
            if (role.getUser() != null) {
                ps.setInt(2, role.getUser().getId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }

            ps.executeUpdate();
            System.out.println("Role inserted successfully: " + role.getRolename());
            return true;

        } catch (SQLException e) {
            System.err.println("Error inserting role: " + e.getMessage());
            return false;
        }
    }

    /**
     * Search role by name
     */
    public Role findRoleByName(String rolename) {
        String sql = "SELECT * FROM Roles WHERE rolename = ?";
        Role role = null;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, rolename);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                role = new Role(rs.getString("rolename"));
                System.out.println("✅ Role found: " + rolename);
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
     * Retrieves all the roles in the table
     */
    public List<Role> getAllRoles() {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT * FROM Roles";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                roles.add(new Role(rs.getString("rolename")));
            }

            System.out.println("Retrieved " + roles.size() + " roles.");

        } catch (SQLException e) {
            System.err.println("Error retrieving roles: " + e.getMessage());
        }

        return roles;
    }

    /**
     * Delete role by name
     */
    public boolean deleteRole(String rolename) {
        String sql = "DELETE FROM Roles WHERE rolename = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, rolename);
            int affected = ps.executeUpdate();

            if (affected > 0) {
                System.out.println("Role deleted: " + rolename);
                return true;
            } else {
                System.out.println("No role found to delete: " + rolename);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error deleting role: " + e.getMessage());
            return false;
        }
    }
}
