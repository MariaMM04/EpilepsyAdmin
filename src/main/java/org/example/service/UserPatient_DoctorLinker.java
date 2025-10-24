package org.example.service;

import org.example.JDBC.medicaldb.MedicalManager;
import org.example.JDBC.securitydb.SecurityManager;
import org.example.entities_medicaldb.Patient;
import org.example.entities_medicaldb.Doctor;
import org.example.entities_securitydb.User;

/**
 * Utility service that provides linkage operations between
 * users (securitydb) and doctors/patients (medicaldb).
 * Replaces the JPA-based UserPatient_DoctorLinker.
 */
public class UserPatient_DoctorLinker {

    private final MedicalManager medicalManager;
    private final SecurityManager securityManager;

    public UserPatient_DoctorLinker(MedicalManager medicalManager, SecurityManager securityManager) {
        this.medicalManager = medicalManager;
        this.securityManager = securityManager;
    }

    /**
     * Finds a patient by their associated user email.
     * The relation is made by the shared "email" field between both databases.
     */
    public Patient findPatientByUserEmail(String email) {
        return medicalManager.getPatientJDBC().findPatientByEmail(email);
    }

    /**
     * Finds a doctor by their associated user email.
     * The relation is made by the shared "email" field between both databases.
     */
    public Doctor findDoctorByUserEmail(String email) {
        return medicalManager.getDoctorJDBC().findDoctorByEmail(email);
    }

    /**
     * Finds a user by their email in the security database.
     */
    public User findUserByEmail(String email) {
        return securityManager.getUserJDBC().findUserByEmail(email);
    }
}
