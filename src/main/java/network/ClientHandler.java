package network;

import com.google.gson.*;
import encryption.PasswordHash;
import encryption.TokenUtils;
import encryption.RSAUtil;
import org.example.JDBC.securitydb.UserJDBC;
import org.example.entities_medicaldb.*;
import org.example.entities_securitydb.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
/**
 * Handles all communication between the server and a single connected client.
 * <p>
 * Each {@code ClientHandler} instance runs in its own thread and is responsible for:
 * </p>
 * <ul>
 *     <li>Reading incoming messages from the client's socket</li>
 *     <li>Parsing JSON requests and dispatching them to the appropriate handlers</li>
 *     <li>Managing RSA public-key exchange during the handshake phase</li>
 *     <li>Receiving and decrypting the AES session key used for all encrypted communication</li>
 *     <li>Processing encrypted client requests after token negotiation</li>
 *     <li>Sending encrypted or plaintext responses depending on protocol stage</li>
 *     <li>Tracking connection state and handling client disconnection</li>
 *     <li>Cleaning up server-side resources when the connection closes</li>
 * </ul>
 *
 * <h3>Encryption Workflow</h3>
 * <ol>
 *     <li>Client sends {@code CLIENT_PUBLIC_KEY}</li>
 *     <li>Server responds with its own RSA public key</li>
 *     <li>Client sends {@code TOKEN_REQUEST}</li>
 *     <li>Server generates and RSA-encrypts an AES session key</li>
 *     <li>Client decrypts AES session key and confirms via {@code CLIENT_AES_KEY}</li>
 *     <li>All subsequent communication uses AES-GCM encrypted JSON</li>
 * </ol>
 *
 * <h3>Thread Model</h3>
 * This class implements {@link Runnable}. The {@link #run()} method loops while:
 * <ul>
 *     <li>the socket is open, and</li>
 *     <li>{@code running.get()} is {@code true}</li>
 * </ul>
 * The loop terminates when the client disconnects, or when the server
 * explicitly shuts down this handler.
 *
 * <h3>Failure Handling</h3>
 * Invalid JSON, unexpected message types, or encryption errors do not crash
 * the server. The handler safely cleans up and removes itself from the
 * server’s active client list when needed.
 */

public class ClientHandler implements Runnable {
    final Socket socket;
    private final Server server;
    BufferedReader in;
    private final PrintWriter out;
    private final Gson gson = new Gson();
    //Asegura que los cambios en la variable se realizan sin interferencia de otros hilos. Evitar race conditions
    private AtomicBoolean running;
    private String clientEmail;
    private final KeyPair serverKeyPair; //This is going to be the server's public key
    private PublicKey clientPublicKey; //This is going to be the client's public key
    private SecretKey token;
    /**
     * Creates a new {@code ClientHandler} bound to a single client socket.
     * <p>
     * The constructor:
     * </p>
     * <ul>
     *     <li>Stores references to the server and the client's socket</li>
     *     <li>Initializes input and output streams for communication</li>
     *     <li>Stores the server's RSA key pair for handshake encryption</li>
     *     <li>Initializes an {@link AtomicBoolean} to control thread lifecycle</li>
     * </ul>
     *
     * @param socket        The socket associated with the connected client
     * @param server        The main server instance managing all connections
     * @param serverKeyPair The server's RSA key pair used during handshake and RSA decryption
     * @throws IOException If input or output streams cannot be created
     */

    public ClientHandler(Socket socket, Server server, KeyPair serverKeyPair) throws IOException {
        this.socket = socket;
        this.server = server;
        this.serverKeyPair = serverKeyPair;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        running = new AtomicBoolean(true);
    }

    /**
     * Main execution loop for the client handler thread.
     * <p>
     * This method continuously listens for messages coming from the connected client,
     * parses them as JSON, decrypts them if required, and dispatches them to the
     * appropriate handler method.
     * </p>
     *
     * <h3>Workflow before AES token exchange:</h3>
     * Messages received before the shared AES session key is established must be
     * processed in plaintext. Only the following message types are allowed:
     * <ul>
     *     <li>{@code ACTIVATION_REQUEST} – account activation prior to login</li>
     *     <li>{@code CLIENT_PUBLIC_KEY} – receives client RSA key for handshake</li>
     *     <li>{@code TOKEN_REQUEST} – initiates token exchange (RSA-encrypted)</li>
     * </ul>
     * Any unexpected message type received before AES setup is logged as an error.
     *
     * <h3>RSA → AES Handshake:</h3>
     * <ol>
     *     <li>Client sends {@code CLIENT_PUBLIC_KEY}</li>
     *     <li>Server responds with its own public key</li>
     *     <li>Client sends {@code TOKEN_REQUEST}</li>
     *     <li>Server encrypts AES token with client RSA public key</li>
     *     <li>Client decrypts token and responds with {@code CLIENT_AES_KEY}</li>
     *     <li>Server decrypts key and stores it in {@code token} field</li>
     * </ol>
     *
     * <h3>Workflow after AES token exchange:</h3>
     * All messages must be received in the form:
     * <pre>
     * {
     *   "type": "ENCRYPTED",
     *   "data": "<AES-GCM ciphertext>"
     * }
     * </pre>
     * The body is decrypted into a secondary JSON object whose {@code type}
     * determines which handler method is executed.
     *
     * <h3>Supported encrypted message types include:</h3>
     * <ul>
     *     <li>{@code LOGIN_REQUEST}</li>
     *     <li>{@code REQUEST_DOCTOR_BY_EMAIL}</li>
     *     <li>{@code REQUEST_PATIENT_BY_EMAIL}</li>
     *     <li>{@code REQUEST_DOCTOR_BY_ID}</li>
     *     <li>{@code UPLOAD_SIGNAL}</li>
     *     <li>{@code REQUEST_SIGNAL}</li>
     *     <li>{@code REQUEST_PATIENT_SIGNALS}</li>
     *     <li>{@code SAVE_REPORT}</li>
     *     <li>{@code SAVE_COMMENTS_SIGNAL}</li>
     *     <li>{@code CHANGE_PASSWORD_REQUEST}</li>
     *     <li>{@code ALERT_ADMIN}</li>
     *     <li>{@code STOP_CLIENT}</li>
     * </ul>
     *
     * <h3>Connection termination:</h3>
     * If the client sends {@code STOP_CLIENT}, or the socket closes unexpectedly,
     * the handler:
     * <ul>
     *     <li>Closes all I/O streams</li>
     *     <li>Removes the client from the server's active list</li>
     *     <li>Stops the handler thread</li>
     * </ul>
     *
     * <h3>Error handling:</h3>
     * Any malformed JSON or unexpected message type is skipped safely without
     * crashing the thread. A {@link SocketException} triggers an immediate cleanup.
     *
     * <p>This loop continues running as long as the client is connected and
     * {@code running.get()} remains {@code true}.</p>
     */

