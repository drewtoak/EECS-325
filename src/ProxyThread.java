import java.io.*;
import java.net.*;

/**
 * This class runs the thread for the given port that is making requests to the proxy server, and returns
 * a response for the request.
 *
 * @author Andrew Hwang
 * EECS 325
 * Project 1
 */
public class ProxyThread extends Thread{

    private Socket client;
    private Socket server = null;

    public ProxyThread(Socket client) {
        this.client = client;
    }

    /**
     * This method runs the Thread and creates input and output streams of data from and to the client.
     */
    public void run() {
        // Initialize a byte array to hold the requested data from the client.
        byte[] request = new byte[1024*16];
        try {
            // Make an input stream to get a stream of data from the client socket.
            InputStream streamFromClient = client.getInputStream();
            OutputStream streamToServer = null;

            String hostName = "";

            // Set an int variable to know the number of bytes of data within a requested stream from the client.
            int requestLength;
            // Constantly read the input stream until there are no more requests from the client.
            while ((requestLength = streamFromClient.read(request)) != -1) {
                // Get the host name found with in the requested stream.
                hostName = getHostName(request, requestLength, hostName);

                // Get the host address found with the host name.
                InetAddress hostAddress = getHostAddress(hostName);

                // Set global Socket variable server to be a Socket with the given host address and 80 as the port#, for http.
                server = new Socket(hostAddress, 80);

                // Set the output stream to the server
                streamToServer = server.getOutputStream();
                int i = 0;
                // Iterate through the Input stream from the client & write & flush every byte of data to the output stream
                while (i < requestLength) {
                    streamToServer.write(request[i]);
                    streamToServer.flush();
                    i++;
                }

                // Run a separate thread to get the response from the server.
                ResponseThread response = new ResponseThread(client, server);
                response.start();
            }
            // Close the connection with the output stream to the server, since there are no more requests.
            streamToServer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (server != null) {
                    server.close();
                }
                if (client != null) {
                    client.close();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method gets the host name from the request stream.
     * @param buffer -- the byte data of the request stream
     * @param length -- the number of bytes in the byte data array
     * @param prevHostName -- the name of the previous host
     * @return the host name from the request
     * @throws IOException
     */
    private String getHostName(byte[] buffer, int length, String prevHostName) throws IOException {
        // Convert the bytes of data to a String
        String clientRequest = new String(buffer, 0, length);

        System.out.println("Request:");
        System.out.println(clientRequest);

        String[] requestLines = clientRequest.split("\n");

        int index = 0;
        // Iterate through the String and return the host name when found.
        while (index < requestLines.length) {
            if (requestLines[index].contains("Host:")) {
                String[] contents = requestLines[1].split(" ");

                String hostName = contents[1].trim();
                System.out.println("Host Name: " + hostName);
                return hostName;
            }
            index++;
        }

        System.err.println("The Request is missing its hostname");
        return prevHostName;
    }

    /**
     * Get the address based off the found host name.
     * @param hostName -- the host name within the request
     * @return the address of the host server
     * @throws UnknownHostException
     */
    private InetAddress getHostAddress(String hostName) throws UnknownHostException {
        if (proxyd.cachedAddress.containsKey(hostName)) {
            return proxyd.cachedAddress.get(hostName);
        }
        else {
            InetAddress address = InetAddress.getByName(hostName);

            proxyd.cachedAddress.put(hostName,address);

            String hostAddress = address.getHostAddress();
            System.out.println("Host Address: " + hostAddress);
            System.out.flush();

            return address;
        }
    }

    /**
     * This thread retrieves the response from the host server.
     */
    private static class ResponseThread extends Thread {
        private final Socket client;
        private final Socket server;

        public ResponseThread(Socket client, Socket server) {
            this.client = client;
            this.server = server;
        }

        /**
         * This method runs through the thread and receives data from the host server,
         * and sends those bytes of data to the client.
         */
        public void run() {
            byte[] reply = new byte[1024*16];
            try {
                // The input stream of data from the server, based off the client's request.
                InputStream streamFromServer = server.getInputStream();
                // The output stream of data to the client, from the input stream.
                OutputStream streamToClient = client.getOutputStream();

                int responseLength;
                // Read the replies from the server host.
                while ((responseLength = streamFromServer.read(reply)) != -1) {
                    int i = 0;
                    // Iterate through the byte array of data & write & flush each byte of data to the client.
                    while (i < responseLength) {
                        streamToClient.write(reply[i]);
                        streamToClient.flush();
                        i++;
                    }
                }
                // Close the connection with the client if no more responses are coming from the server host.
                streamToClient.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
