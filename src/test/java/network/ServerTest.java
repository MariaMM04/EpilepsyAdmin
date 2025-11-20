package network;

import Exceptions.ClientError;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ui.windows.Application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/// que el servidor comienza correctamente y acepte varias conexiones,
/// que cierre las conexiones con los clientes correctamente,
/// que devuelva los clientes conectados, que se pare el servidor.
/// Crea los tests unitarios necesarios para comprobar que el clienthandler comienza correctamente,
/// es capaz de manejar cada uno de los requests planteados en el switch y que se cierra
/// correctamente al ejecutar el servidor la función shutdown
class ServerTest {
    private Server server;
    private Application app;
    private ServerSocket serverSocket;
    private InputStream in;
    private OutputStream out;

    @BeforeEach
    void setUp() throws IOException {
        app = mock(Application.class);
        serverSocket = mock(ServerSocket.class);
        server = spy(new Server(serverSocket, app));
        doReturn(serverSocket).when(server).createServerSocket(anyInt());
        in = mock(InputStream.class);
        out = mock(OutputStream.class);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testServerStartsAndAcceptsMultipleClient() throws Exception {
        // Arrange
        //Server server = new Server(serverSocket, app);
        Socket socket1 = mock(Socket.class);
        Socket socket2 = mock(Socket.class);
        when(serverSocket.accept())
                .thenReturn(socket1).thenReturn(socket2).thenThrow(new SocketException("Stop"));
        when(socket1.getInputStream()).thenReturn(in);
        when(socket1.getOutputStream()).thenReturn(out);
        when(socket1.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));
        when(socket2.getInputStream()).thenReturn(in);
        when(socket2.getOutputStream()).thenReturn(out);
        when(socket2.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));

        //when(clientHandler.in.readLine()).thenReturn("Hi, I'm a new client");

        // Act
        server.startServer();
        Thread.sleep(300); // dejar que acepte

        // Assert get connected clients
        assertEquals(2, server.getConnectedClients().size());
    }

    @Test
    void testServerCloseAllClients() throws Exception {
        // Arrange

        Socket socket1 = mock(Socket.class);
        Socket socket2 = mock(Socket.class);
        when(serverSocket.accept())
                .thenReturn(socket1).thenReturn(socket2).thenThrow(new SocketException("Stop"));
        when(socket1.getInputStream()).thenReturn(in);
        when(socket1.getOutputStream()).thenReturn(out);
        when(socket1.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));
        when(socket2.getInputStream()).thenReturn(in);
        when(socket2.getOutputStream()).thenReturn(out);
        when(socket2.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.2"));

        // Act
        server.startServer();
        Thread.sleep(300); // dejar que acepte
        server.closeAllClients();
        Thread.sleep(300);

        // Assert get connected clients
        assertEquals(0, server.getConnectedClients().size());
    }

    @Test
    void testServerStopsCorrectly() throws Exception {

        Socket socket1 = mock(Socket.class);
        Socket socket2 = mock(Socket.class);
        when(serverSocket.accept())
                .thenReturn(socket1).thenReturn(socket2).thenThrow(new SocketException("Stop"));
        when(socket1.getInputStream()).thenReturn(in);
        when(socket1.getOutputStream()).thenReturn(out);
        when(socket1.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));
        when(socket2.getInputStream()).thenReturn(in);
        when(socket2.getOutputStream()).thenReturn(out);
        when(socket2.getInetAddress()).thenReturn(InetAddress.getByName("127.0.0.2"));

        server.startServer();
        Thread.sleep(300);
        server.stop();

        verify(serverSocket).close();
        assertFalse(server.isRunning());
        assertEquals(0, server.getConnectedClients().size());
    }

}