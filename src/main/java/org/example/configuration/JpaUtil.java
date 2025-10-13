package org.example.configuration;


import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class JpaUtil {

    private static final EntityManagerFactory medicalEMF =
            Persistence.createEntityManagerFactory("MedicalPU");

    private static final EntityManagerFactory securityEMF =
            Persistence.createEntityManagerFactory("SecurityPU");

    public static EntityManagerFactory getMedicalEMF() {
        return medicalEMF;
    }

    public static EntityManagerFactory getSecurityEMF() {
        return securityEMF;
    }
}
