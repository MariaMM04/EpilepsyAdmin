package network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.JDBC.medicaldb.DoctorJDBC;
import org.example.JDBC.medicaldb.MedicalManager;
import org.example.JDBC.medicaldb.PatientJDBC;
import org.example.JDBC.medicaldb.SignalJDBC;
import org.example.JDBC.securitydb.RoleJDBC;
import org.example.JDBC.securitydb.SecurityManager;
import org.example.JDBC.securitydb.UserJDBC;
import org.example.entities_securitydb.*;
import org.example.entities_medicaldb.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ui.windows.Application;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ClientHandlerTest {
    private Server server;
    private Application app;
    private ServerSocket serverSocket;
    private InputStream in;
    private OutputStream out;
    private ClientHandler handler;

    @BeforeEach
    void setUp() throws IOException {
        app = mock(Application.class);
        serverSocket = mock(ServerSocket.class);
        server = new Server(serverSocket, app);
        in = mock(InputStream.class);
        out = mock(OutputStream.class);
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object getField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    @Test
    void testHandleStopClient() throws Exception {
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);
        when(socket.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));
        ClientHandler handler = new ClientHandler(socket, server);

        // replace real BufferedReader with a mock using reflection
        BufferedReader mockReader = mock(BufferedReader.class);
        BufferedWriter mockWriter = mock(BufferedWriter.class);

        // Inject mocks using reflection
        setField(handler, "in", mockReader);
        setField(handler, "out", mockWriter);

        when(mockReader.readLine()).thenReturn("Hi! I'm a new client!")
                .thenReturn("{\"type\":\"STOP_CLIENT\"}")
                .thenReturn(null);

        handler.run();

        verify(mockReader).close();
        verify(mockWriter).close();
        verify(socket).close();
        assertTrue(handler.isStopped());
    }

    /**
     * Check if it allows correct logIn of a Doctor in the Doctor App
     * @throws Exception
     */
    @Test
    void testHandleLoginCorrectDoctorRequest() throws Exception {
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);
        when(socket.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));
        ClientHandler handler = new ClientHandler(socket, server);
        SecurityManager mockSecurityManager = mock(SecurityManager.class);
        UserJDBC  mockUserJDBC = mock(UserJDBC.class);
        RoleJDBC mockRoleJDBC = mock(RoleJDBC.class);

        // replace real BufferedReader with a mock using reflection
        BufferedReader mockReader = mock(BufferedReader.class);
        BufferedWriter mockWriter = mock(BufferedWriter.class);

        // Inject mocks using reflection
        setField(handler, "in", mockReader);
        setField(handler, "out", mockWriter);
        setField(app, "securityManager", mockSecurityManager);
        setField(app, "userJDBC", mockUserJDBC);
        when(app.securityManager.getRoleJDBC()).thenReturn(mockRoleJDBC);

        // Mock BD
        when(app.userJDBC.isUser("doctor@mail.com")).thenReturn(true);
        when(app.userJDBC.login("doctor@mail.com", "123"))
                .thenReturn(new User(1, "doctor@mail.com", "123", true, 1));
        when(app.userJDBC.isUser("patient@mail.com")).thenReturn(true);
        when(app.securityManager.getRoleJDBC().findRoleByID(1)).thenReturn(new Role("Doctor"));

        when(mockReader.readLine())
                .thenReturn("{\"type\":\"LOGIN_REQUEST\",\"data\":{\"email\":\"doctor@mail.com\",\"password\":\"123\",\"access_permits\":\"Doctor\"}}")
                .thenReturn(null);

        handler.run();

        verify(mockWriter, atLeastOnce()).write(contains("SUCCESS"));
    }

    /**
     * It should not allow a Patient or Admin to LogIn in the Doctor App
     * @throws Exception
     */
    @Test
    void testHandleLoginIncorrectDoctorRequest() throws Exception {
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);
        when(socket.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));
        ClientHandler handler = new ClientHandler(socket, server);
        SecurityManager mockSecurityManager = mock(SecurityManager.class);
        UserJDBC  mockUserJDBC = mock(UserJDBC.class);
        RoleJDBC mockRoleJDBC = mock(RoleJDBC.class);

        // replace real BufferedReader with a mock using reflection
        BufferedReader mockReader = mock(BufferedReader.class);
        BufferedWriter mockWriter = mock(BufferedWriter.class);

        // Inject mocks using reflection
        setField(handler, "in", mockReader);
        setField(handler, "out", mockWriter);
        setField(app, "securityManager", mockSecurityManager);
        setField(app, "userJDBC", mockUserJDBC);
        when(app.securityManager.getRoleJDBC()).thenReturn(mockRoleJDBC);

        // Mock BD
        when(app.userJDBC.isUser("patient@mail.com")).thenReturn(true);
        when(app.userJDBC.login("patient@mail.com", "123"))
                .thenReturn(new User(2, "patien@mail.com", "123", true, 2));
        when(app.userJDBC.isUser("admin@mail.com")).thenReturn(true);
        when(app.userJDBC.login("admin@mail.com", "123"))
                .thenReturn(new User(2, "admin@mail.com", "123", true, 3));
        when(app.securityManager.getRoleJDBC().findRoleByID(1)).thenReturn(new Role("Doctor"));
        when(app.securityManager.getRoleJDBC().findRoleByID(2)).thenReturn(new Role("Patient"));
        when(app.securityManager.getRoleJDBC().findRoleByID(3)).thenReturn(new Role("Admin"));

        when(mockReader.readLine())
                .thenReturn("{\"type\":\"LOGIN_REQUEST\",\"data\":{\"email\":\"patient@mail.com\",\"password\":\"123\",\"access_permits\":\"Doctor\"}}")
                .thenReturn("{\"type\":\"LOGIN_REQUEST\",\"data\":{\"email\":\"admin@mail.com\",\"password\":\"123\",\"access_permits\":\"Doctor\"}}")
                .thenReturn(null);

        handler.run();
        verify(mockWriter, atLeast(2)).write(contains("ERROR"));
    }

    /**
     * Check if it allows correct logIn of a Patient on the Patient App
     * @throws Exception
     */
    @Test
    void testHandleLoginCorrectPatientRequest() throws Exception {
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);
        when(socket.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));
        ClientHandler handler = new ClientHandler(socket, server);
        SecurityManager mockSecurityManager = mock(SecurityManager.class);
        UserJDBC  mockUserJDBC = mock(UserJDBC.class);
        RoleJDBC mockRoleJDBC = mock(RoleJDBC.class);

        // replace real BufferedReader with a mock using reflection
        BufferedReader mockReader = mock(BufferedReader.class);
        BufferedWriter mockWriter = mock(BufferedWriter.class);

        // Inject mocks using reflection
        setField(handler, "in", mockReader);
        setField(handler, "out", mockWriter);
        setField(app, "securityManager", mockSecurityManager);
        setField(app, "userJDBC", mockUserJDBC);
        when(app.securityManager.getRoleJDBC()).thenReturn(mockRoleJDBC);

        // Mock BD
        // Mock BD
        when(app.userJDBC.isUser("patient@mail.com")).thenReturn(true);
        when(app.userJDBC.login("patient@mail.com", "123"))
                .thenReturn(new User(2, "patien@mail.com", "123", true, 2));
        when(app.securityManager.getRoleJDBC().findRoleByID(1)).thenReturn(new Role("Doctor"));
        when(app.securityManager.getRoleJDBC().findRoleByID(2)).thenReturn(new Role("Patient"));

        when(mockReader.readLine())
                .thenReturn("{\"type\":\"LOGIN_REQUEST\",\"data\":{\"email\":\"patient@mail.com\",\"password\":\"123\",\"access_permits\":\"Patient\"}}")
                .thenReturn(null);

        handler.run();
        verify(mockWriter).write(contains("SUCCESS"));
    }

    /**
     * It should not allow a Doctor or Admin to login on the Patient App
     * @throws Exception
     */
    @Test
    void testHandleLoginIncorrectPatientRequest() throws Exception {
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);
        when(socket.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));
        ClientHandler handler = new ClientHandler(socket, server);
        SecurityManager mockSecurityManager = mock(SecurityManager.class);
        UserJDBC  mockUserJDBC = mock(UserJDBC.class);
        RoleJDBC mockRoleJDBC = mock(RoleJDBC.class);

        // replace real BufferedReader with a mock using reflection
        BufferedReader mockReader = mock(BufferedReader.class);
        BufferedWriter mockWriter = mock(BufferedWriter.class);

        // Inject mocks using reflection
        setField(handler, "in", mockReader);
        setField(handler, "out", mockWriter);
        setField(app, "securityManager", mockSecurityManager);
        setField(app, "userJDBC", mockUserJDBC);
        when(app.securityManager.getRoleJDBC()).thenReturn(mockRoleJDBC);

        // Mock BD
        when(app.userJDBC.isUser("doctor@mail.com")).thenReturn(true);
        when(app.userJDBC.login("doctor@mail.com", "123"))
                .thenReturn(new User(2, "doctor@mail.com", "123", true, 1));
        when(app.userJDBC.isUser("admin@mail.com")).thenReturn(true);
        when(app.userJDBC.login("admin@mail.com", "123"))
                .thenReturn(new User(2, "admin@mail.com", "123", true, 3));
        when(app.securityManager.getRoleJDBC().findRoleByID(1)).thenReturn(new Role("Doctor"));
        when(app.securityManager.getRoleJDBC().findRoleByID(2)).thenReturn(new Role("Patient"));
        when(app.securityManager.getRoleJDBC().findRoleByID(3)).thenReturn(new Role("Admin"));

        when(mockReader.readLine())
                .thenReturn("{\"type\":\"LOGIN_REQUEST\",\"data\":{\"email\":\"doctor@mail.com\",\"password\":\"123\",\"access_permits\":\"Patient\"}}")
                .thenReturn("{\"type\":\"LOGIN_REQUEST\",\"data\":{\"email\":\"admin@mail.com\",\"password\":\"123\",\"access_permits\":\"Patient\"}}")
                .thenReturn(null);

        handler.run();
        verify(mockWriter, atLeast(2)).write(contains("ERROR"));
    }

    /**
     * It should send an error if the user does not exist
     * @throws Exception
     */
    @Test
    void testHandleLoginInUserNotFound() throws Exception {
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);
        when(socket.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));
        ClientHandler handler = new ClientHandler(socket, server);
        SecurityManager mockSecurityManager = mock(SecurityManager.class);
        UserJDBC  mockUserJDBC = mock(UserJDBC.class);
        RoleJDBC mockRoleJDBC = mock(RoleJDBC.class);

        // replace real BufferedReader with a mock using reflection
        BufferedReader mockReader = mock(BufferedReader.class);
        BufferedWriter mockWriter = mock(BufferedWriter.class);

        // Inject mocks using reflection
        setField(handler, "in", mockReader);
        setField(handler, "out", mockWriter);
        setField(app, "securityManager", mockSecurityManager);
        setField(app, "userJDBC", mockUserJDBC);
        when(app.securityManager.getRoleJDBC()).thenReturn(mockRoleJDBC);

        // Mock BD
        when(app.userJDBC.isUser("random@mail.com")).thenReturn(false);

        when(mockReader.readLine())
                .thenReturn("{\"type\":\"LOGIN_REQUEST\",\"data\":{\"email\":\"random@mail.com\",\"password\":\"123\",\"access_permits\":\"Patient\"}}")
                .thenReturn(null);

        handler.run();
        verify(mockWriter, atLeastOnce()).write(contains("ERROR"));
    }

    @Test
    void testHandleRequestPatientByEmail() throws Exception {
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);
        when(socket.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));
        ClientHandler handler = new ClientHandler(socket, server);
        SecurityManager mockSecurityManager = mock(SecurityManager.class);
        MedicalManager   mockMedicalManager = mock(MedicalManager.class);
        UserJDBC  mockUserJDBC = mock(UserJDBC.class);
        RoleJDBC mockRoleJDBC = mock(RoleJDBC.class);
        PatientJDBC mockPatientJDBC = mock(PatientJDBC.class);

        // replace real BufferedReader with a mock using reflection
        BufferedReader mockReader = mock(BufferedReader.class);
        BufferedWriter mockWriter = mock(BufferedWriter.class);

        // Inject mocks using reflection
        setField(handler, "in", mockReader);
        setField(handler, "out", mockWriter);
        setField(app, "securityManager", mockSecurityManager);
        setField(app, "userJDBC", mockUserJDBC);
        setField(app, "medicalManager", mockMedicalManager);
        setField(app, "patientJDBC", mockPatientJDBC);
        when(app.securityManager.getRoleJDBC()).thenReturn(mockRoleJDBC);

        User user = new User(1, "pat@mail.com", "123", true, 1);
        when(app.userJDBC.findUserByID(1)).thenReturn(user);

        Patient patient = new Patient("A", "B", "pat@mail.com", "61235678", LocalDate.now(), "Female", 2);
        when(app.patientJDBC.findPatientByEmail("pat@mail.com")).thenReturn(patient);
        when(app.securityManager.getRoleJDBC().findRoleByID(1)).thenReturn(new Role(1,"Patient"));

        when(mockReader.readLine())
                .thenReturn("{\"type\":\"REQUEST_PATIENT_BY_EMAIL\",\"data\":{\"email\":\"pat@mail.com\",\"user_id\":1}}")
                .thenReturn(null);

        handler.run();

        //Capture response
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockWriter).write(captor.capture());
        String written = captor.getValue();
        System.out.println("Captured output: " + written);

        verify(mockWriter).write(contains("SUCCESS"));
    }

    /**
     * If it receives a REQUEST_DOCTOR_BY_EMAIL request, it first checks whether the user asking for the info has access permits.
     * - If yes, it sends a REQUES_DOCTOR_BY_EMAIL_RESPONSE with the doctor info
     * - If the user asking is a PATIENT or ADMIN, then it sends an error
     * @throws Exception
     */
    @Test
    void testHandleRequestDoctorByEmail() throws Exception {
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);
        when(socket.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));
        ClientHandler handler = new ClientHandler(socket, server);
        SecurityManager mockSecurityManager = mock(SecurityManager.class);
        MedicalManager   mockMedicalManager = mock(MedicalManager.class);
        UserJDBC  mockUserJDBC = mock(UserJDBC.class);
        RoleJDBC mockRoleJDBC = mock(RoleJDBC.class);
        DoctorJDBC mockDoctorJDBC = mock(DoctorJDBC.class);

        // replace real BufferedReader with a mock using reflection
        BufferedReader mockReader = mock(BufferedReader.class);
        BufferedWriter mockWriter = mock(BufferedWriter.class);

        // Inject mocks using reflection
        setField(handler, "in", mockReader);
        setField(handler, "out", mockWriter);
        setField(app, "securityManager", mockSecurityManager);
        setField(app, "userJDBC", mockUserJDBC);
        setField(app, "medicalManager", mockMedicalManager);
        setField(app, "doctorJDBC", mockDoctorJDBC);
        when(app.securityManager.getRoleJDBC()).thenReturn(mockRoleJDBC);

        User user = new User(1, "doc@mail.com", "123", true, 1);
        User incUser = new User(2, "pat@mail.com", "123", true, 2);
        when(app.userJDBC.findUserByID(1)).thenReturn(user);
        when(app.userJDBC.findUserByID(2)).thenReturn(incUser);

        Doctor doctor = new Doctor("A", "B", "612345678", "doc@mail.com", "Neurology", "Neuroscience");
        when(app.doctorJDBC.findDoctorByEmail("doc@mail.com")).thenReturn(doctor);
        when(app.securityManager.getRoleJDBC().findRoleByID(1)).thenReturn(new Role(1,"Doctor"));
        when(app.securityManager.getRoleJDBC().findRoleByID(2)).thenReturn(new Role(2,"Patient"));

        when(mockReader.readLine())
                .thenReturn("{\"type\":\"REQUEST_DOCTOR_BY_EMAIL\",\"data\":{\"email\":\"doc@mail.com\",\"user_id\":1}}").
                thenReturn("{\"type\":\"REQUEST_DOCTOR_BY_EMAIL\",\"data\":{\"email\":\"pat@mail.com\",\"user_id\":2}}")
                .thenReturn(null);

        handler.run();

        //Capture response
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockWriter, atLeast(2)).write(captor.capture());
        for(String written : captor.getAllValues()) {
            System.out.println("Captured output: " + written);
        }

        verify(mockWriter).write(contains("SUCCESS"));
        verify(mockWriter).write(contains("ERROR"));
    }

    /**
     * If it receives a REQUEST_DOCTOR_BY_EMAIL request, it first checks whether the user asking for the info has access permits.
     * - If yes, it sends a REQUES_DOCTOR_BY_EMAIL_RESPONSE with the doctor info
     * - If the user asking is a PATIENT or ADMIN, then it sends an error
     * @throws Exception
     */
    @Test
    void testHandleRequestDoctorByID() throws Exception {
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);
        when(socket.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));
        ClientHandler handler = new ClientHandler(socket, server);
        SecurityManager mockSecurityManager = mock(SecurityManager.class);
        MedicalManager mockMedicalManager = mock(MedicalManager.class);
        PatientJDBC mockPatientJDBC = mock(PatientJDBC.class);
        UserJDBC  mockUserJDBC = mock(UserJDBC.class);
        RoleJDBC mockRoleJDBC = mock(RoleJDBC.class);
        DoctorJDBC mockDoctorJDBC = mock(DoctorJDBC.class);

        // replace real BufferedReader with a mock using reflection
        BufferedReader mockReader = mock(BufferedReader.class);
        BufferedWriter mockWriter = mock(BufferedWriter.class);

        // Inject mocks using reflection
        setField(handler, "in", mockReader);
        setField(handler, "out", mockWriter);
        setField(app, "securityManager", mockSecurityManager);
        setField(app, "userJDBC", mockUserJDBC);
        setField(app, "medicalManager", mockMedicalManager);
        setField(app, "doctorJDBC", mockDoctorJDBC);
        setField(app, "patientJDBC", mockPatientJDBC);
        when(app.securityManager.getRoleJDBC()).thenReturn(mockRoleJDBC);

        User user = new User(1, "doc@mail.com", "123", true, 1);
        User userdoc2 = new User(3, "doc2@mail.com", "123", true, 1);
        User correctPatUser = new User(2, "pat@mail.com", "123", true, 2);
        User incorrectPatUser = new User(4, "pat2@mail.com", "123", true, 2);
        when(app.userJDBC.findUserByID(1)).thenReturn(user);
        when(app.userJDBC.findUserByID(2)).thenReturn(correctPatUser);
        when(app.userJDBC.findUserByID(3)).thenReturn(userdoc2);
        when(app.userJDBC.findUserByID(4)).thenReturn(incorrectPatUser);


        Patient patient = new Patient("A", "B", "pat@mail.com", "61235678", LocalDate.now(), "Female", 1);
        patient.setId(1);
        when(app.patientJDBC.findPatientByEmail("pat@mail.com")).thenReturn(patient);
        Patient incPatient = new Patient("A", "B", "pat2@mail.com", "61235678", LocalDate.now(), "Female", 2);
        incPatient.setId(2);
        when(app.patientJDBC.findPatientByEmail("pat2@mail.com")).thenReturn(incPatient);
        Doctor doctor = new Doctor("A", "B", "612345678", "doc@mail.com", "Neurology", "Neuroscience");
        doctor.setId(1);
        Doctor doctor2 = new Doctor("A", "B", "612345678", "doc2@mail.com", "Neurology", "Neuroscience");
        doctor2.setId(2);
        when(app.doctorJDBC.getDoctor(1)).thenReturn(doctor);
        when(app.doctorJDBC.getDoctor(2)).thenReturn(doctor2);
        when(app.securityManager.getRoleJDBC().findRoleByID(1)).thenReturn(new Role(1,"Doctor"));
        when(app.securityManager.getRoleJDBC().findRoleByID(2)).thenReturn(new Role(2,"Patient"));

        when(mockReader.readLine())
                .thenReturn("{\"type\":\"REQUEST_DOCTOR_BY_ID\",\"data\":{\"doctor_id\":1,\"user_id\":1}}")
                .thenReturn("{\"type\":\"REQUEST_DOCTOR_BY_ID\",\"data\":{\"user_id\":2,\"doctor_id\":1}}")
                .thenReturn("{\"type\":\"REQUEST_DOCTOR_BY_ID\",\"data\":{\"user_id\":3,\"doctor_id\":1}}")
                .thenReturn("{\"type\":\"REQUEST_DOCTOR_BY_ID\",\"data\":{\"user_id\":4,\"doctor_id\":1}}")
                .thenReturn(null);

        handler.run();

        //Capture response
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockWriter, atLeast(3)).write(captor.capture());
        for(String written : captor.getAllValues()) {
            System.out.println("Captured output: " + written);
        }

        verify(mockWriter, atLeast(2)).write(contains("SUCCESS"));
        verify(mockWriter, atLeast(2)).write(contains("ERROR"));

    }

    @Test
    void testHandleRequestPatientsFromDoctor() throws Exception {
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);
        when(socket.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));

        ClientHandler handler = new ClientHandler(socket, server);

        //Mocks
        SecurityManager mockSecurityManager = mock(SecurityManager.class);
        MedicalManager mockMedicalManager = mock(MedicalManager.class);
        UserJDBC mockUserJDBC = mock(UserJDBC.class);
        DoctorJDBC mockDoctorJDBC = mock(DoctorJDBC.class);
        PatientJDBC mockPatientJDBC = mock(PatientJDBC.class);
        RoleJDBC mockRoleJDBC = mock(RoleJDBC.class);

        //Inject mocks
        BufferedReader mockReader = mock(BufferedReader.class);
        BufferedWriter mockWriter = mock(BufferedWriter.class);

        setField(handler, "in", mockReader);
        setField(handler, "out", mockWriter);
        setField(app, "securityManager", mockSecurityManager);
        setField(app, "medicalManager", mockMedicalManager);
        setField(app, "userJDBC", mockUserJDBC);
        setField(app, "doctorJDBC", mockDoctorJDBC);

        when(app.securityManager.getRoleJDBC()).thenReturn(mockRoleJDBC);
        when(mockMedicalManager.getPatientJDBC()).thenReturn(mockPatientJDBC);

        //Users
        User doctorUser = new User(1, "doc@mail.com", "pass", true, 1);
        User wrongUserPat  = new User(2, "other@mail.com", "pass", true, 2);
        User wrongUserDoc = new User(3, "doc2@mail.com", "pass", true, 1);

        when(mockUserJDBC.findUserByID(1)).thenReturn(doctorUser);
        when(mockUserJDBC.findUserByID(2)).thenReturn(wrongUserPat);
        when(mockUserJDBC.findUserByID(3)).thenReturn(wrongUserDoc);
        when(mockRoleJDBC.findRoleByID(1)).thenReturn(new Role(1, "Doctor"));
        when(mockRoleJDBC.findRoleByID(2)).thenReturn(new Role(2, "Patient"));

        Doctor doctor = new Doctor("A","B","6123","doc@mail.com","Cardiology","Card");
        doctor.setId(10);
        when(mockDoctorJDBC.getDoctor(10)).thenReturn(doctor);

        Doctor wrongDoctor = new Doctor("A","B","6123","doc2@mail.com","Cardiology","Card");
        wrongDoctor.setId(11);
        when(mockDoctorJDBC.getDoctor(11)).thenReturn(wrongDoctor);

        //Patients returned
        Patient p1 = new Patient("Toni","Blue","t@c.com","999",LocalDate.now(),"M",1);
        Patient p2 = new Patient("Rosa","Green","r@g.com","888",LocalDate.now(),"F",1);
        when(mockPatientJDBC.getPatientsOfDoctor(10)).thenReturn(List.of(p1,p2));

        when(mockReader.readLine())
                // authorized doctor
                .thenReturn("{\"type\":\"REQUEST_PATIENTS_FROM_DOCTOR\",\"data\":{\"doctor_id\":10,\"user_id\":1}}")
                // unauthorized user
                .thenReturn("{\"type\":\"REQUEST_PATIENTS_FROM_DOCTOR\",\"data\":{\"doctor_id\":10,\"user_id\":2}}")
                .thenReturn("{\"type\":\"REQUEST_PATIENTS_FROM_DOCTOR\",\"data\":{\"doctor_id\":10,\"user_id\":3}}")
                .thenReturn(null);

        handler.run();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockWriter, atLeast(2)).write(captor.capture());

        // Check that one SUCCESS and one ERROR occurred
        verify(mockWriter).write(contains("SUCCESS"));
        verify(mockWriter, atLeast(2)).write(contains("ERROR"));
    }

    @Test
    void testHandleSaveCommentsSignal() throws Exception {
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);
        when(socket.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));

        ClientHandler handler = new ClientHandler(socket, server);

        //Mocks
        SecurityManager mockSecurityManager = mock(SecurityManager.class);
        MedicalManager mockMedicalManager = mock(MedicalManager.class);
        UserJDBC mockUserJDBC = mock(UserJDBC.class);
        DoctorJDBC mockDoctorJDBC = mock(DoctorJDBC.class);
        PatientJDBC mockPatientJDBC = mock(PatientJDBC.class);
        SignalJDBC mockSignalJDBC = mock(SignalJDBC.class);

        RoleJDBC mockRoleJDBC = mock(RoleJDBC.class);

        BufferedReader mockReader = mock(BufferedReader.class);
        BufferedWriter mockWriter = mock(BufferedWriter.class);

        setField(handler, "in", mockReader);
        setField(handler, "out", mockWriter);

        setField(app, "securityManager", mockSecurityManager);
        setField(app, "medicalManager", mockMedicalManager);
        setField(app, "userJDBC", mockUserJDBC);
        setField(app, "doctorJDBC", mockDoctorJDBC);
        setField(app, "patientJDBC", mockPatientJDBC);

        when(mockMedicalManager.getSignalJDBC()).thenReturn(mockSignalJDBC);
        when(mockMedicalManager.getPatientJDBC()).thenReturn(mockPatientJDBC);

        when(mockSecurityManager.getRoleJDBC()).thenReturn(mockRoleJDBC);
        when(mockRoleJDBC.findRoleByID(1)).thenReturn(new Role(1, "Doctor"));

        //User is a doctor
        User doctorUser = new User(1, "doc@mail.com","123",true,1);
        when(mockUserJDBC.findUserByID(1)).thenReturn(doctorUser);

        //Patient belongs to doctor
        Doctor doctor = new Doctor("A","B","999","doc@mail.com","Cardio","Cardio");
        when(mockDoctorJDBC.findDoctorByEmail("doc@mail.com")).thenReturn(doctor);
        when(mockDoctorJDBC.getDoctorFromPatient(5)).thenReturn(doctor);

        when(mockSignalJDBC.updateSignalComments(7,"hello world")).thenReturn(true);

        when(mockReader.readLine())
                .thenReturn("{\"type\":\"SAVE_COMMENTS_SIGNAL\",\"data\":{\"comments\":\"hello world\",\"signal_id\":7,\"patient_id\":5,\"user_id\":1}}")
                .thenReturn(null);

        handler.run();

        verify(mockWriter).write(contains("SUCCESS"));
    }

    @Test
    void testForceShutdownClosesSocket() throws Exception {
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);
        when(socket.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));

        ClientHandler handler = new ClientHandler(socket, server);
        handler.run();
        handler.forceShutdown();

        verify(socket).close();
    }


    @Test
    void testReleaseResources() throws Exception {
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);
        when(socket.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));

        ClientHandler handler = new ClientHandler(socket, server);

        BufferedReader mockReader = mock(BufferedReader.class);
        BufferedWriter mockWriter = mock(BufferedWriter.class);

        setField(handler, "in", mockReader);
        setField(handler, "out", mockWriter);

        handler.run();

        Method release = ClientHandler.class.getDeclaredMethod(
                "releaseResources", BufferedReader.class, BufferedWriter.class, Socket.class);
        release.setAccessible(true);

        release.invoke(handler, mockReader, mockWriter, socket);

        verify(mockReader).close();
        verify(mockWriter).close();
        verify(socket).close();
        assertTrue(handler.isStopped());
        assertEquals(0, server.getConnectedClients().size());
    }
    @Test
    void testHandleRequestSignalPatient() throws Exception {
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);
        when(socket.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));
        ClientHandler handler = new ClientHandler(socket, server);
        //Mocks
        SecurityManager mockSecurityManager = mock(SecurityManager.class);
        MedicalManager mockMedicalManager = mock(MedicalManager.class);
        UserJDBC mockUserJDBC = mock(UserJDBC.class);
        DoctorJDBC mockDoctorJDBC = mock(DoctorJDBC.class);
        PatientJDBC mockPatientJDBC = mock(PatientJDBC.class);
        RoleJDBC mockRoleJDBC = mock(RoleJDBC.class);
        SignalJDBC mockSignalJDBC = mock(SignalJDBC.class);

        //Inject mocks
        BufferedReader mockReader = mock(BufferedReader.class);
        BufferedWriter mockWriter = mock(BufferedWriter.class);

        setField(handler, "in", mockReader);
        setField(handler, "out", mockWriter);
        setField(app, "securityManager", mockSecurityManager);
        setField(app, "medicalManager", mockMedicalManager);
        setField(app, "userJDBC", mockUserJDBC);
        setField(app, "doctorJDBC", mockDoctorJDBC);

        when(mockMedicalManager.getPatientJDBC()).thenReturn(mockPatientJDBC);
        when(mockMedicalManager.getSignalJDBC()).thenReturn(mockSignalJDBC);
        Patient fakePatient = new Patient("Test", "Patient", "email@x.com", "12", LocalDate.now(), "F", 12);
        when(mockPatientJDBC.findPatientByID(12)).thenReturn(fakePatient);


        File tempZip = File.createTempFile("signal_test_", ".zip");
        Files.writeString(tempZip.toPath(), "SIGNAL_TEST_DATA");

        String base64Zip = Base64.getEncoder().encodeToString(Files.readAllBytes(tempZip.toPath()));

        String jsonRequest = """
                {
                  "type": "UPLOAD_SIGNAL",
                  "metadata": {
                    "patient_id": 12,
                    "sampling_rate": 500,
                    "timestamp": "2025-02-12T18:32:11" //todo cambiar clases signals para que si cojan el tiempo
                  },
                  "compression": "zip-base64",
                  "filename": "signal_test.zip",
                  "datafile": "%s"
                }
                """.formatted(base64Zip);
        when(mockReader.readLine())
                .thenReturn(jsonRequest)
                .thenReturn(null);

        doNothing().when(mockSignalJDBC).insertSignal(any());
        handler.run();
        verify(mockWriter).write(contains("SUCCESS"));
    }
    @Test
    void handleRequestSignal() throws Exception {
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);
        when(socket.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));
        ClientHandler handler = new ClientHandler(socket, server);
        //Mocks
        SecurityManager mockSecurityManager = mock(SecurityManager.class);
        MedicalManager mockMedicalManager = mock(MedicalManager.class);
        UserJDBC mockUserJDBC = mock(UserJDBC.class);
        DoctorJDBC mockDoctorJDBC = mock(DoctorJDBC.class);
        PatientJDBC mockPatientJDBC = mock(PatientJDBC.class);
        RoleJDBC mockRoleJDBC = mock(RoleJDBC.class);
        SignalJDBC mockSignalJDBC = mock(SignalJDBC.class);
        //Inject mocks
        BufferedReader mockReader = mock(BufferedReader.class);
        BufferedWriter mockWriter = mock(BufferedWriter.class);

        setField(handler, "in", mockReader);
        setField(handler, "out", mockWriter);
        setField(app, "securityManager", mockSecurityManager);
        setField(app, "medicalManager", mockMedicalManager);
        setField(app, "userJDBC", mockUserJDBC);
        setField(app, "doctorJDBC", mockDoctorJDBC);
        when(mockSecurityManager.getRoleJDBC()).thenReturn(mockRoleJDBC);
        when(mockMedicalManager.getPatientJDBC()).thenReturn(mockPatientJDBC);
        when(mockMedicalManager.getSignalJDBC()).thenReturn(mockSignalJDBC);
        User doctorUser = new User(1, "doc@mail.com","123",true,1);
        when(mockUserJDBC.findUserByID(1)).thenReturn(doctorUser);
        when(mockRoleJDBC.findRoleByID(1)).thenReturn(new Role(1, "Doctor"));

        File zip = File.createTempFile("signal88_", ".zip");
        Files.writeString(zip.toPath(), "FAKE_ZIP_CONTENT");

        Signal fakeSignal = new Signal(zip, LocalDate.now(), "Hello world", 15, 1000);
        fakeSignal.setId(88);

        when(mockSignalJDBC.findSignalById(88)).thenReturn(fakeSignal);
        when(mockUserJDBC.findUserByID(doctorUser.getId())).thenReturn(doctorUser);
        String jsonRequest = """
    {
       "type": "REQUEST_SIGNAL",
       "data": {
         "signal_id": 88,
         "user_id": 1
       }
    }
    """;
        when(mockReader.readLine()).thenReturn(jsonRequest).thenReturn(null);
        handler.run();
        verify(mockWriter).write(contains("SUCCESS"));
        verify(mockWriter).write(contains("zip-base64"));
        verify(mockWriter).write(contains("signal_88"));
    }
    @Test
    void handleRequestPatientSignals() throws IOException {
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);
        when(socket.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));
        ClientHandler handler = new ClientHandler(socket, server);
        //Mocks
        SecurityManager mockSecurityManager = mock(SecurityManager.class);
        MedicalManager mockMedicalManager = mock(MedicalManager.class);
        UserJDBC mockUserJDBC = mock(UserJDBC.class);
        DoctorJDBC mockDoctorJDBC = mock(DoctorJDBC.class);
        PatientJDBC mockPatientJDBC = mock(PatientJDBC.class);
        RoleJDBC mockRoleJDBC = mock(RoleJDBC.class);
        SignalJDBC mockSignalJDBC = mock(SignalJDBC.class);
        //Inject mocks
        BufferedReader mockReader = mock(BufferedReader.class);
        BufferedWriter mockWriter = mock(BufferedWriter.class);

        setField(handler, "in", mockReader);
        setField(handler, "out", mockWriter);
        setField(app, "securityManager", mockSecurityManager);
        setField(app, "medicalManager", mockMedicalManager);
        setField(app, "userJDBC", mockUserJDBC);
        setField(app, "doctorJDBC", mockDoctorJDBC);

        when(mockSecurityManager.getRoleJDBC()).thenReturn(mockRoleJDBC);
        when(mockMedicalManager.getPatientJDBC()).thenReturn(mockPatientJDBC);
        when(mockMedicalManager.getSignalJDBC()).thenReturn(mockSignalJDBC);
        User doctorUser = new User(1, "doc@mail.com","123",true,1);
        when(mockUserJDBC.findUserByID(1)).thenReturn(doctorUser);
        when(mockRoleJDBC.findRoleByID(1)).thenReturn(new Role(1, "Doctor"));
        when(mockUserJDBC.findUserByID(doctorUser.getId())).thenReturn(doctorUser);
        when(mockSignalJDBC.getSignalsByPatientId(88)).thenReturn(List.of(
                new Signal(1, LocalDate.now(), "Sig 1", 10, 500),
                new Signal(2, LocalDate.now(), "Sig 2", 15, 1000),
                new Signal(3, LocalDate.now(), "Sig 3", 20, 2000)
        ));
        String jsonRequest = """
    {
        "type": "REQUEST_PATIENT_SIGNALS",
        "data": {
            "patient_id": 88,
            "user_id": 1
        }
    }
    """;
        when(mockReader.readLine())
                .thenReturn(jsonRequest)
                .thenReturn(null);
        handler.run();
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockWriter, atLeastOnce()).write(captor.capture());

        String response = String.join("", captor.getAllValues());
        System.out.println("SERVER RESPONSE = " + response);

        // ---- ASSERTIONS ----
        assertTrue(response.contains("SUCCESS"));
        assertTrue(response.contains("signals"));
        assertTrue(response.contains("\"signal_id\":1"));
        assertTrue(response.contains("\"signal_id\":2"));
        assertTrue(response.contains("\"signal_id\":3"));
        assertTrue(response.contains("REQUEST_PATIENT_SIGNALS_RESPONSE"));
    }
}
