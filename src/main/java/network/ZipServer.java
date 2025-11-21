package network;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ZipServer {

    private final int port;
    private File lastReceivedZip;
    private volatile boolean running = true;

    public ZipServer(int port) {
        this.port = port;
    }

    public File getLastReceivedZip() {
        return lastReceivedZip;
    }

    // Servidor test-friendly: recibe SOLO UN ZIP y termina
    public void startAndWaitForOneZip() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);

            Socket socket = serverSocket.accept();
            System.out.println("Client connected!");

            File tempZip = File.createTempFile("received_", ".zip");
            tempZip.deleteOnExit();

            try (InputStream in = socket.getInputStream();
                 FileOutputStream fos = new FileOutputStream(tempZip)) {

                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = in.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            this.lastReceivedZip = tempZip;
            System.out.println("ZIP received OK.");

            socket.close();
            running = false;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
