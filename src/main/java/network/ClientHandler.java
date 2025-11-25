package network;

import com.google.gson.*;
import encryption.AESUtil;
import encryption.RSAUtil;
import org.example.JDBC.medicaldb.SignalJDBC;
import org.example.entities_medicaldb.*;
import org.example.entities_securitydb.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientHandler implements Runnable {
    final Socket socket;
    private final Server server;
    BufferedReader in;
    private BufferedWriter out;
    private final Gson gson = new Gson();;
    //Asegura que los cambios en la variable se realizan sin interferencia de otros hilos. Evitar race conditions
    private AtomicBoolean running;
    private KeyPair serverKeyPair; //This is going to be the server's public key
    private PublicKey clientPublicKey; //This is going to be the client's public key
    private SecretKey AESkey;

    public ClientHandler(Socket socket, Server server, KeyPair serverKeyPair) throws IOException {
        this.socket = socket;
        this.server = server;
        this.serverKeyPair = serverKeyPair;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        running = new AtomicBoolean(true);
    }

    @Override
    public void run(){
        try {
            //Before doing anything, the server sends its public key
            sendPublicKey();

            String line;
            label:
            while (running.get() && (line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {continue;} //Skip empty lines
                // Desencriptar Json
                JsonObject request;
                try {
                   request = gson.fromJson(line, JsonObject.class);
                    System.out.println(request);//Turns the lines into a JsonObject
                }catch (JsonSyntaxException e){
                    System.out.println(line);
                    continue;
                }
                if (request == null) {continue;}

                // Extract the type field from the JSON
                String type = request.get("type").getAsString();
                String typeDecrypted = type; //default original type
                JsonObject decryptedRequest = request; //default original request
                if(type.equals("ENCRYPTED")){
                    String encryptedData = request.get("data").getAsString();
                    String decryptedJson = AESUtil.decrypt(encryptedData, AESkey);
                    System.out.println("This is the decrypted json: "+decryptedJson);
                    decryptedRequest = gson.fromJson(decryptedJson, JsonObject.class);
                    typeDecrypted = decryptedRequest.get("type").getAsString();
                }
                System.out.println("This is the type"+typeDecrypted);

                // The type will tell the server what action to perform
                switch (typeDecrypted) {
                    case "STOP_CLIENT":
                        //Client asked to stop itself or server asked client to stop and client echoes
                        System.out.println("Received STOP_CLIENT from"+getSocketAddress());
                        releaseResources(in, out, socket);;
                        break;
                    case "LOGIN_REQUEST": {
                        System.out.println("LOGIN REQUEST");
                        handleLogIn(decryptedRequest.getAsJsonObject("data"));
                        break;
                    }
                    case "REQUEST_DOCTOR_BY_EMAIL": {
                        System.out.println("REQUEST DOCTOR_BY_EMAIL");
                        handleRequestDoctorByEmail(decryptedRequest.getAsJsonObject("data"));
                        break;
                    }
                    case "REQUEST_PATIENTS_FROM_DOCTOR": {
                        System.out.println("REQUEST_PATIENTS_FROM_DOCTOR");
                        handleRequestPatientsFromDoctor(decryptedRequest.getAsJsonObject("data"));
                        break;
                    }
                    case "REQUEST_PATIENT_BY_EMAIL": {
                        System.out.println("REQUEST_PATIENT_BY_EMAIL");
                        handleRequestPatientByEmail(decryptedRequest.getAsJsonObject("data"));
                        break;
                    }
                    case "REQUEST_DOCTOR_BY_ID": {
                        System.out.println("REQUEST DOCTOR_BY_ID");
                        handleRequestDoctorById(decryptedRequest.getAsJsonObject("data"));
                        break;
                    }
                    case "SAVE_COMMENTS_SIGNAL": {
                        System.out.println("SAVE_COMMENTS_SIGNAL");
                        handleSaveCommentsSignal(decryptedRequest.getAsJsonObject("data"));
                        break;
                    }
                    case "UPLOAD_SIGNAL" : {
                        System.out.println("UPLOAD_SIGNAL");
                        handleRequestSignalPatient(decryptedRequest);
                        break;
                    }
                    case "REQUEST_SIGNAL" : {
                        System.out.println("REQUEST_SIGNAL");
                        handleRequestSignal(decryptedRequest.getAsJsonObject("data"));
                        break;
                    }

                    case "REQUEST_PATIENT_SIGNALS" : {
                        System.out.println("REQUEST_PATIENT_SIGNALS");
                        handleRequestPatientSignals(decryptedRequest.getAsJsonObject("data"));
                        break;
                    }
                    case "SAVE_REPORT":{
                        System.out.println("SAVE_REPORT");
                        handleSaveReportRequest(decryptedRequest.getAsJsonObject("data"));
                        break;
                    }

                    case "CLIENT_AES_KEY" : {
                        System.out.println("CLIENT_AES_KEY");
                        // This one is encrypted by public key encryption2
                        String encryptedAESkey = request.get("data").getAsString();
                        try {
                            String decryptedAESkey = RSAUtil.decrypt(encryptedAESkey, serverKeyPair.getPrivate());
                            byte[] AESkeyBytes = Base64.getDecoder().decode(decryptedAESkey); //In bytes
                            SecretKey AESkey = new SecretKeySpec(AESkeyBytes, 0, AESkeyBytes.length, "AES");
                            //Store the secret key inside the Handler for the connection
                            this.AESkey = AESkey;
                            System.out.println("AES key retrieved and decrypted successfully");
                            System.out.println("This is the Server's secret key AES:"+Base64.getEncoder().encodeToString(AESkey.getEncoded()));
                            System.out.println("This is the Server's public key RSA:"+Base64.getEncoder().encodeToString(serverKeyPair.getPublic().getEncoded())+"and the private RSA key: "+Base64.getEncoder().encode(serverKeyPair.getPrivate().getEncoded()));

                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                }

            }
        } catch (Exception e){
            if(e.getClass() == SocketException.class){
                try {
                    System.out.println("Client stopped connection abruptly");
                    releaseResources(in, out, socket);
                    System.out.println("Client thread stopped");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }else{
                System.out.println("Client: "+socket.getInetAddress()+":"+running.get());
                //System.out.println("Error reading from client"+e.getMessage());
            }
        }
    }

    private void handleRequestPatientSignals(JsonObject data) {
        JsonObject response = new JsonObject();
        response.addProperty("type", "REQUEST_PATIENT_SIGNALS_RESPONSE");

        int patientId = data.get("patient_id").getAsInt();
        int userId   = data.get("user_id").getAsInt();
        User user = server.getAppMain().userJDBC.findUserByID(userId);
        if (user == null) {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "User not found");
            sendRawJson(response);
            return;
        }
        Role role = server.getAppMain().securityManager.getRoleJDBC().findRoleByID(user.getRole_id());
        if (role == null || !role.getRolename().equals("Doctor")) {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Not authorized");
            sendRawJson(response);
            return;
        }

        List<Signal> signals = server.getAppMain().medicalManager.getSignalJDBC().getSignalsByPatientId(patientId);

        JsonArray signalsArray = new JsonArray();
        for (Signal signal : signals) {
            JsonObject signalObj = new JsonObject();
            signalObj.addProperty("signal_id", signal.getId());
            signalObj.addProperty("date", signal.getDate().toString());
            signalObj.addProperty("sampling_rate", signal.getSampleFrequency());
            signalObj.addProperty("comments", signal.getComments());
            signalsArray.add(signalObj);
        }

        response.addProperty("status", "SUCCESS");
        response.add("signals", signalsArray);
        sendRawJson(response);
    }

    private void handleRequestSignal(JsonObject data) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("type", "REQUEST_SIGNAL_RESPONSE");

        int signalId = data.get("signal_id").getAsInt();
        int userId   = data.get("user_id").getAsInt();
        User user = server.getAppMain().userJDBC.findUserByID(userId);
        if (user == null) {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "User not found");
            sendRawJson(response);
            return;
        }
        Role role = server.getAppMain().securityManager.getRoleJDBC().findRoleByID(user.getRole_id());
        if (role == null || !role.getRolename().equals("Doctor")) {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Not authorized");
            sendRawJson(response);
            return;
        }

        Signal signal = server.getAppMain().medicalManager.getSignalJDBC().findSignalById(signalId);

        if (signal == null) {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Signal not found");
            sendRawJson(response);

        }else {
            byte[] zipBytes = Files.readAllBytes(signal.getFile().toPath());
            String base64Zip = Base64.getEncoder().encodeToString(zipBytes);
            JsonObject metadata = new JsonObject();
            metadata.addProperty("type", "REQUEST_SIGNAL_METADATA");
            metadata.addProperty("signal_id", signal.getId());
            metadata.addProperty("patient_id", signal.getPatientId());
            metadata.addProperty("sampling_rate", signal.getSampleFrequency());
            metadata.addProperty("comments", signal.getComments());
            metadata.addProperty("date", signal.getDate().toString());
            response.addProperty("status", "SUCCESS");
            response.addProperty("compression", "zip-base64");
            response.addProperty("filename", "signal_" + signal.getId() + ".zip");
            response.addProperty("data", base64Zip);
            response.add("metadata", metadata);
            sendRawJson(response);
        }
    }

    private void handleRequestSignalPatient(JsonObject dataIn) throws IOException {

        JsonObject response = new JsonObject();
        response.addProperty("type", "UPLOAD_SIGNAL_RESPONSE");

            JsonObject metadata = dataIn.getAsJsonObject("metadata");

            int patientId = metadata.get("patient_id").getAsInt();
            int samplingRate = metadata.get("sampling_rate").getAsInt();
            int duration = metadata.get("duration_seconds").getAsInt();
            String timestamp = metadata.get("timestamp").getAsString();
            LocalDateTime dateTime = LocalDateTime.parse(timestamp);

            String filename = dataIn.get("filename").getAsString();
            String base64Data = dataIn.get("datafile").getAsString();

            Patient patient = server.getAppMain().medicalManager.getPatientJDBC().findPatientByID(patientId);

            if (patient == null) {
                response.addProperty("status", "ERROR");
                response.addProperty("message", "Patient not found");
                sendRawJson(response);
                return;
            }
            // Decode base64 data
            byte[] zipBytes = Base64.getDecoder().decode(base64Data);

            File tempZip = File.createTempFile("signal_", ".zip");
            try (FileOutputStream fos = new FileOutputStream(tempZip)) {
                fos.write(zipBytes);
            }

            Signal record = new Signal(
                    tempZip,
                    dateTime.toLocalDate(),
                    "",              // comments initially empty
                    patientId,
                    samplingRate
            );

            server.getAppMain().medicalManager.getSignalJDBC().insertSignal(record);

            response.addProperty("status", "SUCCESS");
            response.addProperty("message", "Signal uploaded correctly");
            sendRawJson(response);



    }


    /**
     * Sends the Server's public key as a JSON Object to follow the protocol. Adds a "type" field to tell the client
     * what kind of message it is and encodes the server's public key from binary into a Base64 String into the "data"
     * field. This makes it safe to send over a text stream.
     */
    public void sendPublicKey() {
        JsonObject serverKey = new JsonObject();
        serverKey.addProperty("type", "SERVER_PUBLIC_KEY");
        serverKey.addProperty("data", Base64.getEncoder().encodeToString(serverKeyPair.getPublic().getEncoded()));
        sendRawJson(serverKey);
    }

    /**
     * Server-side forced shutdown
     */
    public void forceShutdown(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "STOP_CLIENT");
        sendRawJson(jsonObject);
        running.set(false);
        try {
            socket.close(); //this will unblock readline() in run()
            System.out.println("Socktet closed"+socket.getInetAddress());
            server.removeClient(this);
        }catch (IOException e){
        }
    }

    public boolean isStopped(){
        return !running.get();
    }

    void releaseResources(BufferedReader bufferedReader, BufferedWriter bufferedWriter, Socket clientSocket) throws IOException {
        server.removeClient(this);
        running.set(false);
        try {if (bufferedReader!=null) bufferedReader.close();} catch (IOException ex) {System.out.println("Error closing socket"+ex.getMessage());}
        try {if(bufferedWriter!=null)bufferedWriter.close();} catch (IOException ex) {System.out.println("Error closing socket"+ex.getMessage());}
        try {if(clientSocket!=null && !clientSocket.isClosed())clientSocket.close();} catch (IOException ex) {System.out.println("Error closing socket"+ex.getMessage());}
    }

    public String getSocketAddress(){
        return socket.getInetAddress().toString();
    }

    /**
     * Converts the JsonObject into a raw JSON string and writes it to the client's output stream.
     * It immediately sends the data instead of buffering it.
     *
     * @param json  The JsonObject that will be converted it into a raw JSON string
     */
    private void sendRawJson(JsonObject json){
        try {
            out.write(gson.toJson(json));
            out.newLine();
            out.flush();
        } catch (IOException e) {}
    }

    /// If login success, message format:
    /// {
    ///   "type": "LOGIN_RESPONSE",
    ///   "status": "SUCCESS",
    ///   "user": {
    ///     "id": 1,
    ///     "email": "juan@demo.com",
    ///     "role": "patient"
    ///   }
    /// }
    /// If login failed, message format:
    /// {
    ///   "type": "LOGIN_RESPONSE",
    ///   "status": "ERROR",
    ///   "message": "Invalid credentials"
    /// }
    private void handleLogIn(JsonObject data) throws IOException {
        String email = data.get("email").getAsString();
        String password = data.get("password").getAsString();
        String accessPermits = data.get("access_permits").getAsString();

        JsonObject response = new JsonObject();
        response.addProperty("type", "LOGIN_RESPONSE");

        if (server.getAppMain().userJDBC.isUser(email)) {
            User user = server.getAppMain().userJDBC.login(email, password);

            if (user != null) {
                Role role = server.getAppMain().securityManager.getRoleJDBC().findRoleByID(user.getRole_id());
                if(role != null && role.getRolename().equals(accessPermits)) {
                    if(user.isActive()){
                        response.addProperty("status", "SUCCESS");
                        JsonObject userObj = new JsonObject();
                        userObj.addProperty("id", user.getId());
                        userObj.addProperty("email", user.getEmail());
                        userObj.addProperty("role", role.getRolename());
                        response.add("data", userObj);
                    }else {
                        response.addProperty("status", "ERROR");
                        response.addProperty("message", "The user is no longer active.");
                    }

                }else{
                    response.addProperty("status", "ERROR");
                    response.addProperty("message", "Authorization denied");
                }
            } else {
                response.addProperty("status", "ERROR");
                response.addProperty("message", "Invalid password");
            }
        } else {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "User not found");
        }

        System.out.println(response.toString());
        sendRawJson(response);
    }

    private void handleRequestDoctorByEmail(JsonObject dataIn) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("type", "REQUEST_DOCTOR_BY_EMAIL_RESPONSE");

        String email = dataIn.get("email").getAsString();
        Integer user_id = dataIn.get("user_id").getAsInt();
        User user = server.getAppMain().userJDBC.findUserByID(user_id);
        if(user == null){
            response.addProperty("status", "ERROR");
            response.addProperty("message", "User not found");
            sendRawJson(response);
            return;
        }

        Role role =  server.getAppMain().securityManager.getRoleJDBC().findRoleByID(user.getRole_id());
        if(role==null || !role.getRolename().equals("Doctor") || !email.equals(user.getEmail())) {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Not authorized");
            sendRawJson(response);
            return;
        }

        Doctor doctor = server.getAppMain().doctorJDBC.findDoctorByEmail(email);
        if(doctor != null) {
            response.addProperty("status", "SUCCESS");
            response.add("doctor", doctor.toJason());
        }else{
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Doctor not found");
        }
        //TODO: ENCRYPTION
        sendEncrypted(response, out, AESkey);

    }

    /**
     * Both the Doctor, Admin and the Patient whose Doctor is the requested doctor should have access
     * Each Doctor can only access their own info, not other doctor's info
     * @param data
     * @throws IOException
     */
    private void handleRequestDoctorById(JsonObject data) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("type", "REQUEST_DOCTOR_BY_ID_RESPONSE");

        int doctor_id = data.get("doctor_id").getAsInt();
        int user_id = data.get("user_id").getAsInt();
        User user = server.getAppMain().userJDBC.findUserByID(user_id);
        if(user == null){
            response.addProperty("status", "ERROR");
            response.addProperty("message", "User not found");
            sendRawJson(response);
            return;
        }

        Role role =  server.getAppMain().securityManager.getRoleJDBC().findRoleByID(user.getRole_id());
        if(role == null){
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Role not found");
            sendRawJson(response);
            return;
        }
        //If the patient is requesting the Doctor info of a Doctor that is not theirs, don't authorize the access
        if(role.getRolename().equals("Patient")){
            Patient patient = server.getAppMain().patientJDBC.findPatientByEmail(user.getEmail());
            if(patient.getDoctorId() != doctor_id){
                response.addProperty("status", "ERROR");
                response.addProperty("message", "Not authorized");
                sendRawJson(response);
                return;
            }
        }

        Doctor doctor = server.getAppMain().doctorJDBC.getDoctor(doctor_id);
        if(doctor != null) {
            if(role.getRolename().equals("Doctor")&& !doctor.getEmail().equals(user.getEmail())) {
                //If the doctor requesting for info is a different doctor
                response.addProperty("status", "ERROR");
                response.addProperty("message", "Not authorized");
                sendRawJson(response);
                return;
            }
            response.addProperty("status", "SUCCESS");
            response.add("doctor", doctor.toJason());

        }else{
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Doctor not found");
        }
        sendRawJson(response);
    }

    private void handleRequestPatientByEmail(JsonObject data) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("type", "REQUEST_PATIENT_BY_EMAIL_RESPONSE");

        String email = data.get("email").getAsString();
        Integer user_id = data.get("user_id").getAsInt();
        User user = server.getAppMain().userJDBC.findUserByID(user_id);
        if(user == null || !user.getEmail().equals(email)) {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Not authorized");
            sendRawJson(response);
            return;
        }

        Patient patient = server.getAppMain().patientJDBC.findPatientByEmail(email);
        if(patient != null) {
            response.addProperty("status", "SUCCESS");
            List<Signal> signals = server.getAppMain().medicalManager.getSignalJDBC().getSignalsByPatientId(patient.getId());
            List<Report> symptoms = server.getAppMain().medicalManager.getReportJDBC().getReportsByPatientId(patient.getId());
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
            pJson.add("reports", symptomsArray);
            response.add("patient", pJson);
            System.out.println(pJson.toString());
        }else{
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Doctor not found");
        }
        sendRawJson(response);
    }

    private void handleRequestPatientsFromDoctor(JsonObject data) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("type", "REQUEST_PATIENTS_FROM_DOCTOR_RESPONSE");

        Integer doctorId = data.get("doctor_id").getAsInt();
        Integer user_id = data.get("user_id").getAsInt();
        User user = server.getAppMain().userJDBC.findUserByID(user_id);
        Doctor doctor = server.getAppMain().doctorJDBC.getDoctor(doctorId);

        if(user != null && doctor != null && doctor.getEmail().equals(user.getEmail())) {

            response.addProperty("status", "SUCCESS");
            List<Patient> patients = server.getAppMain().medicalManager.getPatientJDBC().getPatientsOfDoctor(doctorId);

            JsonArray patientArray = new JsonArray();
            for (Patient p : patients) {
                List<Signal> signals = server.getAppMain().medicalManager.getSignalJDBC().getSignalsByPatientId(p.getId());
                List<Report> symptoms = server.getAppMain().medicalManager.getReportJDBC().getReportsByPatientId(p.getId());
                JsonObject pJson = p.toJason();
                JsonArray signalArray = new JsonArray();
                for (Signal s : signals) {
                    signalArray.add(s.toJson());
                }
                JsonArray symptomsArray = new JsonArray();
                for (Report s : symptoms) {
                    symptomsArray.add(s.toJson());
                }
                pJson.add("signals", signalArray);
                pJson.add("reports", symptomsArray);

                patientArray.add(pJson);
            }

            response.add("patients", patientArray);

        }else {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Not authorized");
        }

        sendRawJson(response);
    }


    private void handleSaveCommentsSignal(JsonObject data) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("type", "SAVE_COMMENTS_SIGNAL_RESPONSE");

        String comments = data.get("comments").getAsString();
        Integer user_id = data.get("user_id").getAsInt();
        Integer signal_id = data.get("signal_id").getAsInt();
        Integer patient_id = data.get("patient_id").getAsInt();

        User user = server.getAppMain().userJDBC.findUserByID(user_id);
        if(user == null) {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Not authorized");
            sendRawJson(response);
            return;
        }

        //If the user is the doctor
        Doctor doctor = server.getAppMain().doctorJDBC.findDoctorByEmail(user.getEmail());
        //And the patient_id is a patient of them
        Doctor doctor1 = server.getAppMain().doctorJDBC.getDoctorFromPatient(patient_id);

        if(doctor!= null && doctor1 != null && doctor1.getId() == doctor.getId()) {
            if(server.getAppMain().medicalManager.getSignalJDBC().updateSignalComments(signal_id, comments)) {
                response.addProperty("status", "SUCCESS");
            }else{
                response.addProperty("status", "ERROR");
                response.addProperty("message", "Error saving comments");
            }
        }else {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Not authorized");
        }

        sendRawJson(response);
    }

    private void handleSaveReportRequest(JsonObject data) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("type", "SAVE_REPORT_RESPONSE");

        Integer user_id = data.get("user_id").getAsInt();
        Integer patient_id = data.get("patient_id").getAsInt();
        Report report = Report.fromJson(data.get("report").getAsJsonObject());
        if(report == null) {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Error parsing report");
            sendRawJson(response);
            return;
        }

        User user = server.getAppMain().userJDBC.findUserByID(user_id);
        if(user == null) {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Not authorized");
            sendRawJson(response);
            return;
        }

        Patient patient = server.getAppMain().patientJDBC.findPatientByEmail(user.getEmail());
        if(patient == null) {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Not authorized");
            sendRawJson(response);
            return;
        }

        if(patient.getEmail().equals(user.getEmail())) {
            report.setPatientId(patient_id);
            if(server.getAppMain().medicalManager.getReportJDBC().insertReport(report)) {
                response.addProperty("status", "SUCCESS");
            }else{
                response.addProperty("status", "ERROR");
                response.addProperty("message", "Error saving comments");
            }
        }else {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Not authorized");
        }

        sendRawJson(response);
    }

    /**
     * Encrypts the Json's data property.
     *
     * @param message
     * @param out
     * @param AESkey
     */
    public void sendEncrypted(JsonObject message, BufferedWriter out, SecretKey AESkey){
        try{
            String encryptedJson = AESUtil.encrypt(message.toString(), AESkey);
            JsonObject wrapper = new JsonObject();

            //TODO: ver si realmente el type debería ser especifico para cada case o no
            wrapper.addProperty("type", "ENCRYPTED");
            wrapper.addProperty("data", encryptedJson);

            System.out.println("This is the message:"+wrapper);

            out.write(gson.toJson(wrapper)); //String Json
            out.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

