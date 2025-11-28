package network;

import encryption.RSAKeyManager;
import org.example.service.AdminLinkService;
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
    // List implementation for lock-free thread-safe concurrency. Ideal for multithreaded apps with many reads and few modifications.
    private final CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();
// private List<ClientHandler> clients;
    private volatile Boolean running = false; // So other threads can immediately see changes to this variable
// private final Application appMain; // To access the centralized medicalManager and securityManager
    private KeyPair keyPair; // To store the public and private RSA keys (asymmetric encryption)
    private AdminLinkService adminConn;


    /**
     * Creates a new server instance on the given port, stores the AdminLinkService
     * reference, and generates the RSA key pair for secure communication.
     * @param port
     * @param adminConn
     */
    public Server(int port, AdminLinkService adminConn) {
        this.port = port;
        this.adminConn = adminConn;
        // Creates the key pair for public encryption
        try {
            this.keyPair = RSAKeyManager.generateKeyPair();
        }catch (Exception e){
            System.out.println("Error generating key pair: "+e.getMessage());
        }
    }


    /**
     * Test-only constructor that injects a predefined ServerSocket and initializes
     * the RSA key pair without binding to a real port.
     * @param serverSocket
     * @param adminConn
     * @throws Exception
     */
    public Server(ServerSocket serverSocket, AdminLinkService adminConn) throws Exception{
        this.serverSocket = serverSocket;
        this.port = -1; // unused
        this.keyPair = RSAKeyManager.generateKeyPair();
        this.adminConn = adminConn;
    }

    /**
     * Starts the server in a dedicated thread, opens the ServerSocket,
     * accepts incoming client connections, and creates a new ClientHandler
     * thread for each connected client.
     */
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

    /**
     * Returns a list of human-readable strings describing all currently
     * connected clients and their socket addresses.
     */

    public ArrayList<String> getConnectedClients() {
        ArrayList<String>  connectedClients = new ArrayList<>();
        for (int i = 0; i < clients.size(); i++) {
            connectedClients.add("Client "+(i+1)+": "+clients.get(i).getSocketAddress());
        }
        return connectedClients;
    }

    /**
     * Removes the given ClientHandler from the active client list and logs
     * the updated client count.
     */

    public void removeClient(ClientHandler handler) {
        clients.remove(handler);
        System.out.println("Client removed from list. Total: " + clients.size());
    }

    public AdminLinkService getAdminLinkService() {
        return adminConn;
    }

}


