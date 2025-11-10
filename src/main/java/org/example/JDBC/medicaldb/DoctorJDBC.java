package org.example.JDBC.medicaldb;

import org.example.entities_medicaldb.Doctor;
import org.example.entities_medicaldb.Patient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles JDBC operations for the Doctor table.
 */
public class DoctorJDBC {

    private final Connection connection;

    public DoctorJDBC(Connection connection) {
        this.connection = connection;
    }

    /**
     * Inserts a new doctor into the database.
     */
    public boolean insertDoctor(Doctor doctor) {
        String sql = "INSERT INTO doctor (name, surname, contact, email, department, speciality, active) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, doctor.getName());
            ps.setString(2, doctor.getSurname());
            ps.setString(3, doctor.getContact());
            ps.setString(4, doctor.getEmail());
            ps.setString(5, doctor.getDepartment());
            ps.setString(6, doctor.getSpeciality());
            ps.setBoolean(7, doctor.isActive());
            ps.executeUpdate();
            System.out.println("Doctor inserted: " + doctor.getEmail());
            return true;
        } catch (SQLException e) {
            System.err.println("Error inserting doctor: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves doctors by email.
     */
    public Doctor findDoctorByEmail(String email) {
        String sql = "SELECT * FROM doctor WHERE email = ?";
        Doctor doctor = null;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                doctor = extractDoctorFromResultSet(rs);
                System.out.println("Doctor found: " + email);
            } else {
                System.out.println("No doctor found with email: " + email);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Error finding doctor: " + e.getMessage());
        }

        return doctor;
    }

    /**
     * Retrieves doctors by email.
     */
    public Doctor findDoctorById(Integer id) {
        String sql = "SELECT * FROM doctor WHERE id = ?";
        Doctor doctor = null;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                doctor = extractDoctorFromResultSet(rs);
                System.out.println("Doctor found: " + id);
            } else {
                System.out.println("No doctor found with email: " + id);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Error finding doctor: " + e.getMessage());
        }

        return doctor;
    }

    /**
     * Retrieves doctors by ID.
     */
    public Doctor getDoctor(int id) {
        String sql = "SELECT * FROM doctor WHERE id = ?";
        Doctor doctor = null;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, String.valueOf(id));
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                doctor = extractDoctorFromResultSet(rs);
                System.out.println("Doctor found: " + id);
            } else {
                System.out.println("No doctor found with ID: " + id);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Error finding doctor: " + e.getMessage());
        }

        return doctor;
    }

    /**
     * Retrieves all doctors
     */
    public List<Doctor> getAllDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT * FROM doctor";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                doctors.add(extractDoctorFromResultSet(rs));
            }

            System.out.println("Retrieved " + doctors.size() + " doctors.");

        } catch (SQLException e) {
            System.err.println("Error retrieving doctors: " + e.getMessage());
        }

        return doctors;
    }

    /**
     * Updates de active status of the doctor
     */
    public boolean updateDoctorActiveStatus(String email, boolean active) {
        String sql = "UPDATE doctor SET active = ? WHERE email = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setString(2, email);
            int affected = ps.executeUpdate();

            if (affected > 0) {
                System.out.println("Doctor " + (active ? "activated" : "deactivated") + ": " + email);
                return true;
            } else {
                System.out.println("No doctor found to update: " + email);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error updating doctor active status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Helper method. Creates a Doctor object from the current ResultSet row.
     */
    private Doctor extractDoctorFromResultSet(ResultSet rs) throws SQLException {
        return new Doctor(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("surname"),
                rs.getString("contact"),
                rs.getString("email"),
                rs.getString("department"),
                rs.getString("speciality"),
                rs.getBoolean("active")
        );
    }

    public Doctor getDoctorFromPatient(Integer patient_id){
        String sql = "SELECT d.* FROM Doctor d JOIN Patient p ON p.doctor_id = d.id WHERE p.id = ?";
        Doctor doctor = null;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, patient_id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                doctor = extractDoctorFromResultSet(rs);
                System.out.println("Doctor found of patient: " + patient_id);
            } else {
                System.out.println("No Doctor found for patient: " + patient_id);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Error finding doctor: " + e.getMessage());
        }

        return doctor;
    }
}

