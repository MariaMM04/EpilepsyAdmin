package org.example.JDBC.medicaldb;

import org.example.entities_medicaldb.Patient;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for handling JDBC operations on the Patient table.
 * Used within MedicalManager.
 */
public class PatientJDBC {

    private final Connection connection;

    public PatientJDBC(Connection connection) {
        this.connection = connection;
    }

    /**
     * Inserts new patient into the data base
     */
    public boolean insertPatient(Patient patient) {
        String sql = "INSERT INTO Patient (name, surname, email, contact, date_of_birth, gender) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, patient.getName());
            ps.setString(2, patient.getSurname());
            ps.setString(3, patient.getEmail());
            ps.setString(4, patient.getContact());
            ps.setDate(5, patient.getDateOfBirth() != null ? Date.valueOf(patient.getDateOfBirth()) : null);
            ps.setString(6, patient.getGender());
            ps.executeUpdate();
            System.out.println("Patient inserted successfully: " + patient.getEmail());
            return true;

        } catch (SQLException e) {
            System.err.println("Error inserting patient: " + e.getMessage());
            return false;
        }
    }

    /**
     * Searchs patients by email
     */
    public Patient findPatientByEmail(String email) {
        String sql = "SELECT * FROM Patient WHERE email = ?";
        Patient patient = null;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                patient = new Patient(
                        rs.getString("name"),
                        rs.getString("surname"),
                        rs.getString("email"),
                        rs.getString("contact"),
                        rs.getDate("date_of_birth") != null ? rs.getDate("date_of_birth").toLocalDate() : null,
                        rs.getString("gender")
                );
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
     * Retrieves all patients registered
     */
    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM Patient";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                patients.add(new Patient(
                        rs.getString("name"),
                        rs.getString("surname"),
                        rs.getString("email"),
                        rs.getString("contact"),
                        rs.getDate("date_of_birth") != null ? rs.getDate("date_of_birth").toLocalDate() : null,
                        rs.getString("gender")
                ));
            }

            System.out.println("Retrieved " + patients.size() + " patients.");

        } catch (SQLException e) {
            System.err.println("Error retrieving patients: " + e.getMessage());
        }

        return patients;
    }

    /**
     * Deletes patient by email
     */
    public boolean deletePatient(String email) {
        String sql = "DELETE FROM Patient WHERE email = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            int affected = ps.executeUpdate();

            if (affected > 0) {
                System.out.println("Patient deleted: " + email);
                return true;
            } else {
                System.out.println("No patient found to delete: " + email);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error deleting patient: " + e.getMessage());
            return false;
        }
    }
}

