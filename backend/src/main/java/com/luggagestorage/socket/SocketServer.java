package com.luggagestorage.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Socket Server implementation using java.net.ServerSocket.
 * Listens for client connections and handles them in separate threads.
 * Part of Requirement 4 (1p): Use sockets for client-server communication.
 * Also contributes to Requirement 3 (1p): Use threads to handle multiple clients.
 */
@Component
public class SocketServer {

    private static final Logger logger = LoggerFactory.getLogger(SocketServer.class);

    @Value("${socket.server.port:9091}")
    private int port;

    @Value("${socket.server.enabled:true}")
    private boolean enabled;

    @Value("${socket.server.thread.pool.size:10}")
    private int threadPoolSize;

    private final SocketService socketService;
    private final ExecutorService executorService;
    private final AtomicBoolean running;
    private final AtomicInteger clientCounter;
    private ServerSocket serverSocket;
    private Thread serverThread;

    @Autowired
    public SocketServer(SocketService socketService) {
        this.socketService = socketService;
        this.executorService = Executors.newFixedThreadPool(10); // Thread pool for handling multiple clients
        this.running = new AtomicBoolean(false);
        this.clientCounter = new AtomicInteger(0);
    }

    /**
     * Start the socket server automatically after bean initialization.
     */
    @PostConstruct
    public void start() {
        if (!enabled) {
            logger.info("Socket server is disabled in configuration");
            return;
        }

        if (running.get()) {
            logger.warn("Socket server is already running");
            return;
        }

        serverThread = new Thread(this::runServer, "SocketServer-Main");
        serverThread.setDaemon(true);
        serverThread.start();

        logger.info("Socket server started on port {}", port);
    }

    /**
     * Main server loop that accepts client connections.
     */
    private void runServer() {
        try {
            serverSocket = new ServerSocket(port);
            running.set(true);
            logger.info("Socket server listening on port {}", port);

            while (running.get() && !Thread.currentThread().isInterrupted()) {
                try {
                    // Accept client connection (blocking call)
                    Socket clientSocket = serverSocket.accept();
                    int clientId = clientCounter.incrementAndGet();
                    logger.info("New client connection accepted: Client #{} from {}",
                            clientId, clientSocket.getInetAddress());

                    // Handle client in separate thread using thread pool
                    executorService.submit(() -> handleClient(clientSocket, clientId));

                } catch (SocketException e) {
                    if (running.get()) {
                        logger.error("Socket exception in server loop", e);
                    } else {
                        logger.info("Server socket closed");
                    }
                    break;
                } catch (IOException e) {
                    logger.error("Error accepting client connection", e);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to start socket server on port {}", port, e);
        } finally {
            running.set(false);
        }
    }

    /**
     * Handle individual client connection.
     * Runs in a separate thread from the thread pool.
     *
     * @param clientSocket The client socket
     * @param clientId     Unique client identifier
     */
    private void handleClient(Socket clientSocket, int clientId) {
        logger.info("Handling Client #{} in thread: {}", clientId, Thread.currentThread().getName());

        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            // Send welcome message
            out.println("{\"message\":\"Welcome to Luggage Storage System Socket Server\",\"clientId\":" + clientId + ",\"commands\":\"Type HELP for available commands\"}");

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                logger.info("Client #{} sent command: {}", clientId, inputLine);

                // Check for quit command
                if ("QUIT".equalsIgnoreCase(inputLine.trim())) {
                    out.println("{\"message\":\"Goodbye!\"}");
                    logger.info("Client #{} requested disconnect", clientId);
                    break;
                }

                // Process command using SocketService
                String response = socketService.processCommand(inputLine);
                out.println(response);
            }

        } catch (IOException e) {
            logger.error("Error handling Client #{}", clientId, e);
        } finally {
            try {
                clientSocket.close();
                logger.info("Client #{} connection closed", clientId);
            } catch (IOException e) {
                logger.error("Error closing client socket for Client #{}", clientId, e);
            }
        }
    }

    /**
     * Stop the socket server gracefully.
     */
    @PreDestroy
    public void stop() {
        if (!running.get()) {
            logger.info("Socket server is not running");
            return;
        }

        logger.info("Stopping socket server...");
        running.set(false);

        try {
            // Close server socket to stop accepting new connections
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            // Shutdown executor service
            executorService.shutdown();
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.warn("Executor service did not terminate in time, forcing shutdown");
                executorService.shutdownNow();
            }

            // Wait for server thread to finish
            if (serverThread != null && serverThread.isAlive()) {
                serverThread.interrupt();
                serverThread.join(2000);
            }

            logger.info("Socket server stopped successfully");
        } catch (IOException e) {
            logger.error("Error closing server socket", e);
        } catch (InterruptedException e) {
            logger.error("Interrupted while stopping socket server", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Check if server is running.
     *
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Get the port number.
     *
     * @return port number
     */
    public int getPort() {
        return port;
    }

    /**
     * Get the number of connected clients.
     *
     * @return total number of clients served
     */
    public int getClientCount() {
        return clientCounter.get();
    }
}
