package org.example.JDBC.medicaldb;

import org.example.entities_medicaldb.Signal;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
     * Inserts a new signal record into the database.
     */
    public void insertSignal(Signal signal) {
        String sql = "INSERT INTO signal (path, date, comments, patient_id) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, signal.getPath());
            ps.setDate(2, signal.getDate() != null ? Date.valueOf(signal.getDate()) : null);
            ps.setString(3, signal.getComments());
            ps.setInt(4, signal.getPatientId());
            ps.executeUpdate();
            System.out.println("Signal inserted successfully: " + signal.getPath());
        } catch (SQLException e) {
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
        String path = rs.getString("path");
        LocalDate date = rs.getDate("date") != null ? rs.getDate("date").toLocalDate() : null;
        String comments = rs.getString("comments");
        int patientId = rs.getInt("patient_id");
        double sampleFrequency = rs.getDouble(("sample_frequency"));

        return new Signal(id, path, date, comments, patientId, sampleFrequency);
    }
}
