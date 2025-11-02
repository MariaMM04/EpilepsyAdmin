package org.example.entities_securitydb;

import com.google.gson.JsonObject;

public class User {

    private int id;            // Primary key (auto-increment)
    private String email;      // Linked with Patient.email (foreign reference)
    private String password;   // Encrypted or hashed password
    private boolean active;    // Account enabled/disabled flag
    private int role_id;        // Foreign key referencing the Role table

    // Constructors
    public User(String email, String password,int role_id, boolean active) {
        this.email = email;
        this.password = password;
        this.role_id = role_id;
        this.active = active;
    }
    public User(String email, String password,boolean active) {
        this.email = email;
        this.password = password;
        this.active = active;
    }

    public User(String email, String password, int role_id) {
        this.email = email;
        this.password = password;
        this.role_id = role_id;
        this.active = true;
    }

    public User(int id, String email, String password, boolean active, int role_id) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role_id = role_id;
        this.active = active;
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

    public int getRole_id() { return role_id; }
    public void setRole_id(int role_id) { this.role_id = role_id; }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", active=" + active +
                ", roleId=" + role_id +
                '}';
    }

    public JsonObject toJason() {
        JsonObject jason = new JsonObject();
        jason.addProperty("id", id);
        jason.addProperty("email", email);
        jason.addProperty("role", role_id); //TODO: change to role name
        return jason;
    }
}
