package org.example;

import org.example.JDBC.medicaldb.MedicalManager;
import org.example.JDBC.securitydb.SecurityManager;
import org.example.entities_medicaldb.Doctor;
import org.example.entities_medicaldb.Patient;
import org.example.entities_securitydb.User;
import org.example.service.AdminLinkService;

import java.time.LocalDate;

public class MainAdmin {

    public static void main(String[] args) {

        // --- Initializing managers ---
        MedicalManager medicalManager = new MedicalManager();
        SecurityManager securityManager = new SecurityManager();

        // --- Service that synchronizes both databases ---
        AdminLinkService adminService = new AdminLinkService(medicalManager, securityManager);

        System.out.println("=== Admin system started ===");

        // --- Example: Create a new doctor and associated user ---
        Doctor d = new Doctor("Laura", "Santos", "612345678");
        d.setEmail("laura@hospital.com");
        User userDoctor = new User("laura@hospital.com", "secure123");
        adminService.createDoctorAndUser(d, userDoctor);

        // --- Example: Create a new patient and associated user ---
        Patient p = new Patient("Carlos", "López", "carlos@correo.com", "654789123",
                LocalDate.of(1995, 2, 15), "Male");
        User userPatient = new User("carlos@correo.com", "patient123");
        adminService.createPatientAndUser(p, userPatient);

        // --- Example: synchronized deletion ---
        adminService.deleteDoctorAndUser("laura@hospital.com");
        adminService.deletePatientAndUser("carlos@correo.com");

        System.out.println("=== Operations finished ===");

        // --- Closing connections ---
        medicalManager.close();
        securityManager.close();
    }
}
