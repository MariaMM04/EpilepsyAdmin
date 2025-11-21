package org.example.JDBC.medicaldb;

import org.example.entities_medicaldb.Report;
import org.example.entities_medicaldb.Report.Symptom;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The {@code ReportJDBC} class handles JDBC operations for the simplified {@code Report} entity without the
 * {@code active} column. Newly created instances are considered active by default.
 * This class is typically created and managed by {@link MedicalManager} which provides a shared
 * {@link Connection} to the medical database.
 *
 * @author MariaMM04
 * @author MamenCortes
 */
public class ReportJDBC {

    private final Connection connection;

    /**
     * Creates a {@code ReportJDBC} instance that uses the given JDBC {@link Connection} to access the
     * Report table in the database
     *
     * @param connection    active JDBC connection to the {@code medicaldb} database
     */

    public ReportJDBC(Connection connection) {
        this.connection = connection;

    }
    /**
     * Inserts an existing {@code Report} into the medical database {@code medicaldb} by a SQL query specified
     * inside the method
     *
     * @param report    An existing report
     * @return          boolean value of the performed insertion. May be:
     *                  <code> true </code> if the report was successfully inserted into the database
     *                  <code> false </code> otherwise
     */
    public boolean insertReport(Report report) {
        String sql = "INSERT INTO report (date, symptoms, patient_id) VALUES (?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, report.getDate() != null ? Date.valueOf(report.getDate()) : null);

            // Convert List<Symptom> -> comma-separated string
            String symptomsStr = report.getSymptoms() != null
                    ? report.getSymptoms().stream().map(Enum::name).collect(Collectors.joining(","))
                    : null;
            ps.setString(2, symptomsStr);

            ps.setInt(4, report.getPatientId());

            ps.executeUpdate();
            System.out.println("Report inserted successfully for patient ID: " + report.getPatientId());
            return true;

        } catch (SQLException e) {
            System.err.println("Error inserting report: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves {@code Report} by its unique identifier (id) from the medical database by a SQL query.
     *
     * @param id     the desired report's we want to retrieve id
     * @return       the desired report we want to retrieve
     */
    public Report findReportById(int id) {
        String sql = "SELECT * FROM report WHERE id = ?";
        Report report = null;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                report = extractReportFromResultSet(rs);
                System.out.println("Report found (ID: " + id + ")");
            } else {
                System.out.println("No report found with ID: " + id);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Error retrieving report: " + e.getMessage());
        }

        return report;
    }

    /**
     * Retrieves all {@code Report} instances stored in the medical database by a SQL query.
     *
     * @return  A list of all the reports inside the medical database
     */
    public List<Report> getAllReports() {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM report";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                reports.add(extractReportFromResultSet(rs));
            }

            System.out.println("Retrieved " + reports.size() + " reports from database.");

        } catch (SQLException e) {
            System.err.println("Error retrieving reports: " + e.getMessage());
        }

        return reports;
    }

    /**
     * Retrieves all {@code Report} instances associated to the desired patient.
     *
     * @param patientId    the patient's unique identifier associated to the desired report
     * @return             a list of all Report instances
     */
    public List<Report> getReportsByPatientId(int patientId) {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM report WHERE patient_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                reports.add(extractReportFromResultSet(rs));
            }

            rs.close();
            System.out.println("Retrieved " + reports.size() + " reports for patient ID: " + patientId);

        } catch (SQLException e) {
            System.err.println("Error retrieving reports for patient: " + e.getMessage());
        }

        return reports;
    }

    /**
     * Permanently deletes the {@code Report} instance specifying its unique identifier
     *
     * @param id    the report's id that will be deleted
     * @return          boolean value of the performed deletion. May be:
     *                  <code> true </code> if the report was successfully deleted from the database
     *                  <code> false </code> otherwise
     */
    public boolean deleteReport(int id) {
        String sql = "DELETE FROM report WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            int affected = ps.executeUpdate();

            if (affected > 0) {
                System.out.println("Report deleted permanently (ID: " + id + ")");
                return true;
            } else {
                System.out.println("No report found to delete (ID: " + id + ")");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error deleting report: " + e.getMessage());
            return false;
        }
    }

    /**
     * Helper method that creates a {@code Report} instance from the current ResultSet row.
     *
     * @param rs        the ResultSet which contains the information to create a Report instance as a SQL query
     * @return          the created Report instance
     * @throws SQLException     if the SQL query is invalid
     */
    private Report extractReportFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        LocalDate date = rs.getDate("date") != null ? rs.getDate("date").toLocalDate() : null;

        // Convert comma-separated String to List<Symptom>
        String symptomsStr = rs.getString("symptoms");
        List<Symptom> symptoms = (symptomsStr != null && !symptomsStr.isEmpty())
                ? Arrays.stream(symptomsStr.split(",")).map(Symptom::valueOf).collect(Collectors.toList())
                : new ArrayList<>();

        int patientId = rs.getInt("patient_id");


        return new Report(id, date, symptoms, patientId);
    }
}
