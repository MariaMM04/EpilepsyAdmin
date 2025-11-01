package org.example.entities_securitydb;

/**
 * Represents a user role (e.g., ADMIN, DOCTOR, PATIENT).
 * JDBC version — no JPA annotations.
 */
public class Role {

    // --- Fields ---
    private int id;           // Primary key (auto-increment)
    private String rolename;  // Name of the role (ADMIN, DOCTOR, PATIENT)

    // --- Constructors ---
    public Role() {}

    public Role(String rolename) {
        this.rolename = rolename;
    }

    public Role(int id, String rolename) {
        this.id = id;
        this.rolename = rolename;
    }

    // --- Getters and Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getRolename() { return rolename; }
    public void setRolename(String rolename) { this.rolename = rolename; }


    // --- Utility ---
    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", rolename='" + rolename + '\'' +
                '}';
    }
}
