package org.example.entities_medicaldb;

import java.time.LocalDate;
import javax.persistence.*;

@Entity
@Table(name="Patient")
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String surname;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 20)
    private String contact;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(length = 50)
    private String gender;

    // Realtions between tables
    @ManyToOne
    @JoinColumn(name = "report_id")
    private Report report;

    @ManyToOne
    @JoinColumn(name = "signal_id")
    private Signal signal;

    // Empty Constructor
    public Patient() {}

    // Constructor
    public Patient(String name, String surname, String email, String contact, LocalDate dateOfBirth, String gender) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.contact = contact;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }

    // Getters y Setters
    public int getId() { return id; }

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

    public Report getReport() { return report; }
    public void setReport(Report report) { this.report = report; }

    public Signal getSignal() { return signal; }
    public void setSignal(Signal signal) { this.signal = signal; }
}
