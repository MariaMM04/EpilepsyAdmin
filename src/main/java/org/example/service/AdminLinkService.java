package org.example.service;

import Exceptions.RegisterError;
import org.example.JDBC.medicaldb.MedicalManager;
import org.example.JDBC.securitydb.SecurityManager;
import org.example.entities_medicaldb.Doctor;
import org.example.entities_medicaldb.Patient;
import org.example.entities_securitydb.User;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class that synchronizes operations between medicaldb and securitydb.
 * Uses logical deactivation (active = false) instead of physical deletion.
 */
public class AdminLinkService {

    private final MedicalManager medicalManager;
    private final SecurityManager securityManager;

    public AdminLinkService(MedicalManager medicalManager, SecurityManager securityManager) {
        this.medicalManager = medicalManager;
        this.securityManager = securityManager;
    }

    /**
     * Method to create a User and a Doctor at the same time with the email connection they share
     * @param user
     * @param doctor
     * @return boolean true if the objects were created and false if not
     * @throws SQLException
     */

    public Boolean createUserAndDoctor(User user, Doctor doctor) throws SQLException {
        Boolean result = false;
        try {
            securityManager.getConnection().setAutoCommit(false);
            medicalManager.getConnection().setAutoCommit(false);

            // Get's inserted in securitydb
            securityManager.getUserJDBC().register(user);

            // Get's inserted in medicaldb
            medicalManager.getDoctorJDBC().insertDoctor(doctor);

            // If there's no errors:
            securityManager.getConnection().commit();
            medicalManager.getConnection().commit();
            result = true;

        } catch (SQLException | RegisterError e) {
            System.err.println("Error detected, rolling back both transactions: " + e.getMessage());
            if (securityManager.getConnection() != null) securityManager.getConnection().rollback();
            if (medicalManager.getConnection() != null) medicalManager.getConnection().rollback();
        } finally {
            if (securityManager.getConnection() != null) securityManager.getConnection().setAutoCommit(true);
            if (medicalManager.getConnection() != null) medicalManager.getConnection().setAutoCommit(true);
        }
        return result;
    }

    /**
     * Method to create a User and a Doctor at the same time with the email connection they share
     * @param user
     * @param patient
     * @return boolean true if the objects were created and false if not
     * @throws SQLException
     */

    public Boolean createUserAndPatient(User user, Patient patient) throws SQLException {
        Boolean result = false;
        try {
            securityManager.getConnection().setAutoCommit(false);
            medicalManager.getConnection().setAutoCommit(false);

            securityManager.getUserJDBC().register(user);

            medicalManager.getPatientJDBC().insertPatient(patient);

            securityManager.getConnection().commit();
            medicalManager.getConnection().commit();
            result = true;

        } catch (SQLException | RegisterError e) {
            System.out.println("Error detected, rolling back both transactions: " + e.getMessage());
            if (securityManager.getConnection() != null) securityManager.getConnection().rollback();
            if (medicalManager.getConnection() != null) medicalManager.getConnection().rollback();
        } finally {
            if (securityManager.getConnection() != null) securityManager.getConnection().setAutoCommit(true);
            if (medicalManager.getConnection() != null) medicalManager.getConnection().setAutoCommit(true);
        }
        return result;
    }

    /**
     * Method to change the active status of a Doctor by their email
     * @param email to search for the Doctor
     * @param status active or not active
     * @return boolean true if the objects were created and false if not
     * @throws SQLException
     */

    public Boolean changeDoctorStatus(String email, Boolean status) throws SQLException {
        Boolean result = false;
        try {
            securityManager.getConnection().setAutoCommit(false);
            medicalManager.getConnection().setAutoCommit(false);

            securityManager.getUserJDBC().updateUserActiveStatus(email, status);

            medicalManager.getDoctorJDBC().updateDoctorActiveStatus(email, status);

            securityManager.getConnection().commit();
            medicalManager.getConnection().commit();
            System.out.println("Doctor and corresponding User deactivated (" + email + ")");
            result = true;

        } catch (SQLException e) {
            System.err.println("Error detected, rolling back both transactions: " + e.getMessage());
            if (securityManager.getConnection() != null) securityManager.getConnection().rollback();
            if (medicalManager.getConnection() != null) medicalManager.getConnection().rollback();
        } finally {
            if (securityManager.getConnection() != null) securityManager.getConnection().setAutoCommit(true);
            if (medicalManager.getConnection() != null) medicalManager.getConnection().setAutoCommit(true);
        }
        return result;
    }

