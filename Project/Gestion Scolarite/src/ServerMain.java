import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMain {
    private static final int SERVER_PORT = 12345;
    private static final int MAX_CLIENTS = 50;
    private static final AtomicInteger clientCounter = new AtomicInteger(0);
    private static final ConcurrentHashMap<Integer, ClientHandler> activeClients = new ConcurrentHashMap<>();
    private static volatile boolean isRunning = true;
    private static ServerSocket serverSocket;
    private static ExecutorService threadPool;

    public static void main(String[] args) {
        System.out.println("=== STARTING SCHOOL MANAGEMENT SERVER ===");
        System.out.println("Initializing server...");

        // Clean up old flag files
        cleanupFlags();

        // Add shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            gracefulShutdown();
        }));

        try {
            // Create thread pool for client handlers
            threadPool = Executors.newFixedThreadPool(MAX_CLIENTS);

            // Try to start server socket
            serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("✓ Server socket created on port " + SERVER_PORT);

            // Create server flag AFTER server is actually ready
            createServerFlag();
            System.out.println("✓ Server flag created.");
            System.out.println("✓ Waiting for client connections...");
            System.out.println("========================================");
            System.out.println("Press Ctrl+C to shutdown server gracefully");
            System.out.println("========================================");

            // Start accepting client connections
            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    int clientId = clientCounter.incrementAndGet();

                    // Create and start client handler
                    ClientHandler clientHandler = new ClientHandler(clientId, clientSocket);
                    activeClients.put(clientId, clientHandler);
                    threadPool.execute(clientHandler);

                } catch (IOException e) {
                    if (isRunning) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("✗ ERROR: Could not start server on port " + SERVER_PORT);
            System.err.println("  Reason: " + e.getMessage());
            deleteServerFlag();
            System.exit(1);
        }
    }

    private static void gracefulShutdown() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("SERVER SHUTDOWN INITIATED");
        System.out.println("=".repeat(50));

        isRunning = false;

        // Notify all connected clients
        notifyAllClientsServerShutdown();

        // Close all client connections
        closeAllClientConnections();

        // Close server socket
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                System.out.println("✓ Server socket closed.");
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e.getMessage());
            }
        }

        // Shutdown thread pool
        if (threadPool != null) {
            threadPool.shutdown();
            System.out.println("✓ Thread pool shutdown.");
        }

        // Clean up flag files
        cleanupFlags();

        System.out.println("✓ Server shutdown complete.");
        System.out.println("=".repeat(50));
    }

    private static void notifyAllClientsServerShutdown() {
        if (!activeClients.isEmpty()) {
            System.out.println("\nNotifying " + activeClients.size() + " client(s) about shutdown...");
            for (ClientHandler handler : activeClients.values()) {
                handler.sendShutdownNotification();
            }

            // Give clients time to receive notification
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void closeAllClientConnections() {
        if (!activeClients.isEmpty()) {
            System.out.println("Closing " + activeClients.size() + " active connection(s)...");
            for (ClientHandler handler : activeClients.values()) {
                handler.forceDisconnect();
            }
            activeClients.clear();
        }
    }

    private static void createServerFlag() {
        try {
            File flag = new File("server.flag");
            flag.createNewFile();
            flag.deleteOnExit();
        } catch (IOException e) {
            System.err.println("Warning: Could not create server flag");
        }
    }

    private static void deleteServerFlag() {
        File flag = new File("server.flag");
        if (flag.exists()) flag.delete();
    }

    private static void cleanupFlags() {
        new File("server.flag").delete();
        new File("client.flag").delete();
    }

    // Method to remove client when disconnected
    public static void removeClient(int clientId, String reason) {
        activeClients.remove(clientId);
        System.out.println("[-] Client " + clientId + " Disconnected [" + reason + "]");
        printActiveClients();
    }

    // Method to print currently active clients
    public static void printActiveClients() {
        if (activeClients.isEmpty()) {
            System.out.println("No active clients.");
        } else {
            System.out.print("Active clients (" + activeClients.size() + "): ");
            for (Integer id : activeClients.keySet()) {
                System.out.print("Client " + id + " ");
            }
            System.out.println();
        }
    }

    // Inner class to handle individual client connections
    private static class ClientHandler implements Runnable {
        private final int clientId;
        private final Socket clientSocket;
        private final long connectionTime;
        private volatile boolean clientRunning = true;

        public ClientHandler(int clientId, Socket clientSocket) {
            this.clientId = clientId;
            this.clientSocket = clientSocket;
            this.connectionTime = System.currentTimeMillis();
        }

        @Override
        public void run() {
            String clientAddress = clientSocket.getInetAddress().getHostAddress();
            System.out.println("\n[+] Client " + clientId + " Connected");
            System.out.println("   IP: " + clientAddress);
            System.out.println("   Time: " + new java.util.Date());
            printActiveClients();

            try {
                // Send welcome message
                String welcomeMsg = "HELLO Client " + clientId + "\nSERVER_READY";
                clientSocket.getOutputStream().write(welcomeMsg.getBytes());
                clientSocket.getOutputStream().flush();

                // Set socket timeout for regular checks
                clientSocket.setSoTimeout(5000); // 5 second timeout for reads

                // Keep connection alive until client disconnects or server shuts down
                byte[] buffer = new byte[1024];
                while (isRunning && clientRunning) {
                    try {
                        // Read data from client
                        int bytesRead = clientSocket.getInputStream().read(buffer);
                        if (bytesRead == -1) {
                            // Client disconnected gracefully
                            break;
                        }

                        // Process message
                        String message = new String(buffer, 0, bytesRead).trim();
                        if (!message.isEmpty()) {
                            System.out.println("Client " + clientId + " >>> " + message);

                            // Handle ping/pong for connection monitoring
                            if (message.equals("PING")) {
                                clientSocket.getOutputStream().write("PONG\n".getBytes());
                                clientSocket.getOutputStream().flush();
                            }
                        }

                    } catch (java.net.SocketTimeoutException e) {
                        // Timeout is expected - just continue checking
                        continue;
                    } catch (IOException e) {
                        // Client disconnected unexpectedly
                        break;
                    }
                }

            } catch (IOException e) {
                System.err.println("Error with Client " + clientId + ": " + e.getMessage());
            } finally {
                closeConnection();
            }
        }

        public void sendShutdownNotification() {
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    String shutdownMsg = "SERVER_SHUTDOWN\n";
                    clientSocket.getOutputStream().write(shutdownMsg.getBytes());
                    clientSocket.getOutputStream().flush();
                    System.out.println("  Sent shutdown notification to Client " + clientId);
                }
            } catch (IOException e) {
                // Client may already be disconnected
            }
        }

        public void forceDisconnect() {
            clientRunning = false;
            closeConnection();
        }

        private void closeConnection() {
            if (clientSocket != null && !clientSocket.isClosed()) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    // Ignore close errors
                }
            }

            if (clientRunning) { // Only log if not already removed
                long duration = (System.currentTimeMillis() - connectionTime) / 1000;
                ServerMain.removeClient(clientId, duration + "s session");
            }
            clientRunning = false;
        }
    }
}