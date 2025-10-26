package org.example.entities_medicaldb;

import java.time.LocalDate;

public class Signal {

    private int id; // Primary key (auto-increment)
    private String path; // Path to the recorded signal (ECG, ACC, etc.)
    private LocalDate date; // Date of the signal recording
    private String comments; // Optional notes about the signal
    private int patientId; // Foreign key referencing the Patient table
    private double sampleFrequency;

    // Constructors
    public Signal() {}

    public Signal(int id, String path, LocalDate date, String comments, int patientId, double sampleFrequency) {
        this.id = id;
        this.path = path;
        this.date = date;
        this.comments = comments;
        this.patientId = patientId;
        this.sampleFrequency = sampleFrequency;

    }

    public Signal(String path, LocalDate date, String comments, int patientId, double  sampleFrequency) {
        this.path = path;
        this.date = date;
        this.comments = comments;
        this.patientId = patientId;
        this.sampleFrequency = sampleFrequency;

    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public double getSampleFrequency() { return sampleFrequency; }
    public void setSampleFrequency(double sampleFrequency) { this.sampleFrequency = sampleFrequency; }


    @Override
    public String toString() {
        return "Signal{" +
                "id=" + id +
                ", path='" + path + '\'' +
                ", date=" + date +
                ", comments='" + comments + '\'' +
                ", patientId=" + patientId +
                ", sampleFrequency=" + sampleFrequency +
                '}';
    }
}