    @Override
    public void run(){
        try {
            String line;
            label:
            while (running.get() && (line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {continue;} //Skip empty lines
                // Desencriptar Json
                JsonObject request;
                try {
                   request = gson.fromJson(line, JsonObject.class);
                    System.out.println(request);//Turns the lines into a JsonObject
                }catch (JsonSyntaxException e){
                    System.out.println(line);
                    continue;
                }
                if (request == null) {continue;}

                // Extract the type field from the JSON
                String type = request.get("type").getAsString();

                if(token == null){
                    switch (type){
                        case "ACTIVATION_REQUEST": {
                            // Activation happens BEFORE the real token is exchanged
                            handleActivationRequest(request.getAsJsonObject("data"));
                            break;
                        }

                        case "CLIENT_PUBLIC_KEY" : {
                            System.out.println("This is the Server's Key Pair: ");
                            System.out.println("Public Key (Base64): " + Base64.getEncoder().encodeToString(serverKeyPair.getPublic().getEncoded()));
                            System.out.println("Private Key (Base64): " + Base64.getEncoder().encodeToString(serverKeyPair.getPrivate().getEncoded()));
                            JsonObject data = request.getAsJsonObject("data");
                            handleClientPublicKey(data);
                            break;
                        }
                        case "TOKEN_REQUEST" : {
                            String email = request.get("email").getAsString();

                            UserJDBC userJDBC = server.getAdminLinkService().getSecurityManager().getUserJDBC();
                            User user = userJDBC.findUserByEmail(email);

                            if (user!=null && user.getPublicKey()!=null){
                                try{
                                    byte[] keyBytes = Base64.getDecoder().decode(user.getPublicKey());
                                    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
                                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                                    this.clientPublicKey = keyFactory.generatePublic(keySpec);
                                    System.out.println("Retrieved and set client public key from DB for: "+email);
                                }catch (Exception e){
                                    System.err.println("Error decoding public key from client");
                                    break;
                                }
                            }
                            if (clientPublicKey == null){
                                System.out.println("TOKEN_REQUEST received before CLIENT_PUBLIC_KEY");
                                break;
                            }
                            sendPublicKey();
                            sendTokenToClient();
                            break;
                        }
                        default:
                            System.err.println("Received unexpected message type before AES token exchange: "+type);
                            break;
                    }
                    continue;
                }

                //Further requests
                System.out.println("\nThis is the encrypted message received from the Client: "+request);
                String typeDecrypted = type; //default original type
                JsonObject decryptedRequest = request; //default original request
                if(type.equals("ENCRYPTED")){
                    String encryptedData = request.get("data").getAsString();
                    String decryptedJson = TokenUtils.decrypt(encryptedData, token);
                    decryptedRequest = gson.fromJson(decryptedJson, JsonObject.class);
                    typeDecrypted = decryptedRequest.get("type").getAsString();
                }
                System.out.println("This is the decrypted message received in Server: "+decryptedRequest);

                // The type will tell the server what action to perform
                switch (typeDecrypted) {
                    case "STOP_CLIENT":
                        //TODO: Checked
                        //Client asked to stop itself or server asked client to stop and client echoes
                        System.out.println("Received STOP_CLIENT from"+getSocketAddress());
                        releaseResources(in, out, socket);
                        break;
                    case "LOGIN_REQUEST": {
                        //TODO: Checked
                        System.out.println("LOGIN REQUEST");
                        handleLogIn(decryptedRequest.getAsJsonObject("data"));
                        break;
                    }
                    case "REQUEST_DOCTOR_BY_EMAIL": {
                        //TODO: try
                        System.out.println("REQUEST DOCTOR_BY_EMAIL");
                        handleRequestDoctorByEmail(decryptedRequest.getAsJsonObject("data"));
                        break;
                    }
                    case "REQUEST_PATIENTS_FROM_DOCTOR": {
                        //TODO: try
                        System.out.println("REQUEST_PATIENTS_FROM_DOCTOR");
                        handleRequestPatientsFromDoctor(decryptedRequest.getAsJsonObject("data"));
                        break;
                    }
                    case "REQUEST_PATIENT_BY_EMAIL": {
                        // TODO: Checked
                        System.out.println("REQUEST_PATIENT_BY_EMAIL");
                        handleRequestPatientByEmail(decryptedRequest.getAsJsonObject("data"));
                        break;
                    }
                    case "REQUEST_DOCTOR_BY_ID": {
                        //TODO: Checked
                        System.out.println("REQUEST DOCTOR_BY_ID");
                        handleRequestDoctorById(decryptedRequest.getAsJsonObject("data"));
                        break;
                    }
                    case "SAVE_COMMENTS_SIGNAL": {
                        //TODO: try
                        System.out.println("SAVE_COMMENTS_SIGNAL");
                        handleSaveCommentsSignal(decryptedRequest.getAsJsonObject("data"));
                        break;
                    }
                    case "UPLOAD_SIGNAL" : {
                        //TODO: try
                        System.out.println("UPLOAD_SIGNAL");
                        handleRequestSignalPatient(decryptedRequest);
                        break;
                    }
                    case "REQUEST_SIGNAL" : {
                        //TODO: try
                        System.out.println("REQUEST_SIGNAL");
                        handleRequestSignal(decryptedRequest.getAsJsonObject("data"));
                        break;
                    }

                    case "REQUEST_PATIENT_SIGNALS" : {
                        //TODO: try
                        System.out.println("REQUEST_PATIENT_SIGNALS");
                        handleRequestPatientSignals(decryptedRequest.getAsJsonObject("data"));
                        break;
                    }
                    case "SAVE_REPORT":{
                        //TODO: Checked
                        System.out.println("SAVE_REPORT");
                        handleSaveReportRequest(decryptedRequest.getAsJsonObject("data"));
                        break;
                    }

                    case "CLIENT_AES_KEY" : {
                        //TODO: Checked
                        System.out.println("CLIENT_AES_KEY");
                        // This one is encrypted by public key encryption2
                        String encryptedAESkey = request.get("data").getAsString();
                        try {
                            String decryptedAESkey = RSAUtil.decrypt(encryptedAESkey, serverKeyPair.getPrivate());
                            byte[] AESkeyBytes = Base64.getDecoder().decode(decryptedAESkey); //In bytes
                            SecretKey AESkey = new SecretKeySpec(AESkeyBytes, 0, AESkeyBytes.length, "AES");
                            //Store the secret key inside the Handler for the connection
                            this.token = AESkey;
                            System.out.println("AES key retrieved and decrypted successfully");
                            System.out.println("This is the Server's secret key AES:"+Base64.getEncoder().encodeToString(AESkey.getEncoded()));
                            System.out.println("This is the Server's public key RSA:"+Base64.getEncoder().encodeToString(serverKeyPair.getPublic().getEncoded())+"and the private RSA key: "+Base64.getEncoder().encode(serverKeyPair.getPrivate().getEncoded()));

                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    }

                    case "CHANGE_PASSWORD_REQUEST": {
                        System.out.println("CHANGE_PASSWORD_REQUEST");
                        handleChangePassword(decryptedRequest.getAsJsonObject("data"));
                        break;
                    }
                    case "ALERT_ADMIN": {
                        System.out.println("ALERT_ADMIN");
                        handleClientAlert(decryptedRequest.getAsJsonObject("data"));
                        break;
                    }
                }

            }
        } catch (Exception e){
            if(e.getClass() == SocketException.class){
                try {
                    System.out.println("Client stopped connection abruptly");
                    releaseResources(in, out, socket);
                    System.out.println("Client thread stopped");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }else{
                System.out.println("Client: "+socket.getInetAddress()+":"+running.get());
                //System.out.println("Error reading from client"+e.getMessage());
            }
        }
    }

    /**
     * Decodes and stores the client's RSA public key from its Base64
     * representation for subsequent encrypted communication.
     * @param data
     */
    private void handleClientPublicKey(JsonObject data){
        try{
            String email = data.get("email").getAsString();
            String clientPublicKeyBase64 = data.get("public_key").getAsString();
            //Decode the key and reconstruct the PublicKey object key
            byte[] decoded = Base64.getDecoder().decode(clientPublicKeyBase64);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.clientPublicKey = keyFactory.generatePublic(spec);


            UserJDBC userJDBC = server.getAdminLinkService().getSecurityManager().getUserJDBC();
            User user = userJDBC.findUserByEmail(email);

            if (user !=null){
                userJDBC.changePublicKey(user, clientPublicKeyBase64);
                System.out.println("Stored new client public key for: "+user.getEmail());
            }else{
                System.out.println("User not found when storing public key");
            }
            System.out.println("Received and stored Client's Public Key: "+Base64.getEncoder().encodeToString(this.clientPublicKey.getEncoded()));
        }catch (Exception e){
            System.out.println("Failed to parse client's public key: "+e.getMessage());
        }
    }
    /**
     * Handles an account activation request sent by a client prior to first login.
     * <p>
     * Expected incoming JSON format:
     * <pre>
     * {
     *   "email": "example@demo.com",
     *   "temp_pass": "temporaryPassword",
     *   "temp_token": "oneTimeActivationToken"
     * }
     * </pre>
     *
     * <p>This method validates all activation requirements:</p>
     * <ul>
     *     <li>Verifies that the user exists in the SecurityDB</li>
     *     <li>Checks that the temporary password matches the stored hashed password</li>
     *     <li>Ensures the account is not already active</li>
     *     <li>Validates the one-time activation token</li>
     *     <li>Updates the user's active state in both SecurityDB and MedicalDB
     *         (depending on whether the user is a Patient or Doctor)</li>
     * </ul>
     *
     * <p>Success response:</p>
     * <pre>
     * {
     *   "type": "ACTIVATION_REQUEST_RESPONSE",
     *   "status": "SUCCESS",
     *   "message": "Account activated successfully"
     * }
     * </pre>
     *
     * <p>Error response examples:</p>
     * <pre>
     * {
     *   "status": "ERROR",
     *   "message": "User not found"
     * }
     *
     * {
     *   "status": "ERROR",
     *   "message": "Invalid temporary password"
     * }
     *
     * {
     *   "status": "ERROR",
     *   "message": "Invalid temporary token"
     * }
     *
     * {
     *   "status": "ERROR",
     *   "message": "Account already activated"
     * }
     * </pre>
     *
     * <p>The server writes the response back to the client in plaintext
     * because activation happens before encryption keys are exchanged.</p>
     *
     * @param data JSON object containing email, temporary password, and one-time activation token
     */

    private void handleActivationRequest(JsonObject data){
            System.out.println("Handling activation request");
            String email = data.get("email").getAsString();
            String tempPassword = data.get("temp_pass").getAsString();
            String oneTimeToken = data.get("temp_token").getAsString();

            System.out.println("Activation request from: "+email);

            JsonObject response = new JsonObject();
            response.addProperty("type", "ACTIVATION_REQUEST_RESPONSE");
        try{
            User user = server.getAdminLinkService().getSecurityManager().getUserJDBC().findUserByEmail(email);
            if (user == null){
                response.addProperty("status", "ERROR");
                response.addProperty("message", "User not found");
                out.println(response);
                out.flush();
                return;
            }
            if (!PasswordHash.verifyPassword(tempPassword,user.getPassword())){
                //Verification if the password is the same as the hashedPassword in the DB
                response.addProperty("status", "ERROR");
                response.addProperty("message", "Invalid temporary password");
                out.println(response);
                out.flush();
                return;
            }
            if(user.isActive()){
                response.addProperty("status", "ERROR");
                response.addProperty("message", "Account already activated");
                out.println(response);
                out.flush();
                return;
            }
            if(!user.getPublicKey().equals(oneTimeToken)){
                response.addProperty("status", "ERROR");
                response.addProperty("message", "Invalid temporary token");
                out.println(response);
                out.flush();
                return;
            }


            //Changes the active state
            user.setActive(true);
            //Updates its status
            Role role = server.getAdminLinkService().getSecurityManager().getRoleJDBC().findRoleByID(user.getRole_id());
            if(role.getRolename().equals("Doctor")){
                server.getAdminLinkService().changeDoctorAndUserStatus(email, true);
                //server.getAdminLinkService().getMedicalManager().getDoctorJDBC().updateDoctorActiveStatus(email, true);

            } else if (role.getRolename().equals("Patient")) {
                server.getAdminLinkService().changePatientAndUserStatus(email, true);
                //server.getAdminLinkService().getMedicalManager().getPatientJDBC().updatePatientActiveStatus(email, true);
            }else{
                server.getAdminLinkService().getSecurityManager().getUserJDBC().updateUserActiveStatus(email,true);
            }


            response.addProperty("status", "SUCCESS");
            response.addProperty("message", "Account activated successfully");
            out.println(response);
            out.flush();
            System.out.println("User activated successfully: "+email);

        }catch (Exception e){
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Server error during activation");
            out.println(response);
            out.flush();
            e.printStackTrace();
        }
    }

    /**
     * Generates a session AES token, encrypts it with the client's public key,
     * signs it with the server's private key, and sends the token response
     * to the client.
     * @throws Exception
     */
    private void sendTokenToClient () throws Exception{
        SecretKey token = TokenUtils.generateToken();
        this.token = token; //Stores token for this session
        System.out.println("🔑 Server's AES Token (Base64): " + Base64.getEncoder().encodeToString(token.getEncoded()));
        // Encrypt token with client's Public Key -> Confidentiality
        String encryptedToken = RSAUtil.encrypt(Base64.getEncoder().encodeToString(token.getEncoded()), clientPublicKey);
        // Sign token with server's Private Key -> Authenticity
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(serverKeyPair.getPrivate());
        signature.update(token.getEncoded());
        byte[] signatureBytes = signature.sign();

        //Prepare JSON response to Client
        JsonObject response = new JsonObject();
        response.addProperty("type","TOKEN_REQUEST_RESPONSE");
        response.addProperty("token", encryptedToken);
        response.addProperty("signature", Base64.getEncoder().encodeToString(signatureBytes));

        System.out.println("This is the token sent: "+response);
        out.println(gson.toJson(response));
        out.flush();
        System.out.println("AES token sent to Client successfully");
    }

    /**
     * Sends the Server's public key as a JSON Object to follow the protocol. Adds a "type" field to tell the client
     * what kind of message it is and encodes the server's public key from binary into a Base64 String into the "data"
     * field. This makes it safe to send over a text stream.
     */
    public void sendPublicKey() {
        String serverPublicKey = Base64.getEncoder().encodeToString(serverKeyPair.getPublic().getEncoded());
        JsonObject response = new JsonObject();
        response.addProperty("type", "SERVER_PUBLIC_KEY");
        response.addProperty("data", serverPublicKey);
        out.println(gson.toJson(response));
        out.flush();

        System.out.println("Sent Server's Public Key to Client");
    }

    private void handleClientAlert(JsonObject data) {
        JsonObject response = new JsonObject();
        response.addProperty("type", "SERVER_ALERT_RESPONSE");
        response.addProperty("message", "Response not found, help is on the way!" );
        sendEncrypted(response,out,token);
    }

    /**
     * Handles a REQUEST_PATIENT_SIGNALS request by validating the doctor,
     * retrieving the patient's signals, and sending them in an encrypted response.
     * @param data
     */
    private void handleRequestPatientSignals(JsonObject data) {
        JsonObject response = new JsonObject();
        response.addProperty("type", "REQUEST_PATIENT_SIGNALS_RESPONSE");

        int patientId = data.get("patient_id").getAsInt();
        int userId   = data.get("user_id").getAsInt();
        User user = server.getAdminLinkService().getSecurityManager().getUserJDBC().findUserByID(userId);
        if (user == null) {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "User not found");

            System.out.println("\nBefore encryption, REQUEST_PATIENT_SIGNALS_RESPONSE to Client: "+response);
            sendEncrypted(response,out, token);
            return;
        }
        Role role = server.getAdminLinkService().getSecurityManager().getRoleJDBC().findRoleByID(user.getRole_id());
        if (role == null || !role.getRolename().equals("Doctor")) {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Not authorized");
            System.out.println("\nBefore encryption, REQUEST_PATIENT_SIGNALS_RESPONSE to Client: "+response);
            sendEncrypted(response,out, token);
            return;
        }

        List<Signal> signals = server.getAdminLinkService().getMedicalManager().getSignalJDBC().getSignalsByPatientId(patientId);

        JsonArray signalsArray = new JsonArray();
        for (Signal signal : signals) {
            JsonObject signalObj = new JsonObject();
            signalObj.addProperty("signal_id", signal.getId());
            signalObj.addProperty("date", signal.getDate().toString());
            signalObj.addProperty("sampling_rate", signal.getSampleFrequency());
            signalObj.addProperty("comments", signal.getComments());
            signalsArray.add(signalObj);
        }

        response.addProperty("status", "SUCCESS");
        response.add("signals", signalsArray);
        System.out.println("\nBefore encryption, REQUEST_PATIENT_SIGNALS_RESPONSE to Client: "+response);
        sendEncrypted(response,out, token);
    }

    /**
     * Handles a REQUEST_SIGNAL request by validating the user, locating the
     * requested signal, encoding its ZIP file as Base64, and returning it
     * with metadata in an encrypted response.
     * @param data
     * @throws IOException
     */
    private void handleRequestSignal(JsonObject data) throws IOException {
        System.out.println(data.toString());
        JsonObject response = new JsonObject();
        response.addProperty("type", "REQUEST_SIGNAL_RESPONSE");

        int signalId = data.get("signal_id").getAsInt();
        int userId   = data.get("user_id").getAsInt();
        User user = server.getAdminLinkService().getSecurityManager().getUserJDBC().findUserByID(userId);
        if (user == null) {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "User not found");
            System.out.println("\nBefore encryption, REQUEST_SIGNAL_RESPONSE to Client: "+response);
            sendEncrypted(response,out, token);
            return;
        }
        Role role = server.getAdminLinkService().getSecurityManager().getRoleJDBC().findRoleByID(user.getRole_id());
        if (role == null || !role.getRolename().equals("Doctor")) {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Not authorized");
            System.out.println("\nBefore encryption, REQUEST_DOCTOR_BY_EMAIL_RESPONSE to Client: "+response);
            sendEncrypted(response,out, token);
            return;
        }

        Signal signal = server.getAdminLinkService().getMedicalManager().getSignalJDBC().findSignalById(signalId);

        if (signal == null) {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Signal not found");
            System.out.println("\nBefore encryption, REQUEST_DOCTOR_BY_EMAIL_RESPONSE to Client: "+response);
            sendEncrypted(response,out, token);

        }else {
            byte[] zipBytes = Files.readAllBytes(signal.getFile().toPath());
            String base64Zip = Base64.getEncoder().encodeToString(zipBytes);
            JsonObject metadata = new JsonObject();
            metadata.addProperty("signal_id", signal.getId());
            metadata.addProperty("patient_id", signal.getPatientId());
            metadata.addProperty("sampling_rate", signal.getSampleFrequency());
            metadata.addProperty("comments", signal.getComments());
            metadata.addProperty("date", signal.getDate().toString());
            response.addProperty("status", "SUCCESS");
            //TODO: Ver si realmente coge bien la metadata
            response.add("metadata", metadata);
            response.addProperty("compression", "zip-base64");
            response.addProperty("filename", "signal_" + signal.getId() + ".zip");
            response.addProperty("dataBytes", base64Zip);

            System.out.println("\nBefore encryption, REQUEST_DOCTOR_BY_EMAIL_RESPONSE to Client: "+response);
            sendEncrypted(response,out, token);
        }
    }

    /**
     * Handles an UPLOAD_SIGNAL request by decoding the incoming ZIP data,
     * reconstructing the signal metadata, inserting the signal into the
     * database, and returning an encrypted response.
     * @param dataIn
     * @throws IOException
     */
    private void handleRequestSignalPatient(JsonObject dataIn) throws IOException {

        JsonObject response = new JsonObject();
        response.addProperty("type", "UPLOAD_SIGNAL_RESPONSE");

            JsonObject metadata = dataIn.getAsJsonObject("metadata");

            int patientId = metadata.get("patient_id").getAsInt();
            int sampleFrequency = metadata.get("sampling_rate").getAsInt();
            String timestamp = metadata.get("timestamp").getAsString();
            LocalDateTime dateTime = LocalDateTime.parse(timestamp);

            String filename = dataIn.get("filename").getAsString();
            String base64Data = dataIn.get("dataBytes").getAsString();

            Patient patient = server.getAdminLinkService().getMedicalManager().getPatientJDBC().findPatientByID(patientId);

            if (patient == null) {
                response.addProperty("status", "ERROR");
                response.addProperty("message", "Patient not found");

                System.out.println("\nBefore encryption, REQUEST_PATIENT_SIGNALS_RESPONSE to Client: "+response);
                sendEncrypted(response,out, token);
                return;
            }
            // Decode base64 data
            byte[] zipBytes = Base64.getDecoder().decode(base64Data);

            File tempZip = File.createTempFile("signal_", ".zip"); //se guarda temporalmente en el servidor
            try (FileOutputStream fos = new FileOutputStream(tempZip)) {
                fos.write(zipBytes);  //decodifica los bytes de la señal del paciente
            }

            Signal record = new Signal(
                    tempZip,
                    dateTime.toLocalDate(),
                    "",              // comments initially empty
                    patientId,
                    sampleFrequency
            );
            System.out.println("Inserting signal for patient ID: " + patientId + " with sampling rate: " + sampleFrequency + " at " + dateTime.toString());

               if(!server.getAdminLinkService().getMedicalManager().getSignalJDBC().insertSignal(record)) {
                response.addProperty("status", "ERROR");
                response.addProperty("type", "ERROR ADDING SIGNAL TO DATABASE");
                response.addProperty("message", "Error saving signal: ");
                // TODO: Encriptar response
                   System.out.println("\nBefore encryption, UPLOAD_SIGNAL_RESPONSE to Client: "+response);
                   sendEncrypted(response,out, token);
                }else {
                   response.addProperty("status", "SUCCESS");
                   response.addProperty("message", "Signal uploaded correctly");
                   //TODO: encriptar response
                   System.out.println("\nBefore encryption, UPLOAD_SIGNAL_RESPONSE to Client: "+response);
                   sendEncrypted(response,out, token);
               }

    }


    /**
     * Forces an immediate shutdown of this client connection from the server side.
     * <p>
     * The method:
     * <ul>
     *     <li>Sends an encrypted {@code STOP_CLIENT} message to notify the client</li>
     *     <li>Sets the running flag to {@code false}</li>
     *     <li>Closes the underlying socket, unblocking any pending read operations</li>
     *     <li>Removes the client instance from the server's active client list</li>
     * </ul>
     * This method is typically used when the server needs to terminate a client
     * forcibly (e.g., server shutdown, security violation, or protocol error).
     */

    public void forceShutdown(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "STOP_CLIENT");

        System.out.println("\nBefore encryption, STOP_CLIENT to Client: "+jsonObject);
        sendEncrypted(jsonObject,out, token);

        running.set(false);
        try {
            socket.close(); //this will unblock readline() in run()
            System.out.println("Socktet closed"+socket.getInetAddress());
            server.removeClient(this);
        }catch (IOException e){
            e.getMessage();
        }
    }
    /**
     * Returns whether this client handler is no longer running.
     *
     * @return {@code true} if the client has been stopped, {@code false} otherwise.
     */

