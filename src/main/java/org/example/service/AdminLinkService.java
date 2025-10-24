package org.example.service;

import org.example.JDBC.medicaldb.MedicalManager;
import org.example.JDBC.securitydb.SecurityManager;
import org.example.entities_medicaldb.Doctor;
import org.example.entities_medicaldb.Patient;
import org.example.entities_securitydb.User;

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
     * Creates a new doctor and their corresponding user.
     */
    public void createDoctorAndUser(Doctor doctor, User user) {
        securityManager.getUserJDBC().insertUser(user);
        medicalManager.getDoctorJDBC().insertDoctor(doctor);
        System.out.println("Doctor and User created successfully: " + user.getEmail());
    }

    /**
     * Creates a new patient and their corresponding user.
     */
    public void createPatientAndUser(Patient patient, User user) {
        securityManager.getUserJDBC().insertUser(user);
        medicalManager.getPatientJDBC().insertPatient(patient);
        System.out.println("Patient and User created successfully: " + user.getEmail());
    }

    /**
     * Deactivates a doctor and their corresponding user by email.
     * Performs logical deletion by setting active = false.
     */
    public void deactivateDoctorAndUser(String email) {
        medicalManager.getDoctorJDBC().updateDoctorActiveStatus(email, false);
        securityManager.getUserJDBC().updateUserActiveStatus(email, false);
        System.out.println("Doctor and corresponding User deactivated (" + email + ")");
    }

    /**
     * Deactivates a patient and their corresponding user by email.
     * Performs logical deletion by setting active = false.
     */
    public void deactivatePatientAndUser(String email) {
        medicalManager.getPatientJDBC().updatePatientActiveStatus(email, false);
        securityManager.getUserJDBC().updateUserActiveStatus(email, false);
        System.out.println("Patient and corresponding User deactivated (" + email + ")");
    }

    /**
     * Reactivates a doctor and their corresponding user by email.
     * Useful for restoring logically deleted accounts.
     */
    public void reactivateDoctorAndUser(String email) {
        medicalManager.getDoctorJDBC().updateDoctorActiveStatus(email, true);
        securityManager.getUserJDBC().updateUserActiveStatus(email, true);
        System.out.println("Doctor and corresponding User reactivated (" + email + ")");
    }

    /**
     * Reactivates a patient and their corresponding user by email.
     */
    public void reactivatePatientAndUser(String email) {
        medicalManager.getPatientJDBC().updatePatientActiveStatus(email, true);
        securityManager.getUserJDBC().updateUserActiveStatus(email, true);
        System.out.println("Patient and corresponding User reactivated (" + email + ")");
    }
}

