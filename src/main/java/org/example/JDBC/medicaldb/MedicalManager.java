package org.example.JDBC.medicaldb;

import java.sql.Connection;
import java.sql.SQLException;

// Importa aquí tus clases JDBC concretas (cuando las crees)
import org.example.JDBC.medicaldb.PatientJDBC;
import org.example.JDBC.medicaldb.DoctorJDBC;
import org.example.JDBC.medicaldb.ReportJDBC;
import org.example.JDBC.medicaldb.SignalJDBC;

/**
 * Gestiona todas las operaciones JDBC relacionadas con la base de datos "medicaldb".
 * Equivale a un ConnectionManager pero solo para el contexto médico.
 */
public class MedicalManager {

    private Connection connection;

    // DAOs (Data Access Objects) o clases JDBC para cada entidad
    private PatientJDBC patientJDBC;
    private DoctorJDBC doctorJDBC;
    private ReportJDBC reportJDBC;
    private SignalJDBC signalJDBC;

    public MedicalManager() {
        try {
            this.connection = MedicalConnection.getConnection();
            System.out.println("Connected to medicaldb");

            // Inicializa las clases JDBC de cada entidad
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
                System.out.println("medicaldb connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing medicaldb: " + e.getMessage());
        }
    }
}