    public boolean isStopped(){
        return !running.get();
    }
    /**
     * Releases and closes all I/O resources associated with the client connection.
     * <p>
     * This includes:
     * <ul>
     *     <li>Removing the client from the server's active client list</li>
     *     <li>Stopping the internal running flag</li>
     *     <li>Closing the input stream</li>
     *     <li>Closing the output stream</li>
     *     <li>Closing the socket if still open</li>
     * </ul>
     *
     * @param bufferedReader The input buffer to close (may be null)
     * @param out            The output writer to close (may be null)
     * @param clientSocket   The socket associated with the client (may be null)
     * @throws IOException If any of the resources fail to close properly
     */

    void releaseResources(BufferedReader bufferedReader, PrintWriter out, Socket clientSocket) throws IOException {
        server.removeClient(this);
        running.set(false);
        try {if (bufferedReader!=null) bufferedReader.close();} catch (IOException ex) {System.out.println("Error closing socket"+ex.getMessage());}
        if(out!=null)out.close();
        try {if(clientSocket!=null && !clientSocket.isClosed())clientSocket.close();} catch (IOException ex) {System.out.println("Error closing socket"+ex.getMessage());}
    }
    /**
     * Returns the IP address of the connected client as a string.
     *
     * @return The client's socket IP address in string format.
     */

