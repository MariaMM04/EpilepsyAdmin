package org.example.JDBC.medicaldb;

import org.example.entities_medicaldb.Signal;

import java.io.*;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import java.util.zip.GZIPInputStream;

/**
 * Handles JDBC operations for the Signal table.
 * Used within the MedicalManager.
 */
public class SignalJDBC {

    private final Connection connection;

    public SignalJDBC(Connection connection) {
        this.connection = connection;
    }

    /**
     * Turns the information of the path into a byte[]
     */
    private static byte[] compressFile(String path) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (FileInputStream fis = new FileInputStream(path);
             GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                gzip.write(buffer, 0, len);
            }
        }
        System.out.println("File compressed successfully to");
        return bos.toByteArray();
    }


    /**
     * Turns the information of the byte[] to a path
     */
    public static void decompressToFile(byte[] compressedData, String outputPath) throws IOException {
        // Creates the path if it doesn't exist
        File outputFile = new File(outputPath);
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        // Descompresses the bytes
        try (GZIPInputStream gzipIn = new GZIPInputStream(new ByteArrayInputStream(compressedData));
             FileOutputStream fileOut = new FileOutputStream(outputFile)) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipIn.read(buffer)) != -1) {
                fileOut.write(buffer, 0, len);
            }
        }

        System.out.println("File decompressed successfully to: " + outputPath);
    }


    /**
     * Inserts a new signal record into the database.
     */
    public void insertSignal(Signal signal) {
        //TODO = falta la sampling_frequency
        String sql = "INSERT INTO signal (path, date, comments, patient_id) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBytes(1, compressFile(signal.getPath()));
            ps.setDate(2, signal.getDate() != null ? Date.valueOf(signal.getDate()) : null);
            ps.setString(3, signal.getComments());
            ps.setInt(4, signal.getPatientId());
            ps.setDouble(5,signal.getSampleFrequency());
            ps.executeUpdate();
            System.out.println("Signal inserted successfully: " + signal.getPath());
        } catch (SQLException | IOException e) {
            System.err.println("Error inserting signal: " + e.getMessage());
        }
    }

    /**
     * Finds a signal by its ID.
     * Returns a Signal object if found, or null if no match exists.
     */
    public Signal findSignalById(int id) {
        String sql = "SELECT * FROM signal WHERE id = ?";
        Signal signal = null;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                signal = extractSignalFromResultSet(rs);
                System.out.println("Signal found with ID: " + id);
            } else {
                System.out.println("No signal found with ID: " + id);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Error finding signal: " + e.getMessage());
        }

        return signal;
    }

    /**
     * Retrieves all signals stored in the database.
     * Returns a list of Signal objects.
     */
    public List<Signal> getAllSignals() {
        List<Signal> signals = new ArrayList<>();
        String sql = "SELECT * FROM signal";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                signals.add(extractSignalFromResultSet(rs));
            }

            System.out.println("Retrieved " + signals.size() + " signals.");
        } catch (SQLException e) {
            System.err.println("Error retrieving signals: " + e.getMessage());
        }

        return signals;
    }

    /**
     * Deletes a signal permanently from the database by its ID.
     */
    public void deleteSignal(int id) {
        String sql = "DELETE FROM signal WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            int affected = ps.executeUpdate();

            if (affected > 0) {
                System.out.println("Signal deleted (ID: " + id + ")");
            } else {
                System.out.println("No signal found to delete with ID: " + id);
            }
        } catch (SQLException e) {
            System.err.println("Error deleting signal: " + e.getMessage());
        }
    }

    /**
     * Retrieves all signals belonging to a specific patient.
     * Returns a list of Signal objects.
     */
    public List<Signal> getSignalsByPatientId(int patientId) {
        List<Signal> signals = new ArrayList<>();
        String sql = "SELECT * FROM signal WHERE patient_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                signals.add(extractSignalFromResultSet(rs));
            }

            rs.close();
            System.out.println("Retrieved " + signals.size() + " signals for patient ID: " + patientId);
        } catch (SQLException e) {
            System.err.println("Error retrieving signals by patient: " + e.getMessage());
        }

        return signals;
    }

    /**
     * Utility method that converts a ResultSet row into a Signal object.
     */
    private Signal extractSignalFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        byte[] compressed = rs.getBytes("path");

        // Descompress to a temporal file
        String path = "output/signal_" + id + ".xlsx";
        try {
            decompressToFile(compressed, path);
        } catch (IOException e) {
            System.err.println("Error decompressing signal: " + e.getMessage());
        }
        //LocalDate date = rs.getDate("date") != null ? rs.getDate("date").toLocalDate() : null;
        LocalDate date = null;
        long millis = rs.getLong("date");
        if (!rs.wasNull()) {
            date = Instant.ofEpochMilli(millis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }
        //LocalDate date = rs.getDate("date") != null ? rs.getDate("date").toLocalDate() : null;
        String comments = rs.getString("comments");
        int patientId = rs.getInt("patient_id");
        double sampleFrequency = rs.getDouble(("sample_frequency"));

        return new Signal(id, path, date, comments, patientId, sampleFrequency);
    }

    public boolean updateSignalComments(int signalId, String newComments) {
        String sql = "UPDATE Signal SET comments = ? WHERE id = ?";

        try(PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newComments);
            pstmt.setInt(2, signalId);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Comments updated successfully for signal ID: " + signalId);
                return true;
            } else {
                System.out.println("No signal found with ID: " + signalId);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error updating comments: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {

        MedicalManager medicalManager = new MedicalManager();
        //carlos32@gmail.com = id 5 = signal num = 0

        Signal signal = new Signal();
        signal.setPatientId(5);
        signal.setPath("path");
        signal.setDate(LocalDate.now());
        signal.setComments("");
        signal.setSampleFrequency(100.0);

        medicalManager.getSignalJDBC().insertSignal(signal);
        List<Signal> signals = medicalManager.getSignalJDBC().getAllSignals();
        System.out.println("All signals found: " + signals.size());
        for (Signal s : signals) {
            System.out.println(s);
        }

    }

}
