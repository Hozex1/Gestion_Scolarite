import ui.LoginFrame;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

public class ClientMain {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;
    private static final int MAX_WAIT_TIME = 30000; // 30 seconds max wait

    public static void main(String[] args) {
        System.out.println("=== SCHOOL MANAGEMENT CLIENT ===");
        System.out.println("Checking if server is running...");

        long startTime = System.currentTimeMillis();

        // Wait for server flag AND verify server is actually reachable
        while (!isServerReady()) {
            System.out.print(".");

            // Check timeout
            if (System.currentTimeMillis() - startTime > MAX_WAIT_TIME) {
                System.err.println("\n✗ TIMEOUT: Server not responding after 30 seconds");
                System.err.println("  Please start the server first!");
                System.exit(1);
            }

            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }

        System.out.println("\n✓ Server detected! Launching application...");

        // Clean up client flag if exists from previous run
        new File("client.flag").delete();

        // Launch the GUI
        javax.swing.SwingUtilities.invokeLater(() -> {
            new LoginFrame();
        });
    }

    private static boolean isServerReady() {
        // Check 1: Server flag exists
        if (!new File("server.flag").exists()) {
            return false;
        }

        // Check 2: Actually try to connect to server port
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}