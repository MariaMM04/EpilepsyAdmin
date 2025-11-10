package network;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.example.entities_medicaldb.Doctor;
import org.example.entities_medicaldb.Patient;
import org.example.entities_securitydb.Role;
import org.example.entities_securitydb.User;
import ui.RandomData;
import ui.windows.Application;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//TODO: cómo gestionar los permisos de cada cliente?
// Cómo identificar quién es un paciente y quién es un doctor?
public class ClientHandler implements Runnable {
    private Socket socket;
    private Server server;
    private BufferedReader in;
    private BufferedWriter out;
    private Gson gson;

    public ClientHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        gson = new Gson();
    }

    @Override
    public void run() {
        try {
            String line;
            Gson gson = new Gson();
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    // Skip empty lines
                    continue;
                }

                JsonObject request = null;
                try {
                   request = gson.fromJson(line, JsonObject.class);
                }catch (JsonSyntaxException e){
                    System.out.println(line);
                    continue;
                }

                if (request == null) {
                    System.out.println("Received null JSON, skipping: " + line);
                    continue;
                }

                String type = request.get("type").getAsString();

                if(type.equals("STOP_CLIENT")) {
                    System.out.println("Stopping the client thread");
                    releaseResources(in, out, socket);
                    System.out.println("Client thread stopped");
                    break;
                } else if (type.equals("LOGIN_REQUEST")) {
                    System.out.println("LOGIN REQUEST");
                    JsonObject data = request.getAsJsonObject("data");
                    handleLogIn(data);
                } else if (type.equals("REQUEST_DOCTOR_BY_EMAIL")) {
                    System.out.println("REQUEST DOCTOR_BY_EMAIL");
                    JsonObject data = request.getAsJsonObject("data");
                    handleRequestDoctorByEmail(data);
                } else if (type.equals("REQUEST_PATIENTS_FROM_DOCTOR")) {
                    System.out.println("REQUEST_PATIENTS_FROM_DOCTOR");
                    JsonObject data = request.getAsJsonObject("data");
                    handleRequestPatientsFromDoctor(data);
                } else if (type.equals("REQUEST_PATIENT_BY_EMAIL")) {
                    System.out.println("REQUEST_PATIENT_BY_EMAIL");
                    JsonObject data = request.getAsJsonObject("data");
                    handleRequestPatientByEmail(data);
                }   else if (type.equals("REQUEST_DOCTOR_BY_ID")) {
                    System.out.println("REQUEST DOCTOR_BY_ID");
                    JsonObject data = request.getAsJsonObject("data");
                    handleRequestDoctorById(data);
                }else if (type.equals("SAVE_COMMENTS_SIGNAL")) {
                    System.out.println("SAVE_COMMENTS_SIGNAL");
                    JsonObject data = request.getAsJsonObject("data");
                    handleSaveCommentsSignal(data);
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
                System.out.println("Error reading from client"+e.getMessage());
            }

        }
    }

    private void releaseResources(BufferedReader bufferedReader, BufferedWriter bufferedWriter, Socket clientSocket) throws IOException {
        server.removeClient(this);
        try {
            bufferedReader.close();
            bufferedWriter.close();
            socket.close();
        } catch (IOException ex) {
            System.out.println("Error closing socket"+ex.getMessage());
        }

    }

    public String getSocketAddress(){
        return socket.getRemoteSocketAddress().toString();
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

        JsonObject response = new JsonObject();
        response.addProperty("type", "LOGIN_RESPONSE");
        String accessPermits = data.get("access_permits").getAsString();

        if (server.appMain.userJDBC.isUser(email)) {
            User user = server.appMain.userJDBC.login(email, password);
            System.out.println(user);

            if (user != null) {
                Role role = server.appMain.securityManager.getRoleJDBC().findRoleByID(user.getRole_id());
                if(role.getRolename().equals(accessPermits)) {
                    response.addProperty("status", "SUCCESS");
                    JsonObject userObj = new JsonObject();
                    userObj.addProperty("id", user.getId());
                    userObj.addProperty("email", user.getEmail());
                    //TODO: Add data property
                    //TODO: check roleName == access_permits
                    userObj.addProperty("role", role.getRolename());
                    response.add("data", userObj);
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

        out.write(gson.toJson(response));
        out.newLine();
        out.flush();
    }

    private void handleRequestDoctorByEmail(JsonObject data) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("type", "REQUEST_DOCTOR_BY_EMAIL_RESPONSE");

        String email = data.get("email").getAsString();
        Integer user_id = data.get("user_id").getAsInt();
        User user = server.appMain.userJDBC.findUserByID(user_id);
        Role role =  server.appMain.securityManager.getRoleJDBC().findRoleByID(user.getRole_id());

        if(user != null && email.equals(user.getEmail()) && role.getRolename().equals("Doctor")) {
            Doctor doctor = server.appMain.doctorJDBC.findDoctorByEmail(email);
            if(doctor != null) {
                response.addProperty("status", "SUCCESS");
                //TODO: Add data section
                JsonObject doctorObj = doctor.toJason();
                response.add("doctor", doctorObj);
            }else{
                response.addProperty("status", "ERROR");
                response.addProperty("message", "Doctor not found");
            }
        }else {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Not authorized");
        }

        out.write(gson.toJson(response));
        out.newLine();
        out.flush();
    }

    private void handleRequestDoctorById(JsonObject data) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("type", "REQUEST_DOCTOR_BY_ID_RESPONSE");

        Integer doctor_id = data.get("doctor_id").getAsInt();
        Integer patient_id = data.get("patient_id").getAsInt();
        Integer user_id = data.get("user_id").getAsInt();
        User user = server.appMain.userJDBC.findUserByID(user_id);
        Role role =  server.appMain.securityManager.getRoleJDBC().findRoleByID(user.getRole_id());
        Patient patient = server.appMain.patientJDBC.findPatientByID(patient_id);

        if(user != null && patient != null && patient.getEmail().equals(user.getEmail()) && role.getRolename().equals("Patient")) {
            Doctor doctor = server.appMain.doctorJDBC.findDoctorById(doctor_id);
            if(doctor != null) {
                response.addProperty("status", "SUCCESS");
                JsonObject doctorObj = doctor.toJason();
                response.add("doctor", doctorObj);
            }else{
                response.addProperty("status", "ERROR");
                response.addProperty("message", "Doctor not found");
            }
        }else {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Not authorized");
        }

        out.write(gson.toJson(response));
        out.newLine();
        out.flush();
    }

    private void handleRequestPatientByEmail(JsonObject data) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("type", "REQUEST_PATIENT_BY_EMAIL_RESPONSE");

        String email = data.get("email").getAsString();
        Integer user_id = data.get("user_id").getAsInt();
        User user = server.appMain.userJDBC.findUserByID(user_id);

        if(user != null && email.equals(user.getEmail())) {
            Patient patient = server.appMain.patientJDBC.findPatientByEmail(email);
            if(patient != null) {
                response.addProperty("status", "SUCCESS");
                JsonObject doctorObj = patient.toJason();
                response.add("patient", doctorObj);
            }else{
                response.addProperty("status", "ERROR");
                response.addProperty("message", "Doctor not found");
            }
        }else {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Not authorized");
        }
        out.write(gson.toJson(response));
        out.newLine();
        out.flush();
    }

    private void handleRequestPatientsFromDoctor(JsonObject data) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("type", "REQUEST_PATIENTS_FROM_DOCTOR_RESPONSE");

        Integer doctorId = data.get("doctor_id").getAsInt();
        Integer user_id = data.get("user_id").getAsInt();
        User user = server.appMain.userJDBC.findUserByID(user_id);
        Doctor doctor = server.appMain.doctorJDBC.getDoctor(doctorId);

        if(user != null && doctor != null && doctor.getEmail().equals(user.getEmail())) {

            response.addProperty("status", "SUCCESS");
            List<Patient> patients = server.appMain.medicalManager.getPatientJDBC().getPatientsOfDoctor(doctorId);

            //TODO; Add data section
            JsonArray patientArray = new JsonArray();
            for (Patient p : patients) {
                patientArray.add(p.toJason());
            }

            //change property by data
            response.add("patients", patientArray);

        }else {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Not authorized");
        }

        out.write(gson.toJson(response));
        out.newLine();
        out.flush();
    }


    private void handleSaveCommentsSignal(JsonObject data) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("type", "SAVE_COMMENTS_SIGNAL_RESPONSE");

        String comments = data.get("comments").getAsString();
        Integer user_id = data.get("user_id").getAsInt();
        Integer signal_id = data.get("signal_id").getAsInt();
        Integer patient_id = data.get("patient_id").getAsInt();

        //If its the doctor
        User user = server.appMain.userJDBC.findUserByID(user_id);
        Doctor doctor = server.appMain.doctorJDBC.findDoctorByEmail(user.getEmail());
        //And this is their patient
        Doctor doctor1 = server.appMain.doctorJDBC.getDoctorFromPatient(patient_id);

        if(user != null && doctor.getEmail().equals(user.getEmail()) && doctor1.equals(doctor)) {
            if(server.appMain.medicalManager.getSignalJDBC().updateSignalComments(signal_id, comments)) {
                response.addProperty("status", "SUCCESS");
            }else{
                response.addProperty("status", "ERROR");
                response.addProperty("message", "Error saving comments");
            }
        }else {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Not authorized");
        }

        out.write(gson.toJson(response));
        out.newLine();
        out.flush();
    }
}
