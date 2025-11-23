package org.example.JDBC.medicaldb;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.example.JDBC.medicaldb.*;
import org.example.JDBC.securitydb.SecurityManager;
import org.example.entities_medicaldb.Doctor;
import org.example.entities_medicaldb.Patient;
import org.example.entities_medicaldb.Report;
import org.example.entities_medicaldb.Signal;
import org.example.entities_securitydb.Role;
import org.example.entities_securitydb.User;

/**
 * The {@code MedicalManager} class manages all operations related to the {@code medicaldb} database.
 * This class acts like a ConnectionManager but for the medical context. This class opens one database
 * connection and creates one instance of each Data Access Object using this connection.
 * It encapsulates:
 * <ul>
 *     <li> An active JDBC connection to the {@code medicaldb} database</li>
 *     <li> Initialization of Data Access Object classes:
 *          {@link PatientJDBC} responsible for JDBC operations on the {@code Patient} table
 *          {@link DoctorJDBC} responsible for JDBC operation on the {@code Doctor} table
 *          {@link ReportJDBC} responsible for JDBC operations on the {@code Report} table
 *          {@link SignalJDBC} responsible for JDBC operations on the {@code Signal} table</li>
 * </ul>
 *
 * <p>
 *     In the current fields, it retrieves all patients using {@link PatientJDBC#getAllPatients()} and prints them.
 * </p>
 *
 * @author MariaMM04
 * @author MamenCortes
 */
public class MedicalManager {

    public static void main(String[] args) {
        MedicalManager medicalManager = new MedicalManager();
        SecurityManager securityManager = new SecurityManager();

        /*Signal signal1 = new Signal(0, LocalDate.now(), "Random comments", 5, 100);
        Report report1 = new Report(0, LocalDate.now(), new ArrayList<Report.Symptom>(), 5);
        report1.addSymptom(Report.Symptom.DIZZINESS);
        report1.addSymptom(Report.Symptom.HEADACHE);

        Signal signal2 = new Signal(0, LocalDate.of(2025, 11, 22), "Random comments", 5, 100);
        Report report2 = new Report(0, LocalDate.of(2025, 11, 22), new ArrayList<Report.Symptom>(), 5);
        report2.addSymptom(Report.Symptom.DIZZINESS);
        report2.addSymptom(Report.Symptom.NAUSEA);

        medicalManager.signalJDBC.insertSignal(signal1);
        medicalManager.reportJDBC.insertReport(report1);
        medicalManager.signalJDBC.insertSignal(signal2);
        medicalManager.reportJDBC.insertReport(report2);*/

        List<Patient> patients =  medicalManager.getPatientJDBC().getAllPatients();
        for (Patient patient : patients) {
            System.out.println(patient.toJason().toString());
            List<Signal> signals = medicalManager.getSignalJDBC().getSignalsByPatientId(patient.getId());
            List<Report> symptoms = medicalManager.getReportJDBC().getReportsByPatientId(patient.getId());
            JsonObject pJson = patient.toJason();
            JsonArray signalArray = new JsonArray();
            for (Signal s : signals) {
                signalArray.add(s.toJson());
            }
            JsonArray symptomsArray = new JsonArray();
            for (Report s : symptoms) {
                symptomsArray.add(s.toJson());
            }
            pJson.add("signals", signalArray);
            pJson.add("symptoms", symptomsArray);
            System.out.println(pJson.toString());
        }

        /*Patient patient = new Patient("Jane", "Doe", "jane.doe@example.com", "123456789", LocalDate.of(2004, 05, 11), "Female", 3);
        Role role = securityManager.getRoleJDBC().findRoleByName("Patient");
        User user = new User("jane.doe@example.com", "pasword123", role.getId(), true);
        medicalManager.patientJDBC.insertPatient(patient);
        securityManager.getUserJDBC().insertUser(user);*/

        /*List<Doctor> doctors =  medicalManager.getDoctorJDBC().getAllDoctors();
        for (Doctor doctor : doctors) {
            System.out.println(doctor.toString());
        }*/

        //Doctor doctor = medicalManager.doctorJDBC.getDoctor(3);
        //System.out.println(doctor);

        //Patient patient1 = medicalManager.getPatientJDBC().findPatientByEmail("jane.doe@example.com");
        //System.out.println(patient1);
        medicalManager.close();
    }

    private Connection connection;
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

    /**
     * Closes the connection to the {@code medicaldb} database if it is still open.
     */
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

