package org.example.JDBC.medicaldb;

import org.example.entities_medicaldb.Doctor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for handling JDBC operations for the Doctor table.
 * Used within MedicalManager
 */
public class DoctorJDBC {

    private final Connection connection;

    public DoctorJDBC(Connection connection) {
        this.connection = connection;
    }

    /**
     * Inserts new doctor into the data base
     */
    public boolean insertDoctor(Doctor doctor) {
        String sql = "INSERT INTO Doctor (name, surname, contact, email) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, doctor.getName());
            ps.setString(2, doctor.getSurname());
            ps.setString(3, doctor.getContact());
            ps.setString(4, doctor.getEmail());
            ps.executeUpdate();
            System.out.println("Doctor inserted successfully: " + doctor.getEmail());
            return true;

        } catch (SQLException e) {
            System.err.println("Error inserting doctor: " + e.getMessage());
            return false;
        }
    }

    /**
     * Searchs doctor by email
     */
    public Doctor findDoctorByEmail(String email) {
        String sql = "SELECT * FROM Doctor WHERE email = ?";
        Doctor doctor = null;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                doctor = new Doctor(
                        rs.getString("name"),
                        rs.getString("surname"),
                        rs.getString("contact")
                );
                doctor.setEmail(rs.getString("email"));
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
     * Retrieves all doctors registered
     */
    public List<Doctor> getAllDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT * FROM Doctor";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Doctor doctor = new Doctor(
                        rs.getString("name"),
                        rs.getString("surname"),
                        rs.getString("contact")
                );
                doctor.setEmail(rs.getString("email"));
                doctors.add(doctor);
            }

            System.out.println("Retrieved " + doctors.size() + " doctors.");

        } catch (SQLException e) {
            System.err.println("Error retrieving doctors: " + e.getMessage());
        }

        return doctors;
    }

    /**
     * Delete doctor by email
     */
    public boolean deleteDoctor(String email) {
        String sql = "DELETE FROM Doctor WHERE email = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            int affected = ps.executeUpdate();

            if (affected > 0) {
                System.out.println("Doctor deleted: " + email);
                return true;
            } else {
                System.out.println("No doctor found to delete: " + email);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error deleting doctor: " + e.getMessage());
            return false;
        }
    }
}
