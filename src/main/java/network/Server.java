package network;

import org.example.entities_medicaldb.Doctor;
import org.example.entities_medicaldb.Patient;
import ui.RandomData;
import ui.windows.Application;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//DUDAS:
// se crea un thread para cada cliente conectado verdad? no para el servidor
// dónde ejecutar el main? Puedo añadir en Application una instancia de Server para poder llamar a sus métodos?
//      o al revés? pero los eventos los lanza la interfaz...
//      pero para ejecutar el main en Application, el server no debería ser runnable? para poder estar esperando
//      tod el rato a nuevos clientes en paralelo a la ejecución de la app.

public class Server {
    private int port;
    private ServerSocket serverSocket;
    private List<ClientHandler> clients;
    private boolean running = false;

    public Server(int port) {
        this.port = port;
        clients = new ArrayList<>();
    }

    public void start(){
        if (running) return;  //to avoid it starting 2 times

        running = true;
        //create its own thread to listen for new clients
        Thread serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                System.out.println("Server started in port " + port);

                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler handler = new ClientHandler(clientSocket, this);
                    clients.add(handler);
                    new Thread(handler).start(); //Start client thread
                    //executor.submit(handler);
                    System.out.println("New client connected. Total: " + clients.size());
                }

            } catch (IOException e) {
                if (running) e.printStackTrace(); // ignorar si fue detenido
            }
        });

        serverThread.start();
    }

    public void stop() throws ClientsStillConnectedException{
        if (!running) return;
        running = false;
        try {
            if(!clients.isEmpty()){
                throw new ClientsStillConnectedException("There are still"+clients.size()+" clients connected");
            }
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {}
        clients.clear();
        System.out.println("Server stopped");
    }

    public int checkConnectedClients() {
        return clients.size();
    }

    public void removeClient(ClientHandler handler) {
        clients.remove(handler);
        System.out.println("Client disconnected. Total: " + clients.size());
    }

    public static class ClientsStillConnectedException extends Exception {
        public ClientsStillConnectedException(String message) {
            super(message);
        }
    }

}


