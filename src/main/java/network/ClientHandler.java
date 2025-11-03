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


    /*@Override
    public void run() {
        System.out.println("Thread started for client: " + socket.getRemoteSocketAddress());
        System.out.println("Text Received:\n");
        String line;
        try {
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.toLowerCase().contains("stop")) {
                    System.out.println("Stopping the client thread");
                    releaseResources(in, out, socket);
                    //System.exit(0);
                    System.out.println("Client thread stopped");
                    break;
                } else if (line.toLowerCase().contains("get_patient")) {
                    Patient patient = RandomData.generateRandomPatient();
                    // Convert patient to string and send
                    out.write(patient.toString());
                    out.newLine(); // mark end of line
                    out.flush();   // send immediately
                    System.out.println("Patient sent: " + patient.toString());
                } else if (line.toLowerCase().contains("get_doctor")) {
                    Doctor doctor = RandomData.generateRandomDoctor();
                    // Convert patient to string and send
                    out.write(doctor.toString());
                    out.newLine(); // mark end of line
                    out.flush();   // send immediately
                    System.out.println("Doctor sent: " + doctor.toString());
                }else if (line.toLowerCase().contains("login")) {
                    System.out.println(line);
                    handleLogIn(line);
                }
                else{
                    System.out.println(line);
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

    }*/

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

        if (server.appMain.userJDBC.isUser(email)) {
            User user = server.appMain.userJDBC.login(email, password);
            if (user != null) {
                response.addProperty("status", "SUCCESS");
                JsonObject userObj = new JsonObject();
                userObj.addProperty("id", user.getId());
                userObj.addProperty("email", user.getEmail());
                Role role = server.appMain.securityManager.getRoleJDBC().findRoleByID(user.getRole_id());
                userObj.addProperty("role", role.getRolename());
                response.add("user", userObj);

                /*if(role.getRolename().equals("Patient")) {
                    Patient patient = server.appMain.patientJDBC.findPatientByEmail(user.getEmail());
                    response.add("patient", patient.toJason());

                    Doctor doctor = server.appMain.doctorJDBC.getDoctor(patient.getDoctorId());
                    response.add("doctor", doctor.toJason());
                }else if(role.getRolename().equals("Doctor")) {
                    Doctor doctor = server.appMain.doctorJDBC.findDoctorByEmail(user.getEmail());
                    response.add("doctor", doctor.toJason());
                }*/
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

            JsonArray patientArray = new JsonArray();
            for (Patient p : patients) {
                patientArray.add(p.toJason());
            }

            response.add("patients", patientArray);

        }else {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Not authorized");
        }

        out.write(gson.toJson(response));
        out.newLine();
        out.flush();
    }

    //TODO: test function with database
    private void handleLogIn(String message) throws IOException {
        // Message example: LOGIN;email@example.com;password123
        String[] parts = message.split(";");
        if (parts.length != 3) {
            out.write("LOGIN_FAIL;Invalid format");
            out.newLine();
            out.flush();
            return;
        }

        String email = parts[1];
        String password = parts[2];

        // Check user
        if (!server.appMain.userJDBC.isUser(email)) {
        //if(!testIsUser(email)){
            out.write("LOGIN_FAIL;User not found");
            out.newLine();
            out.flush();
            return;
        }

        // Try login
        User user = server.appMain.userJDBC.login(email, password);
        //User user = testLogIn(email, password);
        if (user != null) {
            out.write("LOGIN_SUCCESS;" + user.toString());
        } else {
            out.write("LOGIN_FAIL;Wrong password");
        }
        out.newLine();
        out.flush();
    }

    private User testLogIn(String email, String password) {
        if(email.equals("test@example.com") && password.equals("1234")) {
            return new User("test@example.com", "1234", true);
        }
        return null;
    }

    private boolean testIsUser(String email){
        if(email.equals("test@example.com")) { //Added to test
            return true;
        }
        return false;
    }
}
