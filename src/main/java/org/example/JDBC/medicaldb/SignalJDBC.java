package org.example.JDBC.medicaldb;

import org.example.entities_medicaldb.Signal;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for handling JDBC operations on the Signal table.
 * Used within MedicalManager.
 */
public class SignalJDBC {

    private final Connection connection;

    public SignalJDBC(Connection connection) {
        this.connection = connection;
    }

    /**
     * Inserts new signal into the data bases
     */
    public boolean insertSignal(Signal signal) {
        String sql = "INSERT INTO Signal (recording, date, frequency, timestamp, comments) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, signal.getRecording());
            ps.setDate(2, signal.getDate() != null ? Date.valueOf(signal.getDate()) : null);
            ps.setDouble(3, signal.getFrequency());
            ps.setTimestamp(4, signal.getTimestamp() != null ? Timestamp.valueOf(signal.getTimestamp()) : null);
            ps.setString(5, signal.getComments());
            ps.executeUpdate();
            System.out.println("Signal inserted successfully.");
            return true;

        } catch (SQLException e) {
            System.err.println("Error inserting signal: " + e.getMessage());
            return false;
        }
    }

    /**
     * Searches signal by id
     */
    public Signal findSignalById(int id) {
        String sql = "SELECT * FROM Signal WHERE id = ?";
        Signal signal = null;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                signal = new Signal(
                        rs.getString("recording"),
                        rs.getDate("date") != null ? rs.getDate("date").toLocalDate() : null,
                        rs.getDouble("frequency"),
                        rs.getTimestamp("timestamp") != null ? rs.getTimestamp("timestamp").toLocalDateTime() : null,
                        rs.getString("comments")
                );
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
     * Retrieves every signall registered
     */
    public List<Signal> getAllSignals() {
        List<Signal> signals = new ArrayList<>();
        String sql = "SELECT * FROM Signal";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                signals.add(new Signal(
                        rs.getString("recording"),
                        rs.getDate("date") != null ? rs.getDate("date").toLocalDate() : null,
                        rs.getDouble("frequency"),
                        rs.getTimestamp("timestamp") != null ? rs.getTimestamp("timestamp").toLocalDateTime() : null,
                        rs.getString("comments")
                ));
            }

            System.out.println("Retrieved " + signals.size() + " signals.");

        } catch (SQLException e) {
            System.err.println("Error retrieving signals: " + e.getMessage());
        }

        return signals;
    }

    /**
     * Deletes signal by id
     */
    public boolean deleteSignal(int id) {
        String sql = "DELETE FROM Signal WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            int affected = ps.executeUpdate();

            if (affected > 0) {
                System.out.println("Signal deleted (ID: " + id + ")");
                return true;
            } else {
                System.out.println("No signal found to delete with ID: " + id);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error deleting signal: " + e.getMessage());
            return false;
        }
    }
}
