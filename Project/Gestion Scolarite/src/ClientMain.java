import ui.LoginFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientMain {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;
    private static final int MAX_WAIT_SECONDS = 30;
    private static final int RECONNECT_DELAY = 5000; // 5 seconds

    private static Socket serverSocket = null;
    private static PrintWriter out = null;
    private static BufferedReader in = null;
    private static AtomicBoolean isConnected = new AtomicBoolean(false);
    private static AtomicBoolean shutdownRequested = new AtomicBoolean(false);
    private static Thread heartbeatThread = null;

    public static void main(String[] args) {
        System.out.println("=== SCHOOL MANAGEMENT CLIENT ===");

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            shutdownClient();
        }));

        // Try to connect to server
        if (!connectToServer()) {
            return; // Connection failed
        }

        System.out.println("Launching application...");
        System.out.println("----------------------------------------");

        // Launch the GUI
        SwingUtilities.invokeLater(() -> {
            new LoginFrame();
        });
    }

    private static boolean connectToServer() {
        System.out.println("Connecting to server...");

        // Clean up old client flag
        new File("client.flag").delete();

        long startTime = System.currentTimeMillis();
        boolean connected = false;

        // Try to connect to server
        for (int seconds = 0; seconds < MAX_WAIT_SECONDS; seconds++) {
            System.out.print(".");

            try {
                // Try to connect
                serverSocket = new Socket(SERVER_HOST, SERVER_PORT);
                out = new PrintWriter(serverSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
                connected = true;
                break;
            } catch (IOException e) {
                // Server not ready yet
                if (seconds % 5 == 0 && seconds > 0) {
                    System.out.println(" [" + seconds + "s]");
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            }
        }

        if (!connected) {
            showServerNotRunningError();
            return false;
        }

        // Connection successful
        long connectionTime = System.currentTimeMillis() - startTime;
        System.out.println("\n✓ Connected to server! (" + (connectionTime/1000.0) + "s)");

        try {
            // Read server welcome message
            String welcomeMsg = in.readLine();
            if (welcomeMsg != null) {
                System.out.println("Server: " + welcomeMsg);
            }

            // Send client identification
            out.println("CLIENT_READY");

            // Start heartbeat monitoring
            startHeartbeatMonitor();

            isConnected.set(true);
            return true;

        } catch (IOException e) {
            System.err.println("Error communicating with server: " + e.getMessage());
            showServerConnectionLost();
            return false;
        }
    }

    private static void startHeartbeatMonitor() {
        heartbeatThread = new Thread(() -> {
            while (!shutdownRequested.get() && isConnected.get()) {
                try {
                    Thread.sleep(3000); // Send heartbeat every 3 seconds

                    if (isConnected.get() && out != null && serverSocket != null && !serverSocket.isClosed()) {
                        // Send ping
                        out.println("PING");
                        out.flush();

                        // Set timeout for response
                        serverSocket.setSoTimeout(2000); // 2 second timeout

                        // Wait for pong
                        String response = in.readLine();
                        if (response == null || !response.startsWith("PONG")) {
                            System.err.println("Heartbeat failed - server may be down");
                            handleServerDisconnection();
                            break;
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Heartbeat error: " + e.getMessage());
                    handleServerDisconnection();
                    break;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        heartbeatThread.setDaemon(true);
        heartbeatThread.start();

        // Start server message listener
        Thread messageListener = new Thread(() -> {
            try {
                while (!shutdownRequested.get() && isConnected.get()) {
                    String message = in.readLine();
                    if (message == null) {
                        // Server closed connection
                        System.err.println("Server closed the connection");
                        handleServerDisconnection();
                        break;
                    }

                    if (message.equals("SERVER_SHUTDOWN")) {
                        System.out.println("\n⚠ Server is shutting down...");
                        showServerShutdownMessage();
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(null,
                                    "Server is shutting down.\nThe application will close.",
                                    "Server Notification",
                                    JOptionPane.WARNING_MESSAGE);
                        });
                        Thread.sleep(2000);
                        System.exit(0);
                    }

                    System.out.println("Server broadcast: " + message);
                }
            } catch (IOException e) {
                if (!shutdownRequested.get()) {
                    handleServerDisconnection();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        messageListener.setDaemon(true);
        messageListener.start();
    }

    private static void handleServerDisconnection() {
        if (isConnected.compareAndSet(true, false)) {
            System.err.println("\n✗ Connection to server lost!");

            SwingUtilities.invokeLater(() -> {
                int option = JOptionPane.showConfirmDialog(null,
                        "Connection to server lost!\n\nWould you like to try to reconnect?",
                        "Connection Lost",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.ERROR_MESSAGE);

                if (option == JOptionPane.YES_OPTION) {
                    new Thread(() -> {
                        try {
                            System.out.println("Attempting to reconnect in 5 seconds...");
                            Thread.sleep(RECONNECT_DELAY);

                            // Try to reconnect
                            cleanupConnection();
                            if (connectToServer()) {
                                JOptionPane.showMessageDialog(null,
                                        "Successfully reconnected to server!",
                                        "Reconnection Successful",
                                        JOptionPane.INFORMATION_MESSAGE);
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Application will now close.",
                            "Goodbye",
                            JOptionPane.INFORMATION_MESSAGE);
                    System.exit(1);
                }
            });
        }
    }

    private static void showServerShutdownMessage() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null,
                    "The server is shutting down.\nPlease save your work.\nApplication will close in 2 seconds.",
                    "Server Shutdown",
                    JOptionPane.WARNING_MESSAGE);
        });
    }

    private static void showServerConnectionLost() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null,
                    "Lost connection to server.\nThe application may not function properly.",
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
        });
    }

    private static void showServerNotRunningError() {
        System.err.println("\n✗ ERROR: Could not connect to server after " + MAX_WAIT_SECONDS + " seconds!");
        System.err.println("\nPlease start the server first:");
        System.err.println("1. Open Command Prompt");
        System.err.println("2. Run: java -cp \"out;lib\\*\" ServerMain");
        System.err.println("3. Then run this client again");

        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    null,
                    "<html><b>Server Not Running!</b><br><br>" +
                            "Please start the server first:<br><br>" +
                            "1. Open Command Prompt<br>" +
                            "2. Run: <code>java -cp \"out;lib\\*\" ServerMain</code><br>" +
                            "3. Then run this client again</html>",
                    "Connection Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        });

        try { Thread.sleep(3000); } catch (InterruptedException e) {}
        System.exit(1);
    }

    private static void cleanupConnection() {
        isConnected.set(false);
        shutdownRequested.set(true);

        if (heartbeatThread != null) {
            heartbeatThread.interrupt();
        }

        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            // Ignore cleanup errors
        }
    }

    private static void shutdownClient() {
        System.out.println("\nClient shutting down...");
        shutdownRequested.set(true);
        cleanupConnection();
        System.out.println("Client shutdown complete.");
    }
}