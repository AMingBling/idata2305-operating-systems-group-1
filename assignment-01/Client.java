import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Scanner;

/**
 * Simple calculator client that sends requests to a socket server.
 */
public class Client {
	private static final String DEFAULT_HOST = "127.0.0.1";
	private static final int DEFAULT_PORT = 5000;

	/**
	 * Connects to the server and sends calculator requests typed by the user.
	 *
	 * @param args optional host and port
	 */
	public static void main(String[] args) {
		String host = DEFAULT_HOST;
		int port = DEFAULT_PORT;
		String autoRequest = null;

		// Allow optional host/port overrides.
		if (args.length > 0) {
			host = args[0];
		}
		if (args.length > 1) {
			try {
				port = Integer.parseInt(args[1]);
			} catch (NumberFormatException ex) {
				System.out.println("Invalid port, using default " + DEFAULT_PORT);
			}
		}
		if (args.length > 2) {
			autoRequest = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
		}

		// Connect to the server and set up I/O streams.
		try (Socket socket = new Socket(host, port);
			 DataInputStream dis = new DataInputStream(socket.getInputStream());
			 DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

			socket.setSoTimeout(1000);
			System.out.println("Connected to server at " + host + ":" + port);

			if (autoRequest != null && !autoRequest.isBlank()) {
				dos.writeUTF(autoRequest);
				String response = readServerResponse(dis);
				System.out.println("Answer = " + response);
			}

			try (Scanner scanner = new Scanner(System.in)) {
			while (true) {
				System.out.println("Enter: <number> <op> <number> (or Quit to quit)");
				String input = scanner.nextLine();
				// Send the raw request to the server.
				dos.writeUTF(input);

				if (input.equalsIgnoreCase("Quit")) {
					break;
				}

				// Read the server response and display it.
				String response = readServerResponse(dis);
				System.out.println("Answer = " + response);
			}
			}
		} catch (IOException ex) {
			System.out.println("Client error: " + ex.getMessage());
		}
	}

	private static String readServerResponse(DataInputStream dis) throws IOException {
		String response;
		boolean waitingPrinted = false;
		while (true) {
			try {
				response = dis.readUTF();
				break;
			} catch (SocketTimeoutException ex) {
				if (!waitingPrinted) {
					System.out.println("Waiting...");
					waitingPrinted = true;
				}
			}
		}
		return response;
	}
}