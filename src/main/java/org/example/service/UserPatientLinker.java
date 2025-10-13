package org.example.service;

import javax.persistence.EntityManager;
import org.example.configuration.JpaUtil;
import org.example.entities_medicaldb.Patient;
import org.example.entities_securitydb.User;

public class UserPatientLinker {

    public static Patient findPatientByUserEmail(String email) {
        EntityManager em = JpaUtil.getMedicalEMF().createEntityManager();
        Patient patient = null;

        try {
            patient = em.createQuery(
                            "SELECT p FROM Patient p WHERE p.email = :email", Patient.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (Exception e) {
            System.out.println("Unable to find patient with email: " + email);
        } finally {
            em.close();
        }

        return patient;
    }

    public static User findUserByEmail(String email) {
        EntityManager em = JpaUtil.getSecurityEMF().createEntityManager();
        User user = null;

        try {
            user = em.createQuery(
                            "SELECT u FROM User u WHERE u.email = :email", User.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (Exception e) {
            System.out.println("Unable to find user with email: " + email);
        } finally {
            em.close();
        }

        return user;
    }
}
