package org.example.entities_medicaldb;

import javax.persistence.*;

@Entity
@Table(name="Doctor")
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String surname;

    @Column(length = 20)
    private String contact;

    @Column(nullable = false, unique = true, length = 150)
    private String email;


    // Realtions between tables
    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    // Empty Constructor
    public Doctor() {}

    // Constructor
    public Doctor(String name, String surname, String contact) {
        this.name = name;
        this.surname = surname;
        this.contact = contact;
        this.email = email;
    }
    // Getters y Setters
    public int getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
}
