package org.example.entities_medicaldb;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Signal")
public class Signal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 100)
    private String recording;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Double frequency;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(length = 500)
    private String comments;

    // Empty Constructor
    public Signal() {}

    // Constructor
    public Signal(String recording, LocalDate date, Double frequency, LocalDateTime timestamp, String comments) {
        this.recording = recording;
        this.date = date;
        this.frequency = frequency;
        this.timestamp = timestamp;
        this.comments = comments;
    }

    // Getters y Setters
    public int getId() { return id; }

    public String getRecording() { return recording; }
    public void setRecording(String recording) { this.recording = recording; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Double getFrequency() { return frequency; }
    public void setFrequency(Double frequency) { this.frequency = frequency; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}