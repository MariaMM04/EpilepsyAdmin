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
 * The {@code SignalJDBC} class handles JDBC operations for the simplified {@code Signal} entity.
 * This class is typically created and managed by {@link MedicalManager} which provides a shared
 * {@link Connection} to the medical database.
 *
 * @author MariaMM04
 * @author MamenCortes
 */

public class SignalJDBC {

    private final Connection connection;

    public SignalJDBC(Connection connection) {
        this.connection = connection;
    }

    /**
     * Turns the information of the path into bytes
     */
    /**
     * The information inside the path input is turned into a byte[]
     *
     * @param path  The path where the signal information is stored
     * @return      The information transformed into a byte[] format
     * @throws IOException  if the information from the file cannot be read.
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
     * Turns the information stored in a bytes into a String as a path to a file containing the same information.
     *
     * @param compressedData    The information stored in bytes
     * @param outputPath        The output path created with the same information stored
     * @throws IOException      if the information inside the bytes cannot be read
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
     * Inserts an existing {@code Signal} into the medical database {@code medicaldb} by a SQL query specified
     * inside the method
     *
     * @param signal    An existing report
     * @return          boolean value of the performed insertion. May be:
     *                  <code> true </code> if the signal was successfully inserted into the database
     *                  <code> false </code> otherwise
     */
    public void insertSignal(Signal signal) {
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
     * Retrieves {@code Signal} by its unique identifier (id) from the medical database by a SQL query.
     *
     * @param id     the desired signal's we want to retrieve id
     * @return       the desired signal we want to retrieve
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
     * Retrieves all {@code Signal} instances stored in the medical database by a SQL query.
     *
     * @return  A list of all the signal inside the medical database
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
     * Permanently deletes the {@code Signal} instance specifying its unique identifier
     *
     * @param id    the signal's id that will be deleted
     * @return      boolean value of the performed deletion. May be:
     *              <code> true </code> if the signal was successfully deleted from the database
     *              <code> false </code> otherwise
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
     * Retrieves all {@code Signal} instances associated to the desired patient.
     *
     * @param patientId    the patient's unique identifier associated to the desired report
     * @return             a list of all Signal instances
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
     * Utility method that creates a {@code Signal} instance from the current ResultSet row.
     *
     * @param rs        the ResultSet which contains the information to create a Signal instance as a SQL query
     * @return          the created Signal instance
     * @throws SQLException     if the SQL query is invalid
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
    /**
     * Updates the comments of the {@code Signal} instance by its corresponding signalId
     *
     * @param signalId      the signal's id that will be changes
     * @param newComments   the new comments inside the signal
     * @return              boolean value of the performed update. May be:
     *                      <code> true </code> if the signal was successfully updated in the database
     *                      <code> false </code> otherwise
     */
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
