package network;

import com.google.gson.Gson;
import org.example.entities_medicaldb.Signal;
import ui.SignalMetadataDTO;
import network.SignalRecordingDAO;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMetadata {

    private final int port=9100;

    public void startForever() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Metadata server listening on " + port);

            Gson gson = new Gson();

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Metadata connection received.");

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                );

                String json = reader.readLine();
                SignalMetadataDTO dto = new SignalMetadataDTO();

                // Construcción del registro
                Signal record = new Signal(dto.signalId, dto.date,dto.comments,dto.patientId, dto.samplingFrequency);
                new SignalRecordingDAO().saveSignal(record);

                System.out.println("Metadata saved for signalId = " + dto.signalId);

                socket.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ServerMetadata().startForever();
    }
}