    public String getSocketAddress(){
        return socket.getInetAddress().toString();
    }

    /**
     * Handles a login request sent by a client and generates an encrypted
     * {@code LOGIN_RESPONSE} message.
     * <p>
     * Expected incoming JSON format:
     * <pre>
     * {
     *   "email": "example@demo.com",
     *   "password": "1234",
     *   "access_permits": "Patient"
     * }
     * </pre>
     *
     * <p>If login succeeds, the server responds with:</p>
     * <pre>
     * {
     *   "type": "LOGIN_RESPONSE",
     *   "status": "SUCCESS",
     *   "data": {
     *     "id": 1,
     *     "email": "juan@demo.com",
     *     "role": "patient"
     *   }
     * }
     * </pre>
     *
     * <p>If login fails, the response contains an error message:</p>
     * <pre>
     * {
     *   "type": "LOGIN_RESPONSE",
     *   "status": "ERROR",
     *   "message": "Invalid credentials"
     * }
     * </pre>
     *
     * <p>This method performs the following steps:</p>
     * <ul>
     *   <li>Extracts email, password, and access-permit role from the request</li>
     *   <li>Checks whether the user exists in the SecurityDB</li>
     *   <li>Validates the password</li>
     *   <li>Verifies the user's assigned role against the required access permits</li>
     *   <li>Ensures the user account is still active</li>
     *   <li>Builds a JSON response containing either SUCCESS or ERROR</li>
     *   <li>Sends the response encrypted using AES</li>
     * </ul>
     *
     * @param data JSON object containing login credentials and required access-permit role
     * @throws IOException If sending the encrypted response fails
     */

