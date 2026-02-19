import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Single-threaded calculator server using blocking sockets.
 */
public class Server {
    private static final int PORT = 5000;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Accepts one client and handles requests until the client quits.
     *
     * @param args unused
     */
    public static void main(String[] args) {
        System.out.println("Server starting on port " + PORT);

        // Accept one client at a time, and keep the server alive for the next client.
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                try (Socket socket = serverSocket.accept();
                        DataInputStream dis = new DataInputStream(socket.getInputStream());
                        DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

                    System.out.println("[" + timestamp() + "] Client connected: " + socket.getInetAddress());
                    while (true) {
                        String input = dis.readUTF();
                        if (input == null) {
                            break;
                        }
                        if (input.equalsIgnoreCase("Quit")) {
                            System.out.println("[" + timestamp() + "] Client requested disconnect: " + socket.getInetAddress());
                            break;
                        }
                        // Evaluate the expression and return the result.
                        String result = evaluate(input);
                        dos.writeUTF(result);
                        System.out.println("[" + timestamp() + "] Request from " + socket.getInetAddress() + ": " + input + " -> " + result);
                    }
                } catch (IOException ex) {
                    System.out.println("[" + timestamp() + "] Client disconnected/error: " + ex.getMessage());
                }
            }
        } catch (IOException ex) {
            System.out.println("Server error: " + ex.getMessage());
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