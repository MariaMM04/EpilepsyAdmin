package org.example;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

//Main → AdminLinkService → Managers → JDBC → Base de datos

public class MainJPA {
    public static void main(String[] args) {
        EntityManagerFactory emfMedical = Persistence.createEntityManagerFactory("MedicalPU");
        EntityManagerFactory emfSecurity = Persistence.createEntityManagerFactory("SecurityPU");

        EntityManager emMedical = emfMedical.createEntityManager();
        EntityManager emSecurity = emfSecurity.createEntityManager();

        System.out.println("Connection established with both data bases.");

        emMedical.close();
        emSecurity.close();
        emfMedical.close();
        emfSecurity.close();
    }
}
