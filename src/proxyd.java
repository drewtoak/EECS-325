import java.io.*;
import java.net.*;

/**
 * This is a simple proxy server.
 *
 * @author Andrew Hwang
 * EECS 325
 * Project 1
 */
public class proxyd {

    /**
     * The main method that takes in an argument from the terminal and listens for a request from the
     * web browser using the proxy.
     * @param args -- "-port 5032"
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        // Make a variable called serverSocket and initially set it to null.
        ServerSocket serverSocket = null;

        // My port number; I am student #32.
        int port = 5032;

        // Parse the argument from the terminal to set the port number to the given port number.
        if (0 < args.length) {
            port = Integer.parseInt(args[1]);
        }

        // Try to set the Server Socket to a new Server Socket with the specified port.
        try {
            serverSocket = new ServerSocket(port);
            System.out.println(serverSocket);

            System.out.println("Waiting for request.");
            System.out.println("port number:" + port);
        }
        // Catch When the Server Socket could not use the given port.
        catch (IOException e) {
            System.err.println("Couldn't listen to the port:" + port);
            System.exit(-1);
        }

        // Listen for a request, and when a request arrives, start the Proxy thread.
        while (true) {
            Socket client = serverSocket.accept();
            System.out.println("The socket being used: " + client);

            new ProxyThread(client).start();
        }
    }
}
