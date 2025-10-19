package org.example.service;

import org.example.JDBC.medicaldb.MedicalManager;
import org.example.JDBC.securitydb.SecurityManager;
import org.example.entities_medicaldb.Doctor;
import org.example.entities_medicaldb.Patient;
import org.example.entities_securitydb.User;

/**
 * Service class that synchronizes operations between medicaldb and securitydb.
 * Centralizes the registration and deregistration of users, doctors, and patients.
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
        boolean userCreated = securityManager.getUserJDBC().insertUser(user);
        boolean doctorCreated = medicalManager.getDoctorJDBC().insertDoctor(doctor);

        if (userCreated && doctorCreated) {
            System.out.println("Doctor and User created successfully.");
        } else {
            System.out.println("Some error occurred during creation.");
        }
    }

    /**
     * Creates a new patient and their corresponding user.
     */
    public void createPatientAndUser(Patient patient, User user) {
        boolean userCreated = securityManager.getUserJDBC().insertUser(user);
        boolean patientCreated = medicalManager.getPatientJDBC().insertPatient(patient);

        if (userCreated && patientCreated) {
            System.out.println("Patient and User created successfully.");
        } else {
            System.out.println("Some error occurred during creation.");
        }
    }

    /**
     * Deletes a doctor and their associated email username.
     */
    public void deleteDoctorAndUser(String email) {
        boolean doctorDeleted = medicalManager.getDoctorJDBC().deleteDoctor(email);
        boolean userDeleted = securityManager.getUserJDBC().deleteUser(email);

        if (doctorDeleted || userDeleted) {
            System.out.println("Doctor and corresponding User deleted successfully (" + email + ")");
        } else {
            System.out.println("No doctor or user found with email: " + email);
        }
    }

    /**
     * Delete a patient and their associated user by email.
     */
    public void deletePatientAndUser(String email) {
        boolean patientDeleted = medicalManager.getPatientJDBC().deletePatient(email);
        boolean userDeleted = securityManager.getUserJDBC().deleteUser(email);

        if (patientDeleted || userDeleted) {
            System.out.println("Patient and corresponding User deleted successfully (" + email + ")");
        } else {
            System.out.println("No patient or user found with email: " + email);
        }
    }
}

