package network;

import org.example.entities_medicaldb.Doctor;
import org.example.entities_medicaldb.Patient;
import ui.RandomData;

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

    public ClientHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }
    @Override
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
                } else{
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

}
