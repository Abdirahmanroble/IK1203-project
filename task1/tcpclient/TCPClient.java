package tcpclient;

import java.net.*;
import java.io.*;

public class TCPClient {

    public TCPClient() {
    }

    public byte[] askServer(String hostname, int port, byte[] toServerBytes) throws IOException {
     Socket clientSocket = null;
     try {
        clientSocket = new Socket(hostname, port);
        if (toServerBytes != null && toServerBytes.length > 0) {
            OutputStream outToServer = clientSocket.getOutputStream();
            outToServer.write(toServerBytes);
            outToServer.flush();
        }
        InputStream inFromServer = clientSocket.getInputStream();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int bytesRead;
        while ((bytesRead = inFromServer.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        buffer.flush();
        return buffer.toByteArray();
     } catch (Exception e) {
        return null;
     } finally {
        if (clientSocket != null) {
            clientSocket.close();
        }
     }
    }
}
