package tcpclient;

import java.net.*;
import java.io.*;

public class TCPClient {

    private boolean shutdown;
    private Integer timeout;
    private Integer limit;

    public TCPClient(boolean shutdown, Integer timeout, Integer limit) {
        this.shutdown = shutdown;
        this.timeout = timeout;
        this.limit = limit;
    }

    public byte[] askServer(String hostname, int port, byte[] toServerBytes) throws IOException {
        Socket clientSocket = null;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try {
            clientSocket = new Socket(hostname, port);
            if (timeout != null) {
                clientSocket.setSoTimeout(timeout);
            }

            if (toServerBytes != null && toServerBytes.length > 0) {
                OutputStream outToServer = clientSocket.getOutputStream();
                outToServer.write(toServerBytes);
                outToServer.flush();

                if (shutdown) {
                    clientSocket.shutdownOutput();
                }
            }

            InputStream inFromServer = clientSocket.getInputStream();
            byte[] data = new byte[1024];
            int bytesRead;
            int totalBytesRead = 0;

            while ((bytesRead = inFromServer.read(data, 0, data.length)) != -1) {
                totalBytesRead += bytesRead;
                buffer.write(data, 0, bytesRead);
                if (limit != null && totalBytesRead >= limit) {
                    break;
                }
            }
            buffer.flush();
            return buffer.toByteArray();
        } catch (SocketTimeoutException e) {
            return buffer.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        } finally {
            if (clientSocket != null) {
                clientSocket.close();
            }
        }
    }
}
