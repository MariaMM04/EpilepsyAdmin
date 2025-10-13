package org.example.entities_medicaldb;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Report")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, name = "date")
    private LocalDate date;

    @Column(length = 255)
    private String symptoms;


    // Empty Constructor
    public Report() {}

    // Constructor
    public Report(LocalDate date, String symptoms, String notes) {
        this.date = date;
        this.symptoms = symptoms;
    }

    // Getters y Setters
    public int getId() { return id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

}
