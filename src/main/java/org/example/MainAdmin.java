package org.example;

import org.example.JDBC.medicaldb.MedicalManager;
import org.example.JDBC.securitydb.SecurityManager;
import org.example.entities_medicaldb.Doctor;
import org.example.entities_medicaldb.Patient;
import org.example.entities_securitydb.User;
import org.example.service.AdminLinkService;

import java.time.LocalDate;

/**
 * Demonstration class for managing doctors, patients and users.
 * Uses AdminLinkService to synchronize operations between medicaldb and securitydb.
 * Compatible with the current JDBC + logical deletion model.
 */
public class MainAdmin {

    public static void main(String[] args) {

        // --- Initialize database managers ---
        MedicalManager medicalManager = new MedicalManager();
        SecurityManager securityManager = new SecurityManager();

        // --- Initialize linking service ---
        AdminLinkService adminService = new AdminLinkService(medicalManager, securityManager);

        System.out.println("=== Admin system started ===");

        // -----------------------------------------------------
        // CREATE DOCTOR + USER
        // -----------------------------------------------------
        Doctor doctor = new Doctor("Laura", "Santos", "612345678");
        doctor.setEmail("laura@hospital.com");
        doctor.setDepartment("Neurology");
        doctor.setSpeciality("Epilepsy Research");
        doctor.setActive(true);

        User doctorUser = new User("laura@hospital.com", "secure123", 56);
        doctorUser.setActive(true);

        adminService.createDoctorAndUser(doctor, doctorUser);

        // -----------------------------------------------------
        // CREATE PATIENT + USER
        // -----------------------------------------------------
        Patient patient = new Patient(
                "Carlos",
                "López",
                "carlos@correo.com",
                "654789123",
                LocalDate.of(1995, 2, 15),
                "Male",1
        );
        patient.setActive(true);

        User patientUser = new User("carlos@correo.com", "patient123", 23);
        patientUser.setActive(true);

        adminService.createPatientAndUser(patient, patientUser);

        // -----------------------------------------------------
        // DEACTIVATE BOTH
        // -----------------------------------------------------
        System.out.println("\n--- Deactivating doctor and patient ---");
        adminService.deactivateDoctorAndUser("laura@hospital.com");
        adminService.deactivatePatientAndUser("carlos@correo.com");

        // -----------------------------------------------------
        // REACTIVATE BOTH
        // -----------------------------------------------------
        System.out.println("\n--- Reactivating doctor and patient ---");
        adminService.reactivateDoctorAndUser("laura@hospital.com");
        adminService.reactivatePatientAndUser("carlos@correo.com");

        // -----------------------------------------------------
        // CLOSE CONNECTIONS
        // -----------------------------------------------------
        medicalManager.close();
        securityManager.close();

        System.out.println("\n=== All operations completed successfully ===");
    }
}