    void handleLogIn(JsonObject data) throws IOException {
        String email = data.get("email").getAsString();
        String password = data.get("password").getAsString();
        String accessPermits = data.get("access_permits").getAsString();

        JsonObject response = new JsonObject();
        response.addProperty("type", "LOGIN_RESPONSE");

        if (server.getAdminLinkService().getSecurityManager().getUserJDBC().isUser(email)) {
            User user = server.getAdminLinkService().getSecurityManager().getUserJDBC().login(email, password);
            System.out.println(user);
            if (user != null) {
                Role role = server.getAdminLinkService().getSecurityManager().getRoleJDBC().findRoleByID(user.getRole_id());
                if(role != null && role.getRolename().equals(accessPermits)) {
                    if(user.isActive()){
                        response.addProperty("status", "SUCCESS");
                        JsonObject userObj = new JsonObject();
                        userObj.addProperty("id", user.getId());
                        userObj.addProperty("email", user.getEmail());
                        userObj.addProperty("role", role.getRolename());
                        response.add("data", userObj);
                    }else {
                        response.addProperty("status", "ERROR");
                        response.addProperty("message", "The user is no longer active.");
                    }

                }else{
                    response.addProperty("status", "ERROR");
                    response.addProperty("message", "Authorization denied");
                }
            } else {
                response.addProperty("status", "ERROR");
                response.addProperty("message", "Invalid password");
            }
        } else {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "User not found");
        }

