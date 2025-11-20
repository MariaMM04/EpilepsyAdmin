package network;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.example.entities_medicaldb.*;
import org.example.entities_securitydb.*;
import Exceptions.*;
import ui.RandomData;
import ui.windows.Application;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {
    final Socket socket;
    private final Server server;
    BufferedReader in;
    private BufferedWriter out;
    private final Gson gson = new Gson();;
    //Asegura que los cambios en la variable se realizan sin interferencia de otros hilos. Evitar race conditions
    private AtomicBoolean running;

    public ClientHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        running = new AtomicBoolean(true);
    }

    @Override
    public void run(){
        try {
            String line;
            label:
            while (running.get() && (line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {continue;} //Skip empty lines

                JsonObject request;
                try {
                   request = gson.fromJson(line, JsonObject.class);
                }catch (JsonSyntaxException e){
                    System.out.println(line);
                    continue;
                }
                if (request == null) {continue;}

                String type = request.get("type").getAsString();

                switch (type) {
                    case "STOP_CLIENT":
                        //Client asked to stop itself or server asked client to stop and client echoes
                        System.out.println("Received STOP_CLIENT from"+getSocketAddress());
                        releaseResources(in, out, socket);;
                        break;
                    case "LOGIN_REQUEST": {
                        System.out.println("LOGIN REQUEST");
                        handleLogIn(request.getAsJsonObject("data"));
                        break;
                    }
                    case "REQUEST_DOCTOR_BY_EMAIL": {
                        System.out.println("REQUEST DOCTOR_BY_EMAIL");
                        handleRequestDoctorByEmail(request.getAsJsonObject("data"));
                        break;
                    }
                    case "REQUEST_PATIENTS_FROM_DOCTOR": {
                        System.out.println("REQUEST_PATIENTS_FROM_DOCTOR");
                        handleRequestPatientsFromDoctor(request.getAsJsonObject("data"));
                        break;
                    }
                    case "REQUEST_PATIENT_BY_EMAIL": {
                        System.out.println("REQUEST_PATIENT_BY_EMAIL");
                        handleRequestPatientByEmail(request.getAsJsonObject("data"));
                        break;
                    }
                    case "REQUEST_DOCTOR_BY_ID": {
                        System.out.println("REQUEST DOCTOR_BY_ID");
                        handleRequestDoctorById(request.getAsJsonObject("data"));
                        break;
                    }
                    case "SAVE_COMMENTS_SIGNAL": {
                        System.out.println("SAVE_COMMENTS_SIGNAL");
                        handleSaveCommentsSignal(request.getAsJsonObject("data"));
                        break;
                    }
                }

            }
        } catch (IOException e){
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
        return running.get();
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

        sendRawJson(response);
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
        //If the patient is requesting the Doctor info of a Doctor that is not their's, don't authorize the access
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
            response.add("patient", patient.toJason());
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
                patientArray.add(p.toJason());
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
}

