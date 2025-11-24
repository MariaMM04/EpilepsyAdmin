package network;

import encryption.RSAKeyManager;
import ui.windows.Application;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import Exceptions.*;

/**
 *
 */
public class Server {
    private int port;
    private ServerSocket serverSocket;
    //Implementación de List para concurrencia segura sin bloqueos. Ideal para apps multihilos con muchas consultas y pocas modificaciones
    private final CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();
    //private List<ClientHandler> clients;
    private volatile Boolean running = false; //Para que otros hilos vean directamente si hay cambios en ella
    private final Application appMain; //To access the centralized medicalManager and securityManager
    private final KeyPair keyPair; //To have a public and private key (RSA asymmetric encryption)

    public Server(int port,  Application appMain) throws Exception {
        this.port = port;
        //clients = new ArrayList<>();
        this.appMain = appMain;
        // Creates the key pair for public encryption
        this.keyPair = RSAKeyManager.generateKeyPair();
    }

    // TEST constructor
    public Server(ServerSocket serverSocket, Application appMain) throws Exception{
        this.serverSocket = serverSocket;
        this.port = -1; // unused
        this.appMain = appMain;
        this.keyPair = RSAKeyManager.generateKeyPair();
    }

    public void startServer(){
        if (running) return;  //to avoid it starting 2 times

        running = true;
        //create its own thread to listen for new clients
        Thread serverThread = new Thread(() -> {
            try {
                serverSocket= createServerSocket(port);
                System.out.println("Server started on port " + port);

                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected from IP: "+clientSocket.getInetAddress().getHostAddress());
                    //New client with the server's public key
                    ClientHandler handler = new ClientHandler(clientSocket, this, keyPair);
                    clients.add(handler);
                    new Thread(handler).start(); //Start client thread
                    System.out.println("New client connected. Total: " + clients.size());
                }

            } catch (IOException e) {
                if (running) e.printStackTrace(); // ignorar si fue detenido
            }finally {
                //cleanup server
                try{
                    if(serverSocket!=null && serverSocket.isClosed()){serverSocket.close();}
                }catch (IOException e){}
            }
        });
        serverThread.start();
    }

    ServerSocket createServerSocket(int port) throws IOException {
        return new ServerSocket(port);
    }

    public boolean isRunning(){
        return running;
    }

    /**
     * Force-stop all clients (server-initiated shutdown).
     * This method attempts to close client sockets, then waits briefly for handlers to finish.
     * @throws ClientError
     */
    public void closeAllClients() throws ClientError {
        System.out.println("Requesting shutdown of all clients ("+clients.size()+")");
        for (ClientHandler client : clients) {
            client.forceShutdown(); //closes sockets and sets runnin=false on the handlers
        }
        System.out.println("After closeAllClients, remaining handlers: " + clients.size());
        if(!clients.isEmpty()){
            throw new ClientError(clients.size()+" clients still connected");
        }
    }

    /**
     * Stop server: attemt to stop clients first, then stop accepting new connections.
     * @throws ClientError
     */
    public void stop() throws ClientError {
        if (!running) return;
        running = false;
        try {
            //Stop accepting new clients
            if (serverSocket != null) {
                serverSocket.close();
            }

            closeAllClients();
            clients.clear();
            System.out.println("Server stopped");

        } catch (IOException e) {
            throw new ClientError("Error closing server socket");
        }
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
        System.out.println("Client removed from list. Total: " + clients.size());
    }

    public Application getAppMain() {
        return appMain;
    }

}


