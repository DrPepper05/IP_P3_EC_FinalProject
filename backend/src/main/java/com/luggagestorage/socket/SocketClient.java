package com.luggagestorage.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class SocketClient {

    private static final Logger logger = LoggerFactory.getLogger(SocketClient.class);
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 9091;

    private final String host;
    private final int port;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public SocketClient() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public SocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() throws IOException {
        logger.info("Connecting to server at {}:{}", host, port);
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        logger.info("Connected successfully to {}:{}", host, port);
    }

    public String sendCommand(String command) throws IOException {
        if (socket == null || socket.isClosed()) {
            throw new IOException("Not connected to server");
        }

        logger.debug("Sending command: {}", command);
        out.println(command);

        String response = in.readLine();
        logger.debug("Received response: {}", response);
        return response;
    }

    public void disconnect() {
        try {
            if (out != null) {
                out.println("QUIT");
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            logger.info("Disconnected from server");
        } catch (IOException e) {
            logger.error("Error during disconnect", e);
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : DEFAULT_HOST;
        int port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;

        SocketClient client = new SocketClient(host, port);

        try {

            client.connect();

            String welcome = client.in.readLine();
            System.out.println("Server: " + welcome);

            Scanner scanner = new Scanner(System.in);
            System.out.println("\n=== Luggage Storage System Socket Client ===");
            System.out.println("Available commands: STATUS, STATS, LOCKERS, BOOKINGS, HELP, QUIT");
            System.out.println("Type a command and press Enter:\n");

            while (true) {
                System.out.print("> ");
                String command = scanner.nextLine();

                if (command.trim().isEmpty()) {
                    continue;
                }

                String response = client.sendCommand(command);
                System.out.println("Server: " + response);

                if ("QUIT".equalsIgnoreCase(command.trim())) {
                    break;
                }
            }

            scanner.close();

        } catch (IOException e) {
            logger.error("Connection error", e);
            System.err.println("Error: " + e.getMessage());
            System.err.println("Make sure the server is running on " + host + ":" + port);
        } finally {
            client.disconnect();
        }
    }

    public static void testProgrammatically() {
        SocketClient client = new SocketClient();

        try {

            client.connect();

            System.out.println("Welcome: " + client.in.readLine());

            System.out.println("\n=== Testing Commands ===\n");

            String[] commands = {"STATUS", "STATS", "LOCKERS", "BOOKINGS", "HELP"};
            for (String command : commands) {
                System.out.println("Command: " + command);
                String response = client.sendCommand(command);
                System.out.println("Response: " + response + "\n");

                Thread.sleep(500);
            }

        } catch (IOException | InterruptedException e) {
            logger.error("Error during test", e);
            System.err.println("Error: " + e.getMessage());
        } finally {
            client.disconnect();
        }
    }
}
