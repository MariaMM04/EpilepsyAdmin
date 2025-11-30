package network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import encryption.RSAUtil;
import javax.crypto.SecretKey;
import encryption.*;
import org.example.JDBC.medicaldb.*;
import org.example.JDBC.securitydb.RoleJDBC;
import org.example.JDBC.securitydb.SecurityManager;
import org.example.JDBC.securitydb.UserJDBC;
import org.example.entities_securitydb.*;
import org.example.entities_medicaldb.*;
import org.example.service.AdminLinkService;
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
import java.security.KeyPair;
import java.security.PublicKey;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ClientHandlerTest {
    private Server server;
    private ServerSocket serverSocket;
    private InputStream in;
    private OutputStream out;
    private ClientHandler handler;
    private KeyPair keyPair;
    private AdminLinkService adminLinkService;
    private SecretKey aes;
    private Application app;

    @BeforeEach
    void setUp() throws Exception {
        serverSocket = mock(ServerSocket.class);
        adminLinkService = mock(AdminLinkService.class);
        server = new Server(serverSocket, adminLinkService);
        in = mock(InputStream.class);
        out = mock(OutputStream.class);
        keyPair = mock(KeyPair.class);
        handler = mock(ClientHandler.class);
        aes = TokenUtils.generateToken();
        setField(handler, "token", aes);

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
    void testHandleStopClient2() throws Exception {

        // ----------- 1) Mock socket + server -----------
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);
        when(socket.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));

        Server server = mock(Server.class);

        KeyPair serverKeyPair = RSAKeyManager.generateKeyPair();
        ClientHandler handler = new ClientHandler(socket, server, serverKeyPair);

        // ----------- 2) Inject mocked reader/writer -----------
        BufferedReader mockReader = mock(BufferedReader.class);
        PrintWriter mockWriter = mock(PrintWriter.class);

        setField(handler, "in", mockReader);
        setField(handler, "out", mockWriter);

        // ----------- 3) Simulate RSA handshake -----------
        PublicKey clientPub = RSAKeyManager.generateKeyPair().getPublic();
        setField(handler, "clientPublicKey", clientPub); // shortcut: skip parsing public key JSON

        SecretKey aesKey = TokenUtils.generateToken();
        setField(handler, "token", aesKey);               // shortcut: skip CLIENT_AES_KEY parsing

        // ----------- 4) STOP_CLIENT encrypted payload -----------
        JsonObject stop = new JsonObject();
        stop.addProperty("type", "STOP_CLIENT");
        String stopJson = stop.toString();
        String encryptedStop = TokenUtils.encrypt(stopJson, aesKey);

        JsonObject encryptedMsg = new JsonObject();
        encryptedMsg.addProperty("type", "ENCRYPTED");
        encryptedMsg.addProperty("data", encryptedStop);

        // ----------- 5) Mock messages arriving from client -----------
        when(mockReader.readLine())
                .thenReturn(encryptedMsg.toString())  // encrypted STOP_CLIENT
                .thenReturn(null);                    // end-of-stream

        // ----------- 6) Execute handler -----------
        handler.run();

        // ----------- 7) Validate close behavior -----------
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

        // --- Arrange mock socket ---
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);
        when(socket.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));

        ClientHandler handler = new ClientHandler(socket, server, keyPair);

        // --- Replace streams ---
        BufferedReader mockReader = mock(BufferedReader.class);
        PrintWriter mockWriter = mock(PrintWriter.class);

        setField(handler, "in", mockReader);
        setField(handler, "out", mockWriter);

        // --- AES key ---
        SecretKey aes = TokenUtils.generateToken();
        setField(handler, "token", aes);

        // --- FULL CORRECT CHAIN OF SERVER DEPENDENCIES ---
        AdminLinkService mockALS = mock(AdminLinkService.class);
        SecurityManager mockSec = mock(SecurityManager.class);
        UserJDBC mockUserJDBC = mock(UserJDBC.class);
        RoleJDBC mockRole = mock(RoleJDBC.class);

        when(mockALS.getSecurityManager()).thenReturn(mockSec);
        when(mockSec.getUserJDBC()).thenReturn(mockUserJDBC);
        when(mockSec.getRoleJDBC()).thenReturn(mockRole);

        setField(server, "adminConn", mockALS);

        // --- Mock DB behavior ---
        when(mockUserJDBC.isUser("doctor@mail.com")).thenReturn(true);
        when(mockUserJDBC.login("doctor@mail.com", "123"))
                .thenReturn(new User(1,"doctor@mail.com","123",true,1));
        when(mockRole.findRoleByID(1)).thenReturn(new Role("Doctor"));

        // --- Correct JSON and encryption ---
        String plain =
                "{\"type\":\"LOGIN_REQUEST\",\"data\":"
                        + "{\"email\":\"doctor@mail.com\",\"password\":\"123\",\"access_permits\":\"Doctor\"}}";

        String enc = TokenUtils.encrypt(plain, aes);

        JsonObject wrapper = new JsonObject();
        wrapper.addProperty("type", "ENCRYPTED");
        wrapper.addProperty("data", enc);

        when(mockReader.readLine()).thenReturn(wrapper.toString()).thenReturn(null);

        // --- Act ---
        handler.run();

        //Assert
        ArgumentCaptor<JsonObject> captor = ArgumentCaptor.forClass(JsonObject.class);

        verify(mockWriter, atLeastOnce()).println(captor.capture());

        JsonObject outJson = captor.getValue();
        assertTrue(outJson.toString().contains("ENCRYPTED"));

        // Extract encrypted field
        String encryptedPayload = outJson.get("data").getAsString();

        // --- Decrypt using the AES key injected earlier ---
        String decryptedJson = TokenUtils.decrypt(encryptedPayload, aes);

        // --- Parse decrypted payload ---
        JsonObject decryptedObj = JsonParser.parseString(decryptedJson).getAsJsonObject();

        // ---- REAL ASSERTIONS ----
        assertEquals("LOGIN_RESPONSE", decryptedObj.get("type").getAsString());
        assertEquals("SUCCESS", decryptedObj.get("status").getAsString());
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

        Server server = mock(Server.class);
        KeyPair keyPair = RSAKeyManager.generateKeyPair();
        ClientHandler handler = new ClientHandler(socket, server, keyPair);

        BufferedReader mockReader = mock(BufferedReader.class);
        PrintWriter mockWriter = mock(PrintWriter.class);
        setField(handler, "in", mockReader);
        setField(handler, "out", mockWriter);

        SecretKey aes = TokenUtils.generateToken();
        setField(handler, "token", aes);

        AdminLinkService mockALS = mock(AdminLinkService.class);
        SecurityManager mockSec = mock(SecurityManager.class);
        UserJDBC mockUserJDBC = mock(UserJDBC.class);
        RoleJDBC mockRole = mock(RoleJDBC.class);

        when(server.getAdminLinkService()).thenReturn(mockALS);
        when(mockALS.getSecurityManager()).thenReturn(mockSec);
        when(mockSec.getUserJDBC()).thenReturn(mockUserJDBC);
        when(mockSec.getRoleJDBC()).thenReturn(mockRole);

        // Users: patient + admin trying to log in as Doctor
        when(mockUserJDBC.isUser("patient@mail.com")).thenReturn(true);
        when(mockUserJDBC.login("patient@mail.com", "123"))
                .thenReturn(new User(2,"patient@mail.com","123",true,2));

        when(mockUserJDBC.isUser("admin@mail.com")).thenReturn(true);
        when(mockUserJDBC.login("admin@mail.com", "123"))
                .thenReturn(new User(3,"admin@mail.com","123",true,3));

        when(mockRole.findRoleByID(1)).thenReturn(new Role("Doctor"));
        when(mockRole.findRoleByID(2)).thenReturn(new Role("Patient"));
        when(mockRole.findRoleByID(3)).thenReturn(new Role("Admin"));

        // Two separate login requests, both ask access_permits "Doctor"
        String plain1 =
                "{\"type\":\"LOGIN_REQUEST\",\"data\":{" +
                        "\"email\":\"patient@mail.com\",\"password\":\"123\",\"access_permits\":\"Doctor\"}}";

        String plain2 =
                "{\"type\":\"LOGIN_REQUEST\",\"data\":{" +
                        "\"email\":\"admin@mail.com\",\"password\":\"123\",\"access_permits\":\"Doctor\"}}";

        String enc1 = TokenUtils.encrypt(plain1, aes);
        String enc2 = TokenUtils.encrypt(plain2, aes);

        JsonObject w1 = new JsonObject();
        w1.addProperty("type", "ENCRYPTED");
        w1.addProperty("data", enc1);

        JsonObject w2 = new JsonObject();
        w2.addProperty("type", "ENCRYPTED");
        w2.addProperty("data", enc2);

        when(mockReader.readLine())
                .thenReturn(w1.toString())
                .thenReturn(w2.toString())
                .thenReturn(null);

        handler.run();

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(mockWriter, atLeast(2)).println(captor.capture());

        int errorCount = 0;
        for (Object obj : captor.getAllValues()) {
            JsonObject outWrapper = (JsonObject) obj;
            String data = outWrapper.get("data").getAsString();
            String decrypted = TokenUtils.decrypt(data, aes);
            JsonObject resp = JsonParser.parseString(decrypted).getAsJsonObject();

            if ("LOGIN_RESPONSE".equals(resp.get("type").getAsString())) {
                if ("ERROR".equals(resp.get("status").getAsString())) {
                    errorCount++;
                }
            }
        }

        assertEquals(2, errorCount, "Both logins should be ERROR");
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

        Server server = mock(Server.class);
        KeyPair keyPair = RSAKeyManager.generateKeyPair();
        ClientHandler handler = new ClientHandler(socket, server, keyPair);

        BufferedReader mockReader = mock(BufferedReader.class);
        PrintWriter mockWriter = mock(PrintWriter.class);
        setField(handler, "in", mockReader);
        setField(handler, "out", mockWriter);

        SecretKey aes = TokenUtils.generateToken();
        setField(handler, "token", aes);

        AdminLinkService mockALS = mock(AdminLinkService.class);
        SecurityManager mockSec = mock(SecurityManager.class);
        UserJDBC mockUserJDBC = mock(UserJDBC.class);
        RoleJDBC mockRole = mock(RoleJDBC.class);

        when(server.getAdminLinkService()).thenReturn(mockALS);
        when(mockALS.getSecurityManager()).thenReturn(mockSec);
        when(mockSec.getUserJDBC()).thenReturn(mockUserJDBC);
        when(mockSec.getRoleJDBC()).thenReturn(mockRole);

        when(mockUserJDBC.isUser("patient@mail.com")).thenReturn(true);
        when(mockUserJDBC.login("patient@mail.com", "123"))
                .thenReturn(new User(2,"patient@mail.com","123",true,2));
        when(mockRole.findRoleByID(2)).thenReturn(new Role("Patient"));

        String plain =
                "{\"type\":\"LOGIN_REQUEST\",\"data\":{" +
                        "\"email\":\"patient@mail.com\",\"password\":\"123\",\"access_permits\":\"Patient\"}}";

        String enc = TokenUtils.encrypt(plain, aes);
        JsonObject w = new JsonObject();
        w.addProperty("type", "ENCRYPTED");
        w.addProperty("data", enc);

        when(mockReader.readLine()).thenReturn(w.toString()).thenReturn(null);

        handler.run();

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(mockWriter, atLeastOnce()).println(captor.capture());

        String success = null;
        for (Object obj : captor.getAllValues()) {
            JsonObject outWrapper = (JsonObject) obj;
            String data = outWrapper.get("data").getAsString();
            String decrypted = TokenUtils.decrypt(data, aes);
            JsonObject resp = JsonParser.parseString(decrypted).getAsJsonObject();

            if ("LOGIN_RESPONSE".equals(resp.get("type").getAsString())
                    && "SUCCESS".equals(resp.get("status").getAsString())) {
                success = decrypted;
            }
        }

        assertNotNull(success, "Expected SUCCESS patient login");
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

        Server server = mock(Server.class);
        KeyPair keyPair = RSAKeyManager.generateKeyPair();
        ClientHandler handler = new ClientHandler(socket, server, keyPair);

        BufferedReader mockReader = mock(BufferedReader.class);
        PrintWriter mockWriter = mock(PrintWriter.class);
        setField(handler, "in", mockReader);
        setField(handler, "out", mockWriter);

        SecretKey aes = TokenUtils.generateToken();
        setField(handler, "token", aes);

        AdminLinkService mockALS = mock(AdminLinkService.class);
        SecurityManager mockSec = mock(SecurityManager.class);
        UserJDBC mockUserJDBC = mock(UserJDBC.class);
        RoleJDBC mockRole = mock(RoleJDBC.class);

        when(server.getAdminLinkService()).thenReturn(mockALS);
        when(mockALS.getSecurityManager()).thenReturn(mockSec);
        when(mockSec.getUserJDBC()).thenReturn(mockUserJDBC);
        when(mockSec.getRoleJDBC()).thenReturn(mockRole);

        when(mockUserJDBC.isUser("doctor@mail.com")).thenReturn(true);
        when(mockUserJDBC.login("doctor@mail.com", "123"))
                .thenReturn(new User(2,"doctor@mail.com","123",true,1));

        when(mockUserJDBC.isUser("admin@mail.com")).thenReturn(true);
        when(mockUserJDBC.login("admin@mail.com", "123"))
                .thenReturn(new User(3,"admin@mail.com","123",true,3));

        when(mockRole.findRoleByID(1)).thenReturn(new Role("Doctor"));
        when(mockRole.findRoleByID(2)).thenReturn(new Role("Patient"));
        when(mockRole.findRoleByID(3)).thenReturn(new Role("Admin"));

        String p1 =
                "{\"type\":\"LOGIN_REQUEST\",\"data\":{" +
                        "\"email\":\"doctor@mail.com\",\"password\":\"123\",\"access_permits\":\"Patient\"}}";
        String p2 =
                "{\"type\":\"LOGIN_REQUEST\",\"data\":{" +
                        "\"email\":\"admin@mail.com\",\"password\":\"123\",\"access_permits\":\"Patient\"}}";

        String enc1 = TokenUtils.encrypt(p1, aes);
        String enc2 = TokenUtils.encrypt(p2, aes);

        JsonObject w1 = new JsonObject();
        w1.addProperty("type","ENCRYPTED");
        w1.addProperty("data",enc1);

        JsonObject w2 = new JsonObject();
        w2.addProperty("type","ENCRYPTED");
        w2.addProperty("data",enc2);

        when(mockReader.readLine())
                .thenReturn(w1.toString())
                .thenReturn(w2.toString())
                .thenReturn(null);

        handler.run();

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(mockWriter, atLeast(2)).println(captor.capture());

        int errorCount = 0;
        for (Object obj : captor.getAllValues()) {
            JsonObject outWrapper = (JsonObject) obj;
            String dec = TokenUtils.decrypt(outWrapper.get("data").getAsString(), aes);
            JsonObject resp = JsonParser.parseString(dec).getAsJsonObject();
            if ("LOGIN_RESPONSE".equals(resp.get("type").getAsString())
                    && "ERROR".equals(resp.get("status").getAsString())) {
                errorCount++;
            }
        }

        assertEquals(2, errorCount);
    }


    /**
     * It should send an error if the user does not exist
     * @throws Exception
     */
    @Test
    void testHandleLoginUserNotFound() throws Exception {
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);
        when(socket.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));

        Server server = mock(Server.class);
        KeyPair keyPair = RSAKeyManager.generateKeyPair();
        ClientHandler handler = new ClientHandler(socket, server, keyPair);

        BufferedReader mockReader = mock(BufferedReader.class);
        PrintWriter mockWriter = mock(PrintWriter.class);
        setField(handler, "in", mockReader);
        setField(handler, "out", mockWriter);

        SecretKey aes = TokenUtils.generateToken();
        setField(handler, "token", aes);

        AdminLinkService mockALS = mock(AdminLinkService.class);
        SecurityManager mockSec = mock(SecurityManager.class);
        UserJDBC mockUserJDBC = mock(UserJDBC.class);
        RoleJDBC mockRole = mock(RoleJDBC.class);

        when(server.getAdminLinkService()).thenReturn(mockALS);
        when(mockALS.getSecurityManager()).thenReturn(mockSec);
        when(mockSec.getUserJDBC()).thenReturn(mockUserJDBC);
        when(mockSec.getRoleJDBC()).thenReturn(mockRole);

        when(mockUserJDBC.isUser("random@mail.com")).thenReturn(false);

        String plain =
                "{\"type\":\"LOGIN_REQUEST\",\"data\":{" +
                        "\"email\":\"random@mail.com\",\"password\":\"123\",\"access_permits\":\"Patient\"}}";

        String enc = TokenUtils.encrypt(plain, aes);

        JsonObject w = new JsonObject();
        w.addProperty("type", "ENCRYPTED");
        w.addProperty("data", enc);

        when(mockReader.readLine()).thenReturn(w.toString()).thenReturn(null);

        handler.run();

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(mockWriter, atLeastOnce()).println(captor.capture());

        boolean errorSeen = false;
        for (Object obj : captor.getAllValues()) {
            JsonObject outWrapper = (JsonObject) obj;
            String dec = TokenUtils.decrypt(outWrapper.get("data").getAsString(), aes);
            JsonObject resp = JsonParser.parseString(dec).getAsJsonObject();
            if ("LOGIN_RESPONSE".equals(resp.get("type").getAsString())
                    && "ERROR".equals(resp.get("status").getAsString())) {
                errorSeen = true;
            }
        }
        assertTrue(errorSeen);
    }


    //TODO: check test
    @Test
    void testHandleRequestPatientByEmail() throws Exception {
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);
        when(socket.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));

        Server server = mock(Server.class);
        KeyPair keyPair = RSAKeyManager.generateKeyPair();
        ClientHandler handler = new ClientHandler(socket, server, keyPair);

        BufferedReader mockReader = mock(BufferedReader.class);
        PrintWriter mockWriter = mock(PrintWriter.class);
        setField(handler, "in", mockReader);
        setField(handler, "out", mockWriter);

        SecretKey aes = TokenUtils.generateToken();
        setField(handler, "token", aes);

        AdminLinkService mockALS = mock(AdminLinkService.class);
        SecurityManager mockSec = mock(SecurityManager.class);
        MedicalManager mockMed = mock(MedicalManager.class);
        UserJDBC mockUserJDBC = mock(UserJDBC.class);
        RoleJDBC mockRole = mock(RoleJDBC.class);
        PatientJDBC mockPatientJDBC = mock(PatientJDBC.class);
        SignalJDBC mockSignalJDBC = mock(SignalJDBC.class);
        ReportJDBC mockReportJDBC = mock(ReportJDBC.class);

        when(mockMed.getSignalJDBC()).thenReturn(mockSignalJDBC);
        when(mockMed.getReportJDBC()).thenReturn(mockReportJDBC);

        when(mockSignalJDBC.getSignalsByPatientId(anyInt())).thenReturn(List.of());
        when(mockReportJDBC.getReportsByPatientId(anyInt())).thenReturn(List.of());


        when(server.getAdminLinkService()).thenReturn(mockALS);
        when(mockALS.getSecurityManager()).thenReturn(mockSec);
        when(mockALS.getMedicalManager()).thenReturn(mockMed);
        when(mockSec.getUserJDBC()).thenReturn(mockUserJDBC);
        when(mockSec.getRoleJDBC()).thenReturn(mockRole);
        when(mockMed.getPatientJDBC()).thenReturn(mockPatientJDBC);

        User user = new User(1, "pat@mail.com", "123", true, 1);
        when(mockUserJDBC.findUserByID(1)).thenReturn(user);

        Patient patient = new Patient("A", "B", "pat@mail.com", "61235678",
                LocalDate.now(), "Female", 2);
        when(mockPatientJDBC.findPatientByEmail("pat@mail.com")).thenReturn(patient);
        when(mockRole.findRoleByID(1)).thenReturn(new Role(1, "Patient"));

        String plain =
                """
                {
                  "type": "REQUEST_PATIENT_BY_EMAIL",
                  "data": {
                     "email": "pat@mail.com",
                     "user_id": 1
                  }
                }
                """;

        String enc = TokenUtils.encrypt(plain, aes);
        JsonObject w = new JsonObject();
        w.addProperty("type","ENCRYPTED");
        w.addProperty("data",enc);

        when(mockReader.readLine()).thenReturn(w.toString()).thenReturn(null);

        handler.run();

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(mockWriter, atLeastOnce()).println(captor.capture());

        String success = null;
        for (Object obj : captor.getAllValues()) {
            JsonObject outWrapper = (JsonObject) obj;
            String dec = TokenUtils.decrypt(outWrapper.get("data").getAsString(), aes);
            JsonObject resp = JsonParser.parseString(dec).getAsJsonObject();

            if ("REQUEST_PATIENT_BY_EMAIL_RESPONSE".equals(resp.get("type").getAsString())
                    && "SUCCESS".equals(resp.get("status").getAsString())) {
                success = dec;
            }
        }

        assertNotNull(success);
    }


    /**
     * If it receives a REQUEST_DOCTOR_BY_EMAIL request, it first checks whether the user asking for the info has access permits.
     * - If yes, it sends a REQUES_DOCTOR_BY_EMAIL_RESPONSE with the doctor info
     * - If the user asking is a PATIENT or ADMIN, then it sends an error
     * @throws Exception
     */
    @Test
    void testHandleRequestDoctorByEmail() throws Exception {

        // ---- Socket + server ----
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);
        when(socket.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));

        Server server = mock(Server.class);
        KeyPair keyPair = RSAKeyManager.generateKeyPair();
        ClientHandler handler = new ClientHandler(socket, server, keyPair);

        // ---- Replace I/O ----
        BufferedReader mockReader = mock(BufferedReader.class);
        PrintWriter mockWriter = mock(PrintWriter.class);
        setField(handler, "in", mockReader);
        setField(handler, "out", mockWriter);

        // ---- AES token ----
        SecretKey aes = TokenUtils.generateToken();
        setField(handler, "token", aes);

        // ---- Full dependency chain ----
        AdminLinkService mockALS = mock(AdminLinkService.class);
        SecurityManager mockSec = mock(SecurityManager.class);
        MedicalManager mockMed = mock(MedicalManager.class);

        UserJDBC mockUserJDBC = mock(UserJDBC.class);
        DoctorJDBC mockDoctorJDBC = mock(DoctorJDBC.class);
        RoleJDBC mockRole = mock(RoleJDBC.class);

        when(server.getAdminLinkService()).thenReturn(mockALS);
        when(mockALS.getSecurityManager()).thenReturn(mockSec);
        when(mockALS.getMedicalManager()).thenReturn(mockMed);

        when(mockSec.getUserJDBC()).thenReturn(mockUserJDBC);
        when(mockSec.getRoleJDBC()).thenReturn(mockRole);

        when(mockMed.getDoctorJDBC()).thenReturn(mockDoctorJDBC);

        // ---- Two users ----
        User doctorUser = new User(1, "doc@mail.com", "123", true, 1); // DOCTOR
        User patientUser = new User(2, "pat@mail.com", "123", true, 2); // PATIENT

        when(mockUserJDBC.findUserByID(1)).thenReturn(doctorUser);
        when(mockUserJDBC.findUserByID(2)).thenReturn(patientUser);

        when(mockRole.findRoleByID(1)).thenReturn(new Role(1, "Doctor"));
        when(mockRole.findRoleByID(2)).thenReturn(new Role(2, "Patient"));

        // ---- DB doctor ----
        Doctor doctor = new Doctor("A","B","61234","doc@mail.com","Neuro","Science");
        when(mockDoctorJDBC.findDoctorByEmail("doc@mail.com")).thenReturn(doctor);

        // ---- Requests ----
        String p1 = """
       {"type":"REQUEST_DOCTOR_BY_EMAIL","data":{"email":"doc@mail.com","user_id":1}}
    """;
        String p2 = """
       {"type":"REQUEST_DOCTOR_BY_EMAIL","data":{"email":"doc@mail.com","user_id":2}}
    """;

        String enc1 = TokenUtils.encrypt(p1, aes);
        String enc2 = TokenUtils.encrypt(p2, aes);

        JsonObject w1 = new JsonObject();
        w1.addProperty("type","ENCRYPTED");
        w1.addProperty("data",enc1);

        JsonObject w2 = new JsonObject();
        w2.addProperty("type","ENCRYPTED");
        w2.addProperty("data",enc2);

        when(mockReader.readLine())
                .thenReturn(w1.toString())
                .thenReturn(w2.toString())
                .thenReturn(null);

        // ---- Run handler ----
        handler.run();

        // ---- Capture responses ----
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(mockWriter, atLeast(2)).println(captor.capture());

        boolean successSeen = false;
        boolean errorSeen = false;

        for (Object printed : captor.getAllValues()) {
            JsonObject out = (JsonObject) printed;
            String dec = TokenUtils.decrypt(out.get("data").getAsString(), aes);
            JsonObject resp = JsonParser.parseString(dec).getAsJsonObject();

            if ("REQUEST_DOCTOR_BY_EMAIL_RESPONSE".equals(resp.get("type").getAsString())) {
                if ("SUCCESS".equals(resp.get("status").getAsString())) successSeen = true;
                if ("ERROR".equals(resp.get("status").getAsString())) errorSeen = true;
            }
        }

        assertTrue(successSeen);
        assertTrue(errorSeen);
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

        Server server = mock(Server.class);
        KeyPair keyPair = RSAKeyManager.generateKeyPair();
        ClientHandler handler = new ClientHandler(socket, server, keyPair);

        BufferedReader mockReader = mock(BufferedReader.class);
        PrintWriter mockWriter = mock(PrintWriter.class);
        setField(handler, "in", mockReader);
        setField(handler, "out", mockWriter);

        SecretKey aes = TokenUtils.generateToken();
        setField(handler, "token", aes);

        AdminLinkService mockALS = mock(AdminLinkService.class);
        SecurityManager mockSec = mock(SecurityManager.class);
        MedicalManager mockMed = mock(MedicalManager.class);

        UserJDBC mockUserJDBC = mock(UserJDBC.class);
        DoctorJDBC mockDoctorJDBC = mock(DoctorJDBC.class);
        PatientJDBC mockPatientJDBC = mock(PatientJDBC.class);
        RoleJDBC mockRole = mock(RoleJDBC.class);

        when(server.getAdminLinkService()).thenReturn(mockALS);
        when(mockALS.getSecurityManager()).thenReturn(mockSec);
        when(mockALS.getMedicalManager()).thenReturn(mockMed);

        when(mockSec.getUserJDBC()).thenReturn(mockUserJDBC);
        when(mockSec.getRoleJDBC()).thenReturn(mockRole);
        when(mockMed.getDoctorJDBC()).thenReturn(mockDoctorJDBC);
        when(mockMed.getPatientJDBC()).thenReturn(mockPatientJDBC);

        // ---- Users ----
        User doc1 = new User(1,"doc@mail.com","123",true,1);
        User goodPatient = new User(2,"pat@mail.com","123",true,2);
        User doc2 = new User(3,"doc2@mail.com","123",true,1);
        User badPatient = new User(4,"pat2@mail.com","123",true,2);

        when(mockUserJDBC.findUserByID(1)).thenReturn(doc1);
        when(mockUserJDBC.findUserByID(2)).thenReturn(goodPatient);
        when(mockUserJDBC.findUserByID(3)).thenReturn(doc2);
        when(mockUserJDBC.findUserByID(4)).thenReturn(badPatient);

        when(mockRole.findRoleByID(1)).thenReturn(new Role(1,"Doctor"));
        when(mockRole.findRoleByID(2)).thenReturn(new Role(2,"Patient"));

        // ---- Doctors ----
        Doctor d1 = new Doctor("A","B","6123","doc@mail.com","Neuro","Brain");
        d1.setId(1);
        Doctor d2 = new Doctor("A","B","6123","doc2@mail.com","Neuro","Brain");
        d2.setId(2);

        when(mockDoctorJDBC.getDoctor(1)).thenReturn(d1);
        when(mockDoctorJDBC.getDoctor(2)).thenReturn(d2);

        // ---- Patients ----
        Patient p1 = new Patient("A","B","pat@mail.com","6123",LocalDate.now(),"Female",1);
        p1.setId(1);
        when(mockPatientJDBC.findPatientByEmail("pat@mail.com")).thenReturn(p1);

        Patient p2 = new Patient("A","B","pat2@mail.com","6123",LocalDate.now(),"Female",2);
        p2.setId(2);
        when(mockPatientJDBC.findPatientByEmail("pat2@mail.com")).thenReturn(p2);

        // ---- Requests ----
        String r1 = "{\"type\":\"REQUEST_DOCTOR_BY_ID\",\"data\":{\"doctor_id\":1,\"user_id\":1}}"; // GOOD doctor
        String r2 = "{\"type\":\"REQUEST_DOCTOR_BY_ID\",\"data\":{\"doctor_id\":1,\"user_id\":2}}"; // GOOD patient?
        String r3 = "{\"type\":\"REQUEST_DOCTOR_BY_ID\",\"data\":{\"doctor_id\":1,\"user_id\":3}}"; // WRONG doctor
        String r4 = "{\"type\":\"REQUEST_DOCTOR_BY_ID\",\"data\":{\"doctor_id\":1,\"user_id\":4}}"; // WRONG patient

        String e1 = TokenUtils.encrypt(r1,aes);
        String e2 = TokenUtils.encrypt(r2,aes);
        String e3 = TokenUtils.encrypt(r3,aes);
        String e4 = TokenUtils.encrypt(r4,aes);

        JsonObject w1 = new JsonObject(); w1.addProperty("type","ENCRYPTED"); w1.addProperty("data",e1);
        JsonObject w2 = new JsonObject(); w2.addProperty("type","ENCRYPTED"); w2.addProperty("data",e2);
        JsonObject w3 = new JsonObject(); w3.addProperty("type","ENCRYPTED"); w3.addProperty("data",e3);
        JsonObject w4 = new JsonObject(); w4.addProperty("type","ENCRYPTED"); w4.addProperty("data",e4);

        when(mockReader.readLine())
                .thenReturn(w1.toString())
                .thenReturn(w2.toString())
                .thenReturn(w3.toString())
                .thenReturn(w4.toString())
                .thenReturn(null);

        handler.run();

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(mockWriter, atLeast(4)).println(captor.capture());

        int success = 0;
        int error = 0;

        for (Object printed : captor.getAllValues()) {
            JsonObject out = (JsonObject) printed;

            String decrypted = TokenUtils.decrypt(out.get("data").getAsString(), aes);
            JsonObject resp = JsonParser.parseString(decrypted).getAsJsonObject();

            if (!resp.get("type").getAsString().equals("REQUEST_DOCTOR_BY_ID_RESPONSE"))
                continue;

            String st = resp.get("status").getAsString();
            if (st.equals("SUCCESS")) success++;
            if (st.equals("ERROR")) error++;
        }

        assertTrue(success >= 2);
        assertTrue(error >= 2);
    }

    @Test
    void testHandleRequestPatientsFromDoctor() throws Exception {

        // --- Socket + handler setup ---
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);
        when(socket.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));

        Server server = mock(Server.class);
        KeyPair keyPair = RSAKeyManager.generateKeyPair();
        ClientHandler handler = new ClientHandler(socket, server, keyPair);

        BufferedReader mockReader = mock(BufferedReader.class);
        PrintWriter mockWriter = mock(PrintWriter.class);
        setField(handler, "in", mockReader);
        setField(handler, "out", mockWriter);

        SecretKey aes = TokenUtils.generateToken();
        setField(handler, "token", aes);

        // --- FULL dependency chain ---
        AdminLinkService mockALS = mock(AdminLinkService.class);
        SecurityManager mockSec = mock(SecurityManager.class);
        MedicalManager mockMed = mock(MedicalManager.class);

        UserJDBC mockUserJDBC = mock(UserJDBC.class);
        RoleJDBC mockRoleJDBC = mock(RoleJDBC.class);
        DoctorJDBC mockDoctorJDBC = mock(DoctorJDBC.class);
        PatientJDBC mockPatientJDBC = mock(PatientJDBC.class);

        when(server.getAdminLinkService()).thenReturn(mockALS);
        when(mockALS.getSecurityManager()).thenReturn(mockSec);
        when(mockALS.getMedicalManager()).thenReturn(mockMed);

        when(mockSec.getUserJDBC()).thenReturn(mockUserJDBC);
        when(mockSec.getRoleJDBC()).thenReturn(mockRoleJDBC);
        when(mockMed.getDoctorJDBC()).thenReturn(mockDoctorJDBC);
        when(mockMed.getPatientJDBC()).thenReturn(mockPatientJDBC);

        SignalJDBC mockSignalJDBC = mock(SignalJDBC.class);
        ReportJDBC mockReportJDBC = mock(ReportJDBC.class);

        when(mockMed.getSignalJDBC()).thenReturn(mockSignalJDBC);
        when(mockMed.getReportJDBC()).thenReturn(mockReportJDBC);

        when(mockSignalJDBC.getSignalsByPatientId(anyInt())).thenReturn(List.of());
        when(mockReportJDBC.getReportsByPatientId(anyInt())).thenReturn(List.of());


        // --- USERS ---
        User doctorUser = new User(1, "doc@mail.com", "pass", true, 1);
        User wrongUserPat = new User(2, "other@mail.com", "pass", true, 2);
        User wrongUserDoc = new User(3, "doc2@mail.com", "pass", true, 1);

        when(mockUserJDBC.findUserByID(1)).thenReturn(doctorUser);
        when(mockUserJDBC.findUserByID(2)).thenReturn(wrongUserPat);
        when(mockUserJDBC.findUserByID(3)).thenReturn(wrongUserDoc);

        when(mockRoleJDBC.findRoleByID(1)).thenReturn(new Role(1,"Doctor"));
        when(mockRoleJDBC.findRoleByID(2)).thenReturn(new Role(2,"Patient"));

        // --- DOCTORS ---
        Doctor doctor = new Doctor("A","B","6123","doc@mail.com","Cardiology","Card");
        doctor.setId(10);
        when(mockDoctorJDBC.getDoctor(10)).thenReturn(doctor);

        // --- DOCTOR’s patients ---
        Patient p1 = new Patient("Toni","Blue","t@c.com","999",LocalDate.now(),"M",1);
        Patient p2 = new Patient("Rosa","Green","r@g.com","888",LocalDate.now(),"F",1);
        when(mockPatientJDBC.getPatientsOfDoctor(10)).thenReturn(List.of(p1, p2));

        // --- 3 requests (1 good, 2 forbidden) ---
        String req1 = """
      {"type":"REQUEST_PATIENTS_FROM_DOCTOR","data":{"doctor_id":10,"user_id":1}}
    """;
        String req2 = """
      {"type":"REQUEST_PATIENTS_FROM_DOCTOR","data":{"doctor_id":10,"user_id":2}}
    """;
        String req3 = """
      {"type":"REQUEST_PATIENTS_FROM_DOCTOR","data":{"doctor_id":10,"user_id":3}}
    """;

        JsonObject w1 = new JsonObject(); w1.addProperty("type","ENCRYPTED"); w1.addProperty("data",TokenUtils.encrypt(req1,aes));
        JsonObject w2 = new JsonObject(); w2.addProperty("type","ENCRYPTED"); w2.addProperty("data",TokenUtils.encrypt(req2,aes));
        JsonObject w3 = new JsonObject(); w3.addProperty("type","ENCRYPTED"); w3.addProperty("data",TokenUtils.encrypt(req3,aes));

        when(mockReader.readLine())
                .thenReturn(w1.toString())
                .thenReturn(w2.toString())
                .thenReturn(w3.toString())
                .thenReturn(null);

        // --- Act ---
        handler.run();

        // --- Assert ---
        ArgumentCaptor<JsonObject> captor = ArgumentCaptor.forClass(JsonObject.class);
        verify(mockWriter, atLeast(3)).println(captor.capture());

        int success = 0;
        int error = 0;

        for (JsonObject wrapper : captor.getAllValues()) {
            String decrypted = TokenUtils.decrypt(wrapper.get("data").getAsString(), aes);
            JsonObject resp = JsonParser.parseString(decrypted).getAsJsonObject();
            if (!resp.has("status")) continue;

            if (resp.get("status").getAsString().equals("SUCCESS")) success++;
            if (resp.get("status").getAsString().equals("ERROR")) error++;
        }

        assertEquals(1, success);
        assertEquals(2, error);
    }



    @Test
    void testHandleSaveCommentsSignal2() throws Exception {

        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);
        when(socket.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));

        Server server = mock(Server.class);
        KeyPair keyPair = RSAKeyManager.generateKeyPair();
        ClientHandler handler = new ClientHandler(socket, server, keyPair);

        BufferedReader mockReader = mock(BufferedReader.class);
        PrintWriter mockWriter = mock(PrintWriter.class);
        setField(handler, "in", mockReader);
        setField(handler, "out", mockWriter);

        SecretKey aes = TokenUtils.generateToken();
        setField(handler, "token", aes);

        AdminLinkService mockALS = mock(AdminLinkService.class);
        SecurityManager mockSec = mock(SecurityManager.class);
        MedicalManager mockMed = mock(MedicalManager.class);

        UserJDBC mockUserJDBC = mock(UserJDBC.class);
        DoctorJDBC mockDoctorJDBC = mock(DoctorJDBC.class);
        PatientJDBC mockPatientJDBC = mock(PatientJDBC.class);
        SignalJDBC mockSignalJDBC = mock(SignalJDBC.class);
        RoleJDBC mockRole = mock(RoleJDBC.class);

        when(server.getAdminLinkService()).thenReturn(mockALS);
        when(mockALS.getSecurityManager()).thenReturn(mockSec);
        when(mockALS.getMedicalManager()).thenReturn(mockMed);

        when(mockSec.getUserJDBC()).thenReturn(mockUserJDBC);
        when(mockSec.getRoleJDBC()).thenReturn(mockRole);

        when(mockMed.getDoctorJDBC()).thenReturn(mockDoctorJDBC);
        when(mockMed.getPatientJDBC()).thenReturn(mockPatientJDBC);
        when(mockMed.getSignalJDBC()).thenReturn(mockSignalJDBC);

        when(mockRole.findRoleByID(1)).thenReturn(new Role(1,"Doctor"));

        User doctorUser = new User(1,"doc@mail.com","123",true,1);
        when(mockUserJDBC.findUserByID(1)).thenReturn(doctorUser);

        Doctor doctor = new Doctor("A","B","999","doc@mail.com","Cardio","Cardio");
        when(mockDoctorJDBC.findDoctorByEmail("doc@mail.com")).thenReturn(doctor);
        when(mockDoctorJDBC.getDoctorFromPatient(5)).thenReturn(doctor);

        when(mockSignalJDBC.updateSignalComments(7,"hello world")).thenReturn(true);

        String plain = """
      {"type":"SAVE_COMMENTS_SIGNAL","data":{"comments":"hello world","signal_id":7,"patient_id":5,"user_id":1}}
    """;

        String enc = TokenUtils.encrypt(plain,aes);
        JsonObject w = new JsonObject();
        w.addProperty("type","ENCRYPTED");
        w.addProperty("data",enc);

        when(mockReader.readLine())
                .thenReturn(w.toString())
                .thenReturn(null);

        handler.run();

        ArgumentCaptor<JsonObject> captor = ArgumentCaptor.forClass(JsonObject.class);
        verify(mockWriter, atLeastOnce()).println(captor.capture());

        JsonObject out = captor.getValue();
        String decrypted = TokenUtils.decrypt(out.get("data").getAsString(), aes);
        JsonObject resp = JsonParser.parseString(decrypted).getAsJsonObject();

        assertEquals("SAVE_COMMENTS_SIGNAL_RESPONSE", resp.get("type").getAsString());
        assertEquals("SUCCESS", resp.get("status").getAsString());
    }


    @Test
    void testForceShutdownClosesSocket() throws Exception {
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);
        when(socket.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));

        ClientHandler handler = new ClientHandler(socket, server, keyPair);
        handler.run();
        handler.forceShutdown();

        verify(socket).close();
    }


    @Test
    void testReleaseResources() throws Exception {

        // --- Mock socket ---
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);
        when(socket.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));

        // --- Mock server ---
        Server server = mock(Server.class);

        // --- Create handler ---
        KeyPair keyPair = RSAKeyManager.generateKeyPair();
        ClientHandler handler = new ClientHandler(socket, server, keyPair);

        // --- Inject mocked reader/writer ---
        BufferedReader mockReader = mock(BufferedReader.class);
        PrintWriter mockWriter = mock(PrintWriter.class);

        setField(handler, "in", mockReader);
        setField(handler, "out", mockWriter);

        // --- Call releaseResources directly ---
        Method release = ClientHandler.class.getDeclaredMethod(
                "releaseResources",
                BufferedReader.class,
                PrintWriter.class,
                Socket.class
        );
        release.setAccessible(true);

        release.invoke(handler, mockReader, mockWriter, socket);

        // --- Verify closing behaviour ---
        verify(mockReader).close();
        verify(mockWriter).close();
        verify(socket).close();

        // --- Verify the handler is stopped ---
        assertTrue(handler.isStopped());

        // --- VERIFY that Server.removeClient(handler) was called! ---
        verify(server).removeClient(handler);
    }

    @Test
    void testHandleRequestSignalPatient2() throws Exception {
        // -------- 1) Mock socket + server --------
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);
        when(socket.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));

        Server server = mock(Server.class);
        KeyPair keyPair = RSAKeyManager.generateKeyPair();

        ClientHandler handler = new ClientHandler(socket, server, keyPair);

        // -------- 2) Inject mocked reader/writer --------
        BufferedReader mockReader = mock(BufferedReader.class);
        PrintWriter mockWriter = mock(PrintWriter.class);

        setField(handler, "in", mockReader);
        setField(handler, "out", mockWriter);

        // -------- 3) Inject AES token (skip RSA handshake) --------
        SecretKey aesKey = TokenUtils.generateToken();
        setField(handler, "token", aesKey);

        // -------- 4) Wire server → AdminLinkService → MedicalManager → JDBC --------
        AdminLinkService mockALS = mock(AdminLinkService.class);
        MedicalManager mockMed = mock(MedicalManager.class);
        PatientJDBC mockPatientJDBC = mock(PatientJDBC.class);
        SignalJDBC mockSignalJDBC = mock(SignalJDBC.class);

        when(server.getAdminLinkService()).thenReturn(mockALS);
        when(mockALS.getMedicalManager()).thenReturn(mockMed);
        when(mockMed.getPatientJDBC()).thenReturn(mockPatientJDBC);
        when(mockMed.getSignalJDBC()).thenReturn(mockSignalJDBC);

        int patientId = 12;
        Patient fakePatient = new Patient("Test", "Patient", "email@x.com", "12",
                LocalDate.now(), "F", 99);
        fakePatient.setId(patientId);

        when(mockPatientJDBC.findPatientByID(patientId)).thenReturn(fakePatient);
        when(mockSignalJDBC.insertSignal(any(Signal.class))).thenReturn(true);

        // -------- 5) Prepare a fake ZIP and Base64-encode it --------
        File tmpZip = File.createTempFile("signal_test_", ".zip");
        Files.writeString(tmpZip.toPath(), "SIGNAL_TEST_DATA");

        String base64Zip = Base64.getEncoder()
                .encodeToString(Files.readAllBytes(tmpZip.toPath()));

        // -------- 6) Build inner UPLOAD_SIGNAL JSON --------
        String innerJson = """
        {
          "type": "UPLOAD_SIGNAL",
          "metadata": {
            "patient_id": %d,
            "sampling_rate": 500,
            "timestamp": "2025-02-12T18:32:11"
          },
          "filename": "signal_test.zip",
          "dataBytes": "%s"
        }
        """.formatted(patientId, base64Zip);

        // Encrypt with AES and wrap in type=ENCRYPTED
        String encryptedPayload = TokenUtils.encrypt(innerJson, aesKey);

        JsonObject wrapper = new JsonObject();
        wrapper.addProperty("type", "ENCRYPTED");
        wrapper.addProperty("data", encryptedPayload);

        when(mockReader.readLine())
                .thenReturn(wrapper.toString()) // encrypted UPLOAD_SIGNAL
                .thenReturn(null);              // end-of-stream

        // -------- 7) Run handler --------
        handler.run();

        // -------- 8) Capture ENCRYPTED response and decrypt it --------
        ArgumentCaptor<JsonObject> captor = ArgumentCaptor.forClass(JsonObject.class);
        verify(mockWriter, atLeastOnce()).println(captor.capture());

        JsonObject sentWrapper = captor.getValue();
        assertEquals("ENCRYPTED", sentWrapper.get("type").getAsString());

        String encryptedResponse = sentWrapper.get("data").getAsString();
        String decryptedResponse = TokenUtils.decrypt(encryptedResponse, aesKey);

        System.out.println("DECRYPTED SERVER RESPONSE = " + decryptedResponse);

        // Basic assertions on the INNER response
        assertTrue(decryptedResponse.contains("\"type\":\"UPLOAD_SIGNAL_RESPONSE\""));
        assertTrue(decryptedResponse.contains("\"status\":\"SUCCESS\""));
        assertTrue(decryptedResponse.contains("Signal uploaded correctly"));
    }


    @Test
    void testHandleRequestSignal2() throws Exception {
        // ---- 1) Socket + Server + handler ----
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);
        when(socket.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));

        Server server = mock(Server.class);
        KeyPair keyPair = RSAKeyManager.generateKeyPair();
        ClientHandler handler = new ClientHandler(socket, server, keyPair);

        // ---- 2) Streams ----
        BufferedReader mockReader = mock(BufferedReader.class);
        PrintWriter mockWriter = mock(PrintWriter.class);
        setField(handler, "in", mockReader);
        setField(handler, "out", mockWriter);

        // ---- 3) AES token ----
        SecretKey aesKey = TokenUtils.generateToken();
        setField(handler, "token", aesKey);

        // ---- 4) Server deps ----
        AdminLinkService mockALS = mock(AdminLinkService.class);
        MedicalManager mockMed = mock(MedicalManager.class);
        SecurityManager mockSec = mock(SecurityManager.class);
        UserJDBC mockUserJDBC = mock(UserJDBC.class);
        RoleJDBC mockRoleJDBC = mock(RoleJDBC.class);
        SignalJDBC mockSignalJDBC = mock(SignalJDBC.class);

        when(server.getAdminLinkService()).thenReturn(mockALS);
        when(mockALS.getMedicalManager()).thenReturn(mockMed);
        when(mockALS.getSecurityManager()).thenReturn(mockSec);

        when(mockMed.getSignalJDBC()).thenReturn(mockSignalJDBC);
        when(mockSec.getUserJDBC()).thenReturn(mockUserJDBC);
        when(mockSec.getRoleJDBC()).thenReturn(mockRoleJDBC);

        // User is a Doctor
        User doctorUser = new User(1, "doc@mail.com", "pass", true, 1);
        when(mockUserJDBC.findUserByID(1)).thenReturn(doctorUser);
        when(mockRoleJDBC.findRoleByID(1)).thenReturn(new Role(1, "Doctor"));

        // ---- 5) Fake signal ----
        File zip = File.createTempFile("signal88_", ".zip");
        Files.writeString(zip.toPath(), "FAKE_ZIP_CONTENT");

        Signal fakeSignal = new Signal(zip, LocalDate.now(), "Hello world", 15, 1000);
        fakeSignal.setId(88);
        when(mockSignalJDBC.findSignalById(88)).thenReturn(fakeSignal);

        // ---- 6) Inner REQUEST_SIGNAL JSON ----
        String innerJson = """
        {
           "type": "REQUEST_SIGNAL",
           "data": {
             "signal_id": 88,
             "user_id": 1
           }
        }
        """;

        String encryptedPayload = TokenUtils.encrypt(innerJson, aesKey);

        JsonObject wrapper = new JsonObject();
        wrapper.addProperty("type", "ENCRYPTED");
        wrapper.addProperty("data", encryptedPayload);

        when(mockReader.readLine())
                .thenReturn(wrapper.toString())
                .thenReturn(null);

        // ---- 7) Run handler ----
        handler.run();

        // ---- 8) Capture and decrypt response ----
        ArgumentCaptor<JsonObject> captor = ArgumentCaptor.forClass(JsonObject.class);
        verify(mockWriter, atLeastOnce()).println(captor.capture());

        JsonObject respWrapper = captor.getValue();
        assertEquals("ENCRYPTED", respWrapper.get("type").getAsString());

        String encResp = respWrapper.get("data").getAsString();
        String decResp = TokenUtils.decrypt(encResp, aesKey);

        System.out.println("DECRYPTED RESPONSE = " + decResp);

        // Old assertions, but on decrypted payload
        assertTrue(decResp.contains("\"status\":\"SUCCESS\""));
        assertTrue(decResp.contains("zip-base64"));      // compression
        assertTrue(decResp.contains("signal_88"));       // filename or id, like before
        assertTrue(decResp.contains("REQUEST_SIGNAL_RESPONSE"));
    }



    @Test
    void testHandleRequestPatientSignals2() throws Exception {
        // ---- 1) Socket + Server + handler ----
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);
        when(socket.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));

        Server server = mock(Server.class);
        KeyPair keyPair = RSAKeyManager.generateKeyPair();
        ClientHandler handler = new ClientHandler(socket, server, keyPair);

        // ---- 2) Streams ----
        BufferedReader mockReader = mock(BufferedReader.class);
        PrintWriter mockWriter = mock(PrintWriter.class);
        setField(handler, "in", mockReader);
        setField(handler, "out", mockWriter);

        // ---- 3) AES token ----
        SecretKey aesKey = TokenUtils.generateToken();
        setField(handler, "token", aesKey);

        // ---- 4) Server deps ----
        AdminLinkService mockALS = mock(AdminLinkService.class);
        MedicalManager mockMed = mock(MedicalManager.class);
        SecurityManager mockSec = mock(SecurityManager.class);
        UserJDBC mockUserJDBC = mock(UserJDBC.class);
        RoleJDBC mockRoleJDBC = mock(RoleJDBC.class);
        SignalJDBC mockSignalJDBC = mock(SignalJDBC.class);
        PatientJDBC mockPatientJDBC = mock(PatientJDBC.class);

        when(server.getAdminLinkService()).thenReturn(mockALS);
        when(mockALS.getMedicalManager()).thenReturn(mockMed);
        when(mockALS.getSecurityManager()).thenReturn(mockSec);

        when(mockSec.getUserJDBC()).thenReturn(mockUserJDBC);
        when(mockSec.getRoleJDBC()).thenReturn(mockRoleJDBC);

        when(mockMed.getSignalJDBC()).thenReturn(mockSignalJDBC);
        when(mockMed.getPatientJDBC()).thenReturn(mockPatientJDBC);

        // Doctor user
        User doctorUser = new User(1, "doc@mail.com", "123", true, 1);
        when(mockUserJDBC.findUserByID(1)).thenReturn(doctorUser);
        when(mockRoleJDBC.findRoleByID(1)).thenReturn(new Role(1, "Doctor"));

        int patientId = 88;
        when(mockPatientJDBC.findPatientByID(patientId))
                .thenReturn(new Patient("A", "B", "pat@mail.com", "999",
                        LocalDate.now(), "F", 1));

        // Signals
        when(mockSignalJDBC.getSignalsByPatientId(patientId)).thenReturn(List.of(
                new Signal(1, LocalDate.now(), "Sig 1", 10, 500),
                new Signal(2, LocalDate.now(), "Sig 2", 15, 1000),
                new Signal(3, LocalDate.now(), "Sig 3", 20, 2000)
        ));

        // ---- 5) Inner REQUEST_PATIENT_SIGNALS JSON ----
        String innerJson = """
        {
            "type": "REQUEST_PATIENT_SIGNALS",
            "data": {
                "patient_id": %d,
                "user_id": 1
            }
        }
        """.formatted(patientId);

        String encryptedPayload = TokenUtils.encrypt(innerJson, aesKey);

        JsonObject wrapper = new JsonObject();
        wrapper.addProperty("type", "ENCRYPTED");
        wrapper.addProperty("data", encryptedPayload);

        when(mockReader.readLine())
                .thenReturn(wrapper.toString())
                .thenReturn(null);

        // ---- 6) Run handler ----
        handler.run();

        // ---- 7) Capture and decrypt ----
        ArgumentCaptor<JsonObject> captor = ArgumentCaptor.forClass(JsonObject.class);
        verify(mockWriter, atLeastOnce()).println(captor.capture());

        JsonObject respWrapper = captor.getValue();
        assertEquals("ENCRYPTED", respWrapper.get("type").getAsString());

        String encResp = respWrapper.get("data").getAsString();
        String decResp = TokenUtils.decrypt(encResp, aesKey);

        System.out.println("DECRYPTED RESPONSE = " + decResp);

        // ---- Assertions on decrypted payload ----
        assertTrue(decResp.contains("REQUEST_PATIENT_SIGNALS_RESPONSE"));
        assertTrue(decResp.contains("\"status\":\"SUCCESS\""));
        assertTrue(decResp.contains("signals"));
        assertTrue(decResp.contains("\"signal_id\":1"));
        assertTrue(decResp.contains("\"signal_id\":2"));
        assertTrue(decResp.contains("\"signal_id\":3"));
    }

}
