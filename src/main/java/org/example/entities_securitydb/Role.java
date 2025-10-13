package org.example.entities_securitydb;

import javax.persistence.*;

@Entity
@Table(name = "Roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true, length = 50)
    private String rolename;

    @OneToOne
    @JoinColumn(name="user_id", unique = true)
    private User user;

    public Role() {}

    public Role(String name) {
        this.rolename = name;
    }

    // Getters y Setters
    public int getId() { return id; }

    public String getRolename() { return rolename; }
    public void setRolename(String name) { this.rolename = name; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}