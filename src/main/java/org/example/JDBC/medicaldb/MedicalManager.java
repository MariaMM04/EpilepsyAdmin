package org.example.JDBC.medicaldb;

import java.sql.Connection;
import java.sql.SQLException;

import org.example.JDBC.medicaldb.*;
import org.example.entities_securitydb.Role;

/**
 * Gestiona todas las operaciones JDBC relacionadas con la base de datos "medicaldb".
 * Equivale a un ConnectionManager pero solo para el contexto médico.
 */
public class MedicalManager {

    private Connection connection;

    // DAOs (Data Access Objects)
    private PatientJDBC patientJDBC;
    private DoctorJDBC doctorJDBC;
    private ReportJDBC reportJDBC;
    private SignalJDBC signalJDBC;

    public MedicalManager() {
        try {
            this.connection = MedicalConnection.getConnection();
            System.out.println("Connected to SQLite medicaldb");

            this.patientJDBC = new PatientJDBC(connection);
            this.doctorJDBC = new DoctorJDBC(connection);
            this.reportJDBC = new ReportJDBC(connection);
            this.signalJDBC = new SignalJDBC(connection);

        } catch (SQLException e) {
            System.err.println("Error connecting to medicaldb: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public PatientJDBC getPatientJDBC() {
        return patientJDBC;
    }

    public DoctorJDBC getDoctorJDBC() {
        return doctorJDBC;
    }

    public ReportJDBC getReportJDBC() {
        return reportJDBC;
    }

    public SignalJDBC getSignalJDBC() {
        return signalJDBC;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("SQLite medicaldb connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing medicaldb: " + e.getMessage());
        }
    }
}

