package network;

import com.google.gson.Gson;
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
                    JsonObject data = request.getAsJsonObject("data");
                    handleLogIn(data);
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
                String role = "Patient";
                response.addProperty("status", "SUCCESS");
                JsonObject userObj = new JsonObject();
                userObj.addProperty("id", user.getId());
                userObj.addProperty("email", user.getEmail());
                //userObj.addProperty("role", user.getRole());
                userObj.addProperty("role", role); //TODO: change by actual role
                response.add("user", userObj);

                if(role.equals("Patient")) {
                    Patient patient = server.appMain.patientJDBC.findPatientByEmail(user.getEmail());
                    response.add("patient", patient.toJason());

                    Doctor doctor = server.appMain.doctorJDBC.getDoctor(patient.getDoctorId());
                    response.add("doctor", doctor.toJason());
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
