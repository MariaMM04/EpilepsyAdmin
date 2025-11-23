package ui;

import java.time.LocalDate;

public class SignalMetadataDTO {
    public int signalId;
    public LocalDate date;
    public int patientId;
    public String comments;
    public int samplingFrequency;
    public String timestamp;
    public String zipFileName;

    public SignalMetadataDTO(int signalId, LocalDate date, String comments, int patientId, int samplingFrequency, String timestamp, String zipFileName) {
    }

    public SignalMetadataDTO() {

    }
}
