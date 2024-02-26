import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

import tcpclient.TCPClient;

public class HTTPAsk {
    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server is running on port " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            handleClientRequest(clientSocket);
            clientSocket.close();
        }
    }
    private static void handleClientRequest(Socket clientSocket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String requestLine = getRequestLine(in);
            if (requestLine != null) {
                String[] requestParts = requestLine.split(" ");
                if (requestParts.length > 1 && requestParts[0].equals("GET")) {
                    String uri = requestParts[1];
                    if (uri.startsWith("/ask?")) {
                        Map<String, String> queryParams = getQueryParams(requestLine);
                        if (queryParams.containsKey("hostname") && queryParams.containsKey("port")) {
                            processRequest(queryParams, out);
                        } else {
                            sendBadRequestResponse(out);
                        }
                    } else {
                        sendNotFoundResponse(out);
                    }
                } else {
                    sendBadRequestResponse(out);
                }
            } else {
                sendBadRequestResponse(out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getRequestLine(BufferedReader in) throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            if (!line.isEmpty()) {
                return line;
            }
        }
        return null;
    }

    private static Map<String, String> getQueryParams(String requestLine) {
        Map<String, String> queryParams = new HashMap<>();
        String[] parts = requestLine.split(" ")[1].split("\\?");
        if (parts.length > 1) {
            String[] params = parts[1].split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length > 1) {
                    queryParams.put(keyValue[0], URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8));
                }
            }
        }
        return queryParams;
    }

    private static void processRequest(Map<String, String> queryParams, PrintWriter out) {
        try {
            String hostname = queryParams.get("hostname");
            int port = Integer.parseInt(queryParams.get("port"));
            boolean shutdown = Boolean.parseBoolean(queryParams.getOrDefault("shutdown", "false"));
            Integer timeout = queryParams.containsKey("timeout") ? Integer.parseInt(queryParams.get("timeout")) : null;
            Integer limit = queryParams.containsKey("limit") ? Integer.parseInt(queryParams.get("limit")) : null;
            String stringData = queryParams.getOrDefault("string", "") + "\n";

            TCPClient tcpClient = new TCPClient(shutdown, timeout, limit);
            byte[] serverResponse = tcpClient.askServer(hostname, port, stringData.getBytes());

            sendHttpResponse(new String(serverResponse, StandardCharsets.UTF_8), out);
        } catch (Exception e) {
            sendBadRequestResponse(out);
        }
    }

    private static void sendHttpResponse(String responseBody, PrintWriter out) {
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: text/plain; charset=utf-8");
        out.println("Content-Length: " + responseBody.getBytes(StandardCharsets.UTF_8).length);
        out.println();
        out.println(responseBody);
    }

    private static void sendBadRequestResponse(PrintWriter out) {
        out.println("HTTP/1.1 400 Bad Request");
        out.println("Content-Type: text/plain; charset=utf-8");
        out.println();
        out.println("Bad Request");
    }

    private static void sendNotFoundResponse(PrintWriter out) {
        out.println("HTTP/1.1 404 Not Found");
        out.println("Content-Type: text/plain; charset=utf-8");
        out.println();
        out.println("Not Found");
    }

}
