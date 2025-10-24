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
 * Handles JDBC operations for the Report table.
 * Works with the simplified entity (no 'active' column).
 */
public class ReportJDBC {

    private final Connection connection;

    public ReportJDBC(Connection connection) {
        this.connection = connection;
    }

    /**
     * Inserts a new report into the database.
     */
    public boolean insertReport(Report report) {
        String sql = "INSERT INTO report (date, symptoms, notes, patient_id, doctor_id) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, report.getDate() != null ? Date.valueOf(report.getDate()) : null);

            // Convert List<Symptom> -> comma-separated string
            String symptomsStr = report.getSymptoms() != null
                    ? report.getSymptoms().stream().map(Enum::name).collect(Collectors.joining(","))
                    : null;
            ps.setString(2, symptomsStr);

            ps.setString(3, report.getNotes());
            ps.setInt(4, report.getPatientId());
            ps.setInt(5, report.getDoctorId());

            ps.executeUpdate();
            System.out.println("Report inserted successfully for patient ID: " + report.getPatientId());
            return true;

        } catch (SQLException e) {
            System.err.println("Error inserting report: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves a single report by its ID.
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
     * Retrieves all reports.
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
     * Retrieves all reports belonging to a specific patient.
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
     * Permanently deletes a report from the database.
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
     * Helper method. Creates a Report object from the current ResultSet row.
     */
    private Report extractReportFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        LocalDate date = rs.getDate("date") != null ? rs.getDate("date").toLocalDate() : null;

        // Convert comma-separated String to List<Symptom>
        String symptomsStr = rs.getString("symptoms");
        List<Symptom> symptoms = (symptomsStr != null && !symptomsStr.isEmpty())
                ? Arrays.stream(symptomsStr.split(",")).map(Symptom::valueOf).collect(Collectors.toList())
                : new ArrayList<>();

        String notes = rs.getString("notes");
        int patientId = rs.getInt("patient_id");
        int doctorId = rs.getInt("doctor_id");

        return new Report(id, date, symptoms, notes, patientId, doctorId, true);
    }
}
