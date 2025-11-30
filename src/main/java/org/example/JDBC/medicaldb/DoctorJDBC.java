package org.example.JDBC.medicaldb;

import org.example.entities_medicaldb.Doctor;
import org.example.entities_medicaldb.Patient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles JDBC operations for the Doctor table.
 *
 * @author MariaMM04
 * @author MamenCortes
 */
public class DoctorJDBC {

    private final Connection connection;

    public DoctorJDBC(Connection connection) {
        this.connection = connection;
    }

    /**
     * Inserts an existing {@code Doctor} into the medical database {@code medicaldb} by a SQL query specified
     * inside the method
     *
     * @param doctor    An existing doctor
     * @return          boolean value of the performed insertion. May be:
     *                  <code> true </code> if the doctor was successfully inserted into the database
     *                  <code> false </code> otherwise
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
            ps.setBoolean(7, false); //false by default
            ps.executeUpdate();
            System.out.println("Doctor inserted: " + doctor.getEmail());
            return true;
        } catch (SQLException e) {
            System.err.println("Error inserting doctor: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves {@code Doctor} by its email address from the medical database by a SQL query.
     *
     * @param email     the desired doctor's we want to retrieve email address
     * @return          the desired doctor we want to retrieve
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
     * Retrieves {@code Doctor} by its unique identifier (id) from the medical database by a SQL query.
     *
     * @param id     the desired doctor's we want to retrieve id
     * @return       the desired doctor we want to retrieve
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
     * Retrieves all {@code Doctor} instances stored in the medical database by a SQL query.
     *
     * @return  A list of all the doctors inside the medical database
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
     * Updates the active status of the {@code Doctor} instance with the corresponding email.
     *
     * @param email     the patient who is updated email.
     * @param active    the patient's logical flag
     * @return          boolean value of the performed update. May be:
     *                  <code> true </code> if the patient was successfully updated into the database
     *                  <code> false </code> otherwise
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
     * Helper method that creates a {@code Doctor} instance from the current ResultSet row.
     *
     * @param rs        the ResultSet which contains the information to create a Doctor as a SQL query
     * @return          the created Doctor instance
     * @throws SQLException     if the SQL query is invalid
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

    /**
     * Retrieves a {@code Doctor} instance associated to the desired patient.
     *
     * @param patient_id    the patient's unique identifier associated to the desired doctor
     * @return      the created Doctor instance
     */
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

