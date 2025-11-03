package org.example.entities_medicaldb;

import com.google.gson.JsonObject;

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
    private Doctor doctor;

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
        this.doctor = null;
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
        this.doctor = null;
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

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

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

    public JsonObject toJason() {
        JsonObject jason = new JsonObject();
        jason.addProperty("id", id);
        jason.addProperty("name", name);
        jason.addProperty("surname", surname);
        jason.addProperty("email", email);
        jason.addProperty("contact", contact);
        jason.addProperty("dateOfBirth", dateOfBirth.toString());
        jason.addProperty("gender", gender);
        jason.addProperty("active", active);
        jason.addProperty("doctorId", doctorId);
        return jason;
    }

    public static Patient fromJson(JsonObject jason) {
        Patient patient = new Patient();
        patient.setId(jason.get("id").getAsInt());
        patient.setName(jason.get("name").getAsString());
        patient.setSurname(jason.get("surname").getAsString());
        patient.setEmail(jason.get("email").getAsString());
        patient.setContact(jason.get("contact").getAsString());
        patient.setDateOfBirth(LocalDate.parse(jason.get("dateOfBirth").getAsString()));
        patient.setGender(jason.get("gender").getAsString());
        patient.setDoctorId(jason.get("doctorId").getAsInt());
        return patient;
    }
}
