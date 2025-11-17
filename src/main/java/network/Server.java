package network;

import ui.windows.Application;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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
    Application appMain; //To access the centralized medicalManager and securityManager

    public Server(int port,  Application appMain) {
        this.port = port;
        clients = new ArrayList<>();
        this.appMain = appMain;
    }

    public void startServer(){
        if (running) return;  //to avoid it starting 2 times

        running = true;
        //create its own thread to listen for new clients
        Thread serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                System.out.println("Server started in port " + port);

                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected from IP: "+clientSocket.getInetAddress().getHostAddress());
                    ClientHandler handler = new ClientHandler(clientSocket, this);
                    clients.add(handler);
                    new Thread(handler).start(); //Start client thread
                    System.out.println("New client connected. Total: " + clients.size());
                }

            } catch (IOException e) {
                if (running) e.printStackTrace(); // ignorar si fue detenido
            }
        });

        serverThread.start();
    }

    public boolean isRunning(){
        return running;
    }

    public void stop() throws ClientsStillConnectedException{
        if (!running) return;
        try {
            if(!clients.isEmpty()){
                throw new ClientsStillConnectedException("There are still"+clients.size()+" clients connected");
            }
            if (serverSocket != null) {
                serverSocket.close();
                running = false;
            }
        } catch (IOException e) {}
        clients.clear();
        System.out.println("Server stopped");
    }

    public ArrayList<String> getConnectedClients() {
        ArrayList<String>  connectedClients = new ArrayList<>();
        for (int i = 0; i < clients.size(); i++) {
            connectedClients.add("Client "+(i+1)+": "+clients.get(i).getSocketAddress());
        }
        return connectedClients;
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


