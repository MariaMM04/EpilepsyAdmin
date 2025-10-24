package org.example.entities_securitydb;

public class User {

    private int id;            // Primary key (auto-increment)
    private String email;      // Linked with Patient.email (foreign reference)
    private String password;   // Encrypted or hashed password
    private boolean active;    // Account enabled/disabled flag
    private int roleId;        // Foreign key referencing the Role table

    // Constructors
    public User() {}

    public User(String email, String password, int roleId) {
        this.email = email;
        this.password = password;
        this.roleId = roleId;
        this.active = true;
    }

    public User(int id, String email, String password, boolean active, int roleId) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.active = active;
        this.roleId = roleId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", active=" + active +
                ", roleId=" + roleId +
                '}';
    }
}
