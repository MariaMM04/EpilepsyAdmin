package org.example.JDBC.medicaldb;

import org.example.entities_medicaldb.Doctor;
import org.example.entities_medicaldb.Patient;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles JDBC operations for the Patient table.
 */
public class PatientJDBC {

    private final Connection connection;

    public PatientJDBC(Connection connection) {
        this.connection = connection;
    }

    /**
     * Inserts a new patient into the database.
     */
    public boolean insertPatient(Patient patient) {
        String sql = "INSERT INTO patient (name, surname, email, contact, date_of_birth, gender, active, doctor_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, patient.getName());
            ps.setString(2, patient.getSurname());
            ps.setString(3, patient.getEmail());
            ps.setString(4, patient.getContact());
            ps.setDate(5, patient.getDateOfBirth() != null ? Date.valueOf(patient.getDateOfBirth()) : null);
            ps.setString(6, patient.getGender());
            ps.setBoolean(7, patient.isActive());
            ps.setInt(8, patient.getDoctorId());
            ps.executeUpdate();
            System.out.println("Patient inserted: " + patient.getEmail());
            return true;
        } catch (SQLException e) {
            System.err.println("Error inserting patient: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves patient by email.
     */
    public Patient findPatientByEmail(String email) {
        String sql = "SELECT * FROM patient WHERE email = ?";
        Patient patient = null;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                patient = extractPatientFromResultSet(rs);
                System.out.println("Patient found: " + email);
            } else {
                System.out.println("No patient found with email: " + email);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Error finding patient: " + e.getMessage());
        }

        return patient;
    }

    /**
     * Retrieves patient by ID.
     */
    public Patient findPatientByID(Integer id) {
        String sql = "SELECT * FROM patient WHERE id = ?";
        Patient patient = null;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                patient = extractPatientFromResultSet(rs);
                System.out.println("Patient found: " + id);
            } else {
                System.out.println("No patient found with email: " + id);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Error finding patient: " + e.getMessage());
        }

        return patient;
    }

    /**
     * Retrieves all patients
     */
    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patient";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                patients.add(extractPatientFromResultSet(rs));
            }

            System.out.println("Retrieved " + patients.size() + " patients.");

        } catch (SQLException e) {
            System.err.println("Error retrieving patients: " + e.getMessage());
        }

        return patients;
    }

    /**
     * Retrieves all patients
     */
    public List<Patient> getPatientsOfDoctor(int doctorId) {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patient WHERE  doctor_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                patients.add(extractPatientFromResultSet(rs));
            }

            System.out.println("Retrieved " + patients.size() + " patients.");

        } catch (SQLException e) {
            System.err.println("Error retrieving patients: " + e.getMessage());
        }

        return patients;
    }

    /**
     * Updates de active status of the patient
     */
    public boolean updatePatientActiveStatus(String email, boolean active) {
        String sql = "UPDATE patient SET active = ? WHERE email = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setString(2, email);
            int affected = ps.executeUpdate();

            if (affected > 0) {
                System.out.println("Patient " + (active ? "activated" : "deactivated") + ": " + email);
                return true;
            } else {
                System.out.println("No patient found to update: " + email);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error updating patient active status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Helper method. Creates a Patient object from the current ResultSet row.
     */
    private Patient extractPatientFromResultSet(ResultSet rs) throws SQLException {
        //Timestamp ts = rs.getTimestamp("date_of_birth");
        //LocalDate dateOfBirth = ts != null ? ts.toLocalDateTime().toLocalDate() : null;
        LocalDate dateOfBirth = null;
        long millis = rs.getLong("date_of_birth");
        if (!rs.wasNull()) {
            dateOfBirth = Instant.ofEpochMilli(millis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }
        return new Patient(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("surname"),
                rs.getString("email"),
                rs.getString("contact"),
                dateOfBirth,
                rs.getString("gender"),
                rs.getBoolean("active"),
                rs.getInt("doctor_id")
        );
    }


}


