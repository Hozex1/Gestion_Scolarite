import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

public class ServerMain {
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        System.out.println("=== Starting SCHOOL MANAGEMENT SERVER ===");

        // Clean up old flag files
        cleanupFlags();

        try {
            // Try to start server socket (actual network server)
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("✓ Server socket created on port " + SERVER_PORT);

            // Create server flag AFTER server is actually ready
            createServerFlag();
            System.out.println("✓ Server flag created. Waiting for clients...");

            // Keep server running
            while (true) {
                // Accept client connections (you can expand this)
                // serverSocket.accept();
                Thread.sleep(1000);
            }

        } catch (IOException e) {
            System.err.println("✗ ERROR: Could not start server on port " + SERVER_PORT);
            System.err.println("  Reason: " + e.getMessage());
            deleteServerFlag();
            System.exit(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void createServerFlag() {
        try {
            File flag = new File("server.flag");
            flag.createNewFile();
            // Delete flag when JVM shuts down
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
}