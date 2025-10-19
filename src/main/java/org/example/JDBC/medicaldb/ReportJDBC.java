package org.example.JDBC.medicaldb;

import org.example.entities_medicaldb.Report;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for managing JDBC operations for the Report table.
 * Used within MedicalManager.
 */
public class ReportJDBC {

    private final Connection connection;

    public ReportJDBC(Connection connection) {
        this.connection = connection;
    }

    /**
     * Inserts a new report
     */
    public boolean insertReport(Report report) {
        String sql = "INSERT INTO Report (date, symptoms) VALUES (?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, report.getDate() != null ? Date.valueOf(report.getDate()) : null);
            ps.setString(2, report.getSymptoms());
            ps.executeUpdate();
            System.out.println("Report inserted successfully (" + report.getDate() + ")");
            return true;

        } catch (SQLException e) {
            System.err.println("Error inserting report: " + e.getMessage());
            return false;
        }
    }

    /**
     * Searchs report by ID
     */
    public Report findReportById(int id) {
        String sql = "SELECT * FROM Report WHERE id = ?";
        Report report = null;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                report = new Report(
                        rs.getDate("date") != null ? rs.getDate("date").toLocalDate() : null,
                        rs.getString("symptoms"),
                        null
                );
                System.out.println("Report found (ID: " + id + ")");
            } else {
                System.out.println("No report found with ID: " + id);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Error finding report: " + e.getMessage());
        }

        return report;
    }

    /**
     * Retrieves all reports
     */
    public List<Report> getAllReports() {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM Report";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                reports.add(new Report(
                        rs.getDate("date") != null ? rs.getDate("date").toLocalDate() : null,
                        rs.getString("symptoms"),
                        null
                ));
            }

            System.out.println("Retrieved " + reports.size() + " reports.");

        } catch (SQLException e) {
            System.err.println("Error retrieving reports: " + e.getMessage());
        }

        return reports;
    }

    /**
     * Deletes a report by ID
     */
    public boolean deleteReport(int id) {
        String sql = "DELETE FROM Report WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            int affected = ps.executeUpdate();

            if (affected > 0) {
                System.out.println("Report deleted (ID: " + id + ")");
                return true;
            } else {
                System.out.println("No report found to delete with ID: " + id);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error deleting report: " + e.getMessage());
            return false;
        }
    }
}
