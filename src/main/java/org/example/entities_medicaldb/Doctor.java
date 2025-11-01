package org.example.entities_medicaldb;

import com.google.gson.JsonObject;

public class Doctor {

    // Fields
    private int id;             // Primary key (auto-increment)
    private String name;        // Doctor's first name
    private String surname;     // Doctor's last name
    private String contact;     // Optional phone or other contact info
    private String email;       // Unique email identifier
    private String department;  // Department or area of work
    private String speciality;  // Medical speciality
    private boolean active;     // Logical deletion flag (true = active)

    // Constructors
    public Doctor() {}

    public Doctor(String name, String surname, String contact) {
        this.name = name;
        this.surname = surname;
        this.contact = contact;

    }

    public Doctor(String name, String surname, String contact, String email,
                  String department, String speciality) {
        this.name = name;
        this.surname = surname;
        this.contact = contact;
        this.email = email;
        this.department = department;
        this.speciality = speciality;
        this.active = true;
    }

    public Doctor(int id, String name, String surname, String contact, String email,
                  String department, String speciality, boolean active) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.contact = contact;
        this.email = email;
        this.department = department;
        this.speciality = speciality;
        this.active = active;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getSpeciality() { return speciality; }
    public void setSpeciality(String speciality) { this.speciality = speciality; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    // --- Utility ---
    @Override
    public String toString() {
        return "Doctor{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", contact='" + contact + '\'' +
                ", email='" + email + '\'' +
                ", department='" + department + '\'' +
                ", speciality='" + speciality + '\'' +
                ", active=" + active +
                '}';
    }

    public JsonObject toJason() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("name", name);
        json.addProperty("surname", surname);
        json.addProperty("contact", contact);
        json.addProperty("email", email);
        json.addProperty("department", department);
        json.addProperty("speciality", speciality);
        return json;
    }

    public static Doctor fromJason(JsonObject json) {
        Doctor doctor = new Doctor();
        doctor.setId(json.get("id").getAsInt());
        doctor.setName(json.get("name").getAsString());
        doctor.setSurname(json.get("surname").getAsString());
        doctor.setContact(json.get("contact").getAsString());
        doctor.setEmail(json.get("email").getAsString());
        doctor.setDepartment(json.get("department").getAsString());
        doctor.setSpeciality(json.get("speciality").getAsString());
        return doctor;
    }
}
