package network;

import org.example.entities_medicaldb.Doctor;
import org.example.entities_medicaldb.Patient;
import ui.RandomData;
import ui.windows.Application;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {
    private int port;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private Application application;

    public Server(int port) {
        this.port = port;
    }

    @Override
    public void run() {

        try{
            serverSocket = new ServerSocket(port);
            System.out.println("Server listening on port " + port);
            clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress());
            handleMessages(clientSocket);
            // You can spawn a new thread here for each client

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleMessages(Socket clientSocket){
        try {
            bufferedReader = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));

            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(clientSocket.getOutputStream())
            );
            System.out.println("Text Received:\n");
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.toLowerCase().contains("stop")) {
                    System.out.println("Stopping the server");
                    releaseResources(bufferedReader, clientSocket, serverSocket);
                    //System.exit(0);
                    System.out.println("Server stopped");
                    break;
                }else if(line.toLowerCase().contains("get_patient")) {
                    Patient patient = RandomData.generateRandomPatient();
                    // Convert patient to string and send
                    writer.write(patient.toString());
                    writer.newLine(); // mark end of line
                    writer.flush();   // send immediately
                }else if(line.toLowerCase().contains("get_doctor")) {
                    Doctor doctor = RandomData.generateRandomDoctor();
                    // Convert patient to string and send
                    writer.write(doctor.toString());
                    writer.newLine(); // mark end of line
                    writer.flush();   // send immediately
                }
                System.out.println(line);
            }
        }catch (IOException e){
            System.out.println("Error reading from client"+e.getMessage());
        }
    }


    private static void releaseResources(BufferedReader bufferedReader,
                                         Socket socket, ServerSocket socketServidor) {
        try {
            bufferedReader.close();
        } catch (IOException ex) {
            System.out.println("Error closing socket"+ex.getMessage());
        }

        try {
            socket.close();
        } catch (IOException ex) {
            System.out.println("Error closing socket"+ex.getMessage());
        }

        try {
            socketServidor.close();
        } catch (IOException ex) {
            System.out.println("Error closing socket"+ex.getMessage());
        }
    }

}
