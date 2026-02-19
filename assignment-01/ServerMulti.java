import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Multi-threaded calculator server using a thread per connection.
 */
public class ServerMulti {
    private static final int PORT = 5000;
    private static final AtomicInteger NEXT_CLIENT_ID = new AtomicInteger(1);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Accepts clients and dispatches each connection to a worker thread.
     *
     * @param args unused
     */
    public static void main(String[] args) {
        System.out.println("ServerMulti starting on port " + PORT);

        // Accept clients continuously and handle each on a separate thread.
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                int clientId = NEXT_CLIENT_ID.getAndIncrement();
                Thread worker = new Thread(() -> handleClient(socket, clientId));
                worker.start();
            }
        } catch (IOException ex) {
            System.out.println("Server error: " + ex.getMessage());
        }
    }

    /**
     * Handles a single client connection.
     *
     * @param socket connected client socket
     */
    private static void handleClient(Socket socket, int clientId) {
        try (Socket client = socket;
             DataInputStream dis = new DataInputStream(client.getInputStream());
             DataOutputStream dos = new DataOutputStream(client.getOutputStream())) {

            System.out.println("[" + timestamp() + "] Client #" + clientId + " connected: " + client.getInetAddress());
            while (true) {
                String input = dis.readUTF();
                if (input == null) {
                    break;
                }
                if (input.equalsIgnoreCase("Quit")) {
                    System.out.println("[" + timestamp() + "] Client #" + clientId + " requested disconnect");
                    break;
                }
                // Evaluate the expression and return the result.
                String result = evaluate(input);
                dos.writeUTF(result);
                System.out.println("[" + timestamp() + "] Client #" + clientId + " request: " + input + " -> " + result);
            }
            System.out.println("[" + timestamp() + "] Client #" + clientId + " disconnected");
        } catch (IOException ex) {
            System.out.println("Client #" + clientId + " error: " + ex.getMessage());
        }
    }

    /**
     * Evaluates a single calculator expression.
     *
     * @param input expression in the form "<number> <op> <number>"
     * @return the result or an error message
     */
    private static String evaluate(String input) {
        String[] parts = input.trim().split("\\s+");
        if (parts.length != 3) {
            return "Error: use '<number> <op> <number>'";
        }

        double left;
        double right;
        try {
            left = Double.parseDouble(parts[0]);
            right = Double.parseDouble(parts[2]);
        } catch (NumberFormatException ex) {
            return "Error: invalid number";
        }

        String op = parts[1];
        switch (op) {
            case "+":
                return Double.toString(left + right);
            case "-":
                return Double.toString(left - right);
            case "*":
                return Double.toString(left * right);
            case "/":
                if (right == 0.0) {
                    return "Error: division by zero";
                }
                return Double.toString(left / right);
            default:
                return "Error: unsupported operator";
        }
    }

    private static String timestamp() {
        return LocalDateTime.now().format(TIME_FORMAT);
    }
}
