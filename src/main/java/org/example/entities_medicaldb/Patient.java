package org.example.entities_medicaldb;

import java.time.LocalDate;

public class Patient {

    private int id;                 // Primary key (auto-increment)
    private String name;            // Patient's first name
    private String surname;         // Patient's last name
    private String email;           // Unique email address (linked with Users table)
    private String contact;         // Contact phone or alternative info
    private LocalDate dateOfBirth;  // Date of birth
    private String gender;          // Gender identity or biological sex
    private boolean active;         // Logical deletion flag (true = active)
    private int doctorId;           // Foreign key referencing assigned Doctor

    // Constructors
    public Patient() {}

    public Patient(String name, String surname, String email, String contact,
                   LocalDate dateOfBirth, String gender, int doctorId) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.contact = contact;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.doctorId = doctorId;
        this.active = true;
    }

    public Patient(int id, String name, String surname, String email, String contact,
                   LocalDate dateOfBirth, String gender, boolean active, int doctorId) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.contact = contact;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.active = active;
        this.doctorId = doctorId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    @Override
    public String toString() {
        return "Patient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", email='" + email + '\'' +
                ", contact='" + contact + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", gender='" + gender + '\'' +
                ", active=" + active +
                ", doctorId=" + doctorId +
                '}';
    }
}
