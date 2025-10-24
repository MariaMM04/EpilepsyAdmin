package org.example.entities_medicaldb;

import java.time.LocalDate;
import java.util.List;

public class Report {

    // --- Enum for possible patient symptoms ---
    public enum Symptom {
        HEADACHE,
        DIZZINESS,
        TREMORS,
        NAUSEA,
        LOSS_OF_CONSCIOUSNESS,
        PALPITATIONS,
        OTHER
    }

    private int id;                  // Primary key (auto-increment)
    private LocalDate date;          // Date of the report
    private List<Symptom> symptoms;  // List of selected symptoms (enum)
    private int patientId;           // Foreign key referencing Patient
    private int doctorId;            // Foreign key referencing Doctor
    private String notes;            // Additional comments or observations
    private boolean active;          // Logical deletion flag (true = active)

    //Constructors
    public Report() {}

    public Report(LocalDate date, List<Symptom> symptoms, String notes, int patientId, int doctorId) {
        this.date = date;
        this.symptoms = symptoms;
        this.notes = notes;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.active = true;
    }

    public Report(int id, LocalDate date, List<Symptom> symptoms, String notes, int patientId, int doctorId, boolean active) {
        this.id = id;
        this.date = date;
        this.symptoms = symptoms;
        this.notes = notes;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.active = active;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public List<Symptom> getSymptoms() { return symptoms; }
    public void setSymptoms(List<Symptom> symptoms) { this.symptoms = symptoms; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    // --- Utility ---
    @Override
    public String toString() {
        return "Report{" +
                "id=" + id +
                ", date=" + date +
                ", symptoms=" + symptoms +
                ", notes='" + notes + '\'' +
                ", patientId=" + patientId +
                ", doctorId=" + doctorId +
                ", active=" + active +
                '}';
    }
}

