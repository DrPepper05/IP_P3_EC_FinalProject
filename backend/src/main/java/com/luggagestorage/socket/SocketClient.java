package com.luggagestorage.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Socket Client implementation for testing socket communication.
 * Connects to the SocketServer and allows sending commands.
 * Part of Requirement 4 (1p): Use sockets for client-server communication.
 *
 * Usage:
 * - Run as main method: java com.luggagestorage.socket.SocketClient
 * - Or instantiate and use programmatically
 */
public class SocketClient {

    private static final Logger logger = LoggerFactory.getLogger(SocketClient.class);
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 9091;

    private final String host;
    private final int port;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    /**
     * Create a socket client with default host and port.
     */
    public SocketClient() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    /**
     * Create a socket client with custom host and port.
     *
     * @param host Server host
     * @param port Server port
     */
    public SocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Connect to the server.
     *
     * @throws IOException if connection fails
     */
    public void connect() throws IOException {
        logger.info("Connecting to server at {}:{}", host, port);
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        logger.info("Connected successfully to {}:{}", host, port);
    }

    /**
     * Send a command to the server and return the response.
     *
     * @param command The command to send
     * @return Server response
     * @throws IOException if communication fails
     */
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

    /**
     * Disconnect from the server.
     */
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

    /**
     * Check if client is connected.
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    /**
     * Main method for interactive testing.
     *
     * @param args Command line arguments (optional: host port)
     */
    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : DEFAULT_HOST;
        int port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;

        SocketClient client = new SocketClient(host, port);

        try {
            // Connect to server
            client.connect();

            // Read welcome message
            String welcome = client.in.readLine();
            System.out.println("Server: " + welcome);

            // Interactive command loop
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

                // Send command and receive response
                String response = client.sendCommand(command);
                System.out.println("Server: " + response);

                // Exit if QUIT command
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

    /**
     * Example programmatic usage.
     */
    public static void testProgrammatically() {
        SocketClient client = new SocketClient();

        try {
            // Connect
            client.connect();

            // Read welcome message
            System.out.println("Welcome: " + client.in.readLine());

            // Test commands
            System.out.println("\n=== Testing Commands ===\n");

            String[] commands = {"STATUS", "STATS", "LOCKERS", "BOOKINGS", "HELP"};
            for (String command : commands) {
                System.out.println("Command: " + command);
                String response = client.sendCommand(command);
                System.out.println("Response: " + response + "\n");

                // Wait a bit between commands
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
