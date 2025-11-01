package org.example.entities_securitydb;

/**
 * Represents a user role (e.g., ADMIN, DOCTOR, PATIENT).
 * JDBC version — no JPA annotations.
 */
public class Role {

    // --- Fields ---
    private int id;           // Primary key (auto-increment)
    private String rolename;  // Name of the role (ADMIN, DOCTOR, PATIENT)
    private int userId;       // Foreign key referencing the User table

    // --- Constructors ---
    public Role() {}

    public Role(String rolename) {
        this.rolename = rolename;
    }

    public Role(String rolename, int userId) {
        this.rolename = rolename;
        this.userId = userId;
    }

    public Role(int id, String rolename, int userId) {
        this.id = id;
        this.rolename = rolename;
        this.userId = userId;
    }

    // --- Getters and Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getRolename() { return rolename; }
    public void setRolename(String rolename) { this.rolename = rolename; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    // --- Utility ---
    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", rolename='" + rolename + '\'' +
                ", userId=" + userId +
                '}';
    }
}