        System.out.println("\nBefore encryption, LOGIN_RESPONSE to Client: "+response);
        sendEncrypted(response,out, token);
    }

    /**
     * Handles a REQUEST_DOCTOR_BY_EMAIL request by validating the user and role,
     * retrieving the doctor data if authorized, and sending the encrypted response.
     * @param dataIn
     * @throws IOException
     */
    private void handleRequestDoctorByEmail(JsonObject dataIn) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("type", "REQUEST_DOCTOR_BY_EMAIL_RESPONSE");

        String email = dataIn.get("email").getAsString();
        Integer user_id = dataIn.get("user_id").getAsInt();
        User user = server.getAdminLinkService().getSecurityManager().getUserJDBC().findUserByID(user_id);
        if(user == null){
            response.addProperty("status", "ERROR");
            response.addProperty("message", "User not found");
            System.out.println("\nBefore encryption, REQUEST_DOCTOR_BY_EMAIL_RESPONSE to Client: "+response);
            sendEncrypted(response,out, token);
            return;
        }

        Role role =  server.getAdminLinkService().getSecurityManager().getRoleJDBC().findRoleByID(user.getRole_id());
        if(role==null || !role.getRolename().equals("Doctor") || !email.equals(user.getEmail())) {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Not authorized");
            System.out.println("\nBefore encryption, REQUEST_DOCTOR_BY_EMAIL_RESPONSE to Client: "+response);
            sendEncrypted(response,out, token);
            return;
        }

        Doctor doctor = server.getAdminLinkService().getMedicalManager().getDoctorJDBC().findDoctorByEmail(email);
        if(doctor != null) {
            if(doctor.isActive()){
                response.addProperty("status", "SUCCESS");
                response.add("doctor", doctor.toJason());
            }else{
                response.addProperty("status", "ERROR");
                response.addProperty("message", "Doctor is no longer active.");
            }
        }else{
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Doctor not found");
        }
        System.out.println("\nBefore encryption, REQUEST_DOCTOR_BY_EMAIL_RESPONSE to Client: "+response);
        sendEncrypted(response, out, token);
    }

    /**
     * Both the Doctor, Admin and the Patient whose Doctor is the requested doctor should have access
     * Each Doctor can only access their own info, not other doctor's info
     * @param data
     * @throws IOException
     */
    private void handleRequestDoctorById(JsonObject data) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("type", "REQUEST_DOCTOR_BY_ID_RESPONSE");

        int doctor_id = data.get("doctor_id").getAsInt();
        int user_id = data.get("user_id").getAsInt();
        User user = server.getAdminLinkService().getSecurityManager().getUserJDBC().findUserByID(user_id);
        if(user == null){
            response.addProperty("status", "ERROR");
            System.out.println("\nBefore encryption, REQUEST_DOCTOR_BY_ID_RESPONSE to Client: "+response);
            sendEncrypted(response,out, token);
            return;
        }

        Role role =  server.getAdminLinkService().getSecurityManager().getRoleJDBC().findRoleByID(user.getRole_id());
        if(role == null){
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Role not found");
            System.out.println("\nBefore encryption, REQUEST_DOCTOR_BY_ID_RESPONSE to Client: "+response);
            sendEncrypted(response,out, token);
            return;
        }
        //If the patient is requesting the Doctor info of a Doctor that is not theirs, don't authorize the access
        if(role.getRolename().equals("Patient")){
            Patient patient = server.getAdminLinkService().getMedicalManager().getPatientJDBC().findPatientByEmail(user.getEmail());
            if(patient.getDoctorId() != doctor_id){
                response.addProperty("status", "ERROR");
                response.addProperty("message", "Not authorized");

                System.out.println("\nBefore encryption, REQUEST_DOCTOR_BY_ID_RESPONSE to Client: "+response);
                sendEncrypted(response,out, token);
                return;
            }
        }

        Doctor doctor = server.getAdminLinkService().getMedicalManager().getDoctorJDBC().getDoctor(doctor_id);
        if(doctor != null) {
            if(role.getRolename().equals("Doctor")&& !doctor.getEmail().equals(user.getEmail())) {
                //If the doctor requesting for info is a different doctor
                response.addProperty("status", "ERROR");
                response.addProperty("message", "Not authorized");

                System.out.println("\nBefore encryption, REQUEST_DOCTOR_BY_ID_RESPONSE to Client: "+response);
                sendEncrypted(response,out, token);
                return;
            }
            response.addProperty("status", "SUCCESS");
            response.add("doctor", doctor.toJason());

        }else{
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Doctor not found");
        }
        System.out.println("\nBefore encryption REQUEST_DOCTOR_BY_ID_RESPONSE to Client: "+response);
        sendEncrypted(response,out, token);
    }

    /**
     * Handles a REQUEST_PATIENT_BY_EMAIL request by validating the user,
     * retrieving the patient and their related signals and reports, and
     * sending the result in an encrypted response.
     * @param data
     * @throws IOException
     */
    private void handleRequestPatientByEmail(JsonObject data) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("type", "REQUEST_PATIENT_BY_EMAIL_RESPONSE");

        String email = data.get("email").getAsString();
        Integer user_id = data.get("user_id").getAsInt();
        User user = server.getAdminLinkService().getSecurityManager().getUserJDBC().findUserByID(user_id);
        if(user == null || !user.getEmail().equals(email)) {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Not authorized");
            System.out.println("\nBefore encryption, REQUEST_PATIENT_BY_EMAIL_RESPONSE to Client: "+response);
            sendEncrypted(response,out, token);
            return;
        }

        Patient patient = server.getAdminLinkService().getMedicalManager().getPatientJDBC().findPatientByEmail(email);
        if(patient != null) {
            if(patient.isActive()) {
                response.addProperty("status", "SUCCESS");
                List<Signal> signals = server.getAdminLinkService().getMedicalManager().getSignalJDBC().getSignalsByPatientId(patient.getId());
                List<Report> symptoms = server.getAdminLinkService().getMedicalManager().getReportJDBC().getReportsByPatientId(patient.getId());
                JsonObject pJson = patient.toJason();
                JsonArray signalArray = new JsonArray();
                for (Signal s : signals) {
                    signalArray.add(s.toJson());
                }
                JsonArray symptomsArray = new JsonArray();
                for (Report s : symptoms) {
                    symptomsArray.add(s.toJson());
                }
                pJson.add("signals", signalArray);
                pJson.add("reports", symptomsArray);
                response.add("patient", pJson);
                System.out.println(pJson.toString());
            }else {
                response.addProperty("status", "ERROR");
                response.addProperty("message", "The user is no longer active");
            }
        }else{
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Doctor not found");
        }
        System.out.println("\nBefore encryption, REQUEST_PATIENT_BY_EMAIL_RESPONSE to Client: "+response);
        sendEncrypted(response,out, token);
    }

    /**
     * Handles a REQUEST_PATIENTS_FROM_DOCTOR request by validating the doctor,
     * retrieving all associated patients with their signals and reports,
     * and sending the aggregated data in an encrypted response.
     * @param data
     * @throws IOException
     */
    private void handleRequestPatientsFromDoctor(JsonObject data) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("type", "REQUEST_PATIENTS_FROM_DOCTOR_RESPONSE");

        Integer doctorId = data.get("doctor_id").getAsInt();
        Integer user_id = data.get("user_id").getAsInt();
        User user = server.getAdminLinkService().getSecurityManager().getUserJDBC().findUserByID(user_id);
        Doctor doctor = server.getAdminLinkService().getMedicalManager().getDoctorJDBC().getDoctor(doctorId);

        if(user != null && doctor != null && doctor.getEmail().equals(user.getEmail())) {

            response.addProperty("status", "SUCCESS");
            List<Patient> patients = server.getAdminLinkService().getMedicalManager().getPatientJDBC().getPatientsOfDoctor(doctorId);

            JsonArray patientArray = new JsonArray();
            for (Patient p : patients) {
                List<Signal> signals = server.getAdminLinkService().getMedicalManager().getSignalJDBC().getSignalsByPatientId(p.getId());
                List<Report> symptoms = server.getAdminLinkService().getMedicalManager().getReportJDBC().getReportsByPatientId(p.getId());
                JsonObject pJson = p.toJason();
                JsonArray signalArray = new JsonArray();
                for (Signal s : signals) {
                    signalArray.add(s.toJson());
                }
                JsonArray symptomsArray = new JsonArray();
                for (Report s : symptoms) {
                    symptomsArray.add(s.toJson());
                }
                pJson.add("signals", signalArray);
                pJson.add("reports", symptomsArray);

                patientArray.add(pJson);
            }

            response.add("patients", patientArray);

        }else {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Not authorized");
        }

        System.out.println("\nBefore encryption, REQUEST_PATIENT_FROM_DOCTOR_RESPONSE to Client: "+response);
        sendEncrypted(response,out, token);
    }

    /**
     * Handles a SAVE_COMMENTS_SIGNAL request by validating the doctor–patient
     * relationship, updating the signal comments, and sending an encrypted response.
     * @param data
     * @throws IOException
     */
    private void handleSaveCommentsSignal(JsonObject data) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("type", "SAVE_COMMENTS_SIGNAL_RESPONSE");

        String comments = data.get("comments").getAsString();
        Integer user_id = data.get("user_id").getAsInt();
        Integer signal_id = data.get("signal_id").getAsInt();
        Integer patient_id = data.get("patient_id").getAsInt();

        User user = server.getAdminLinkService().getSecurityManager().getUserJDBC().findUserByID(user_id);
        if(user == null) {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Not authorized");
            System.out.println("\nBefore encryption, SAVE_COMMENTS_SIGNAL_RESPONSE to Client: "+response);
            sendEncrypted(response,out, token);
            return;
        }

        //If the user is the doctor
        Doctor doctor = server.getAdminLinkService().getMedicalManager().getDoctorJDBC().findDoctorByEmail(user.getEmail());
        //And the patient_id is a patient of them
        Doctor doctor1 = server.getAdminLinkService().getMedicalManager().getDoctorJDBC().getDoctorFromPatient(patient_id);

        if(doctor!= null && doctor1 != null && doctor1.getId() == doctor.getId()) {
            if(server.getAdminLinkService().getMedicalManager().getSignalJDBC().updateSignalComments(signal_id, comments)) {
                response.addProperty("status", "SUCCESS");
            }else{
                response.addProperty("status", "ERROR");
                response.addProperty("message", "Error saving comments");
            }
        }else {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Not authorized");
        }

        System.out.println("\nBefore encryption, REQUEST_DOCTOR_BY_EMAIL_RESPONSE to Client: "+response);
        sendEncrypted(response,out, token);
    }

    /**
     * Handles a SAVE_REPORT request by validating the user and patient,
     * inserting the report into the database, and sending an encrypted response.
     * @param data
     * @throws IOException
     */
    private void handleSaveReportRequest(JsonObject data) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("type", "SAVE_REPORT_RESPONSE");

        Integer user_id = data.get("user_id").getAsInt();
        Integer patient_id = data.get("patient_id").getAsInt();
        Report report = Report.fromJson(data.get("report").getAsJsonObject());
        if(report == null) {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Error parsing report");
            System.out.println("\nBefore encryption, SAVE_REPORT_RESPONSE to Client: "+response);
            sendEncrypted(response,out, token);
            return;
        }

        User user = server.getAdminLinkService().getSecurityManager().getUserJDBC().findUserByID(user_id);
        if(user == null) {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Not authorized");
            System.out.println("\nBefore encryption, SAVE_REPORT_RESPONSE to Client: "+response);
            sendEncrypted(response,out, token);
            return;
        }

        Patient patient = server.getAdminLinkService().getMedicalManager().getPatientJDBC().findPatientByEmail(user.getEmail());
        if(patient == null) {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Not authorized");
            System.out.println("\nBefore encryption, SAVE_REPORT_RESPONSE to Client: "+response);
            sendEncrypted(response,out, token);
            return;
        }

        if(patient.getEmail().equals(user.getEmail())) {
            report.setPatientId(patient_id);
            if(server.getAdminLinkService().getMedicalManager().getReportJDBC().insertReport(report)) {
                response.addProperty("status", "SUCCESS");
            }else{
                response.addProperty("status", "ERROR");
                response.addProperty("message", "Error saving comments");
            }
        }else {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Not authorized");
        }

        System.out.println("\nBefore encryption, SAVE_REPORT_RESPONSE to Client: "+response);
        sendEncrypted(response,out, token);
    }

    /**
     * Processes a CHANGE_PASSWORD request by validating the user, hashing the
     * new password, updating it in the database, and sending an encrypted response.
     * @param data
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    private void handleChangePassword (JsonObject data) throws NoSuchAlgorithmException, InvalidKeySpecException {
        JsonObject response = new JsonObject();
        response.addProperty("type", "CHANGE_PASSWORD_REQUEST_RESPONSE");

        String email = data.get("email").getAsString();
        String newPassword = data.get("new_password").getAsString();

        if(!server.getAdminLinkService().getSecurityManager().getUserJDBC().isUser(email)){
            response.addProperty("status","ERROR");
            response.addProperty("message", "User not found");
        } else{
            User user = server.getAdminLinkService().getSecurityManager().getUserJDBC().findUserByEmail(email);
            String hashedPassword = encryption.PasswordHash.generatePasswordHash(newPassword);
            boolean success = server.getAdminLinkService().getSecurityManager().getUserJDBC().changePassword(user,hashedPassword);
            if (success){
                response.addProperty("status", "SUCCESS");
                JsonObject userObj = new JsonObject();
                userObj.addProperty("email", email);
                response.add("data", userObj);
                System.out.println("Password successfully updated for user: "+email);
            }else{
                response.addProperty("status", "ERROR");
                response.addProperty("message", "Failed to update password");
            }
        }
        System.out.println("\n Before encryption, CHANGE_PASSWORD_RESPONSE to Client: "+response);
        sendEncrypted(response, out, token);

    }

    /**
     * Encrypts the Json's data property.
     *
     * @param message
     * @param out
     * @param AESkey
     */
    private void sendEncrypted(JsonObject message, PrintWriter out, SecretKey AESkey){
        try{
            String encryptedJson = TokenUtils.encrypt(message.toString(), AESkey);
            JsonObject wrapper = new JsonObject();

            //TODO: ver si realmente el type debería ser especifico para cada case o no
            wrapper.addProperty("type", "ENCRYPTED");
            wrapper.addProperty("data", encryptedJson);

            System.out.println("\nThis is the encrypted message sent to Client: "+wrapper);

            out.println(wrapper); //String Json
            out.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