    /**
     * Method to change the active status of a Patient by their email
     * @param email to search for the Patient
     * @param status active or not active
     * @return boolean true if the objects were created and false if not
     * @throws SQLException
     */

    public Boolean changePatientStatus(String email, Boolean status) throws SQLException {
        Boolean result = false;
        try {
            securityManager.getConnection().setAutoCommit(false);
            medicalManager.getConnection().setAutoCommit(false);

            securityManager.getUserJDBC().updateUserActiveStatus(email, status);

            medicalManager.getPatientJDBC().updatePatientActiveStatus(email, status);

            securityManager.getConnection().commit();
            medicalManager.getConnection().commit();
            System.out.println("Patient and corresponding User deactivated (" + email + ")");
            result = true;

        } catch (SQLException e) {
            System.err.println("Error detected, rolling back both transactions: " + e.getMessage());
            if (securityManager.getConnection() != null) securityManager.getConnection().rollback();
            if (medicalManager.getConnection() != null) medicalManager.getConnection().rollback();
        } finally {
            if (securityManager.getConnection() != null) securityManager.getConnection().setAutoCommit(true);
            if (medicalManager.getConnection() != null) medicalManager.getConnection().setAutoCommit(true);
        }
        return result;
    }

    /**
     * Deactivates a Doctor and their corresponding user by email.
     * Performs logical deletion by setting active = false.
     * @param email
     */
    public void deactivateDoctorAndUser(String email) {
        medicalManager.getDoctorJDBC().updateDoctorActiveStatus(email, false);
        securityManager.getUserJDBC().updateUserActiveStatus(email, false);
        System.out.println("Doctor and corresponding User deactivated (" + email + ")");
    }

    /**
     * Deactivates a Patient and their corresponding user by email.
     * Performs logical deletion by setting active = false.
     * @param email
     */
    public void deactivatePatientAndUser(String email) {
        medicalManager.getPatientJDBC().updatePatientActiveStatus(email, false);
        securityManager.getUserJDBC().updateUserActiveStatus(email, false);
        System.out.println("Patient and corresponding User deactivated (" + email + ")");
    }

    /**
     * Reactivates a Doctor and their corresponding user by email.
     * Useful for restoring logically deleted accounts.
     * @param email
     */
    public void reactivateDoctorAndUser(String email) {
        medicalManager.getDoctorJDBC().updateDoctorActiveStatus(email, true);
        securityManager.getUserJDBC().updateUserActiveStatus(email, true);
        System.out.println("Doctor and corresponding User reactivated (" + email + ")");
    }

    /**
     * Reactivates a Patient and their corresponding user by email.
     * Useful for restoring logically deleted accounts.
     * @param email
     */
    public void reactivatePatientAndUser(String email) {
        medicalManager.getPatientJDBC().updatePatientActiveStatus(email, true);
        securityManager.getUserJDBC().updateUserActiveStatus(email, true);
        System.out.println("Patient and corresponding User reactivated (" + email + ")");
    }

    /**
     * Retrieves all patients of a Doctor
     * @return
     */
    public List<Patient> getAllPatientsWithDoctor() {
        List<Patient> patients = medicalManager.getPatientJDBC().getAllPatients();
        for (Patient patient : patients) {
            patient.setDoctor(medicalManager.getDoctorJDBC().getDoctor(patient.getDoctorId()));
        }
        return patients;
    }

    public Patient findPatientByUserId(int user_id) {
        User u = securityManager.getUserJDBC().findUserByID(user_id);
        return medicalManager.getPatientJDBC().findPatientByEmail(u.getEmail());
    }
}


