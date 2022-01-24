
// klass från Canvas

import java.io.*;
import java.net.*;

public class Server {

    private final int port = 8989;

    public static void main(String[] args) {
        new Server();
    }

    public Server() { 
        try (ServerSocket serverSocket = new ServerSocket(this.port)) {
            System.out.println("Listening on port: " + this.port);

            while (true) {

                try (Socket socket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

                    String line;
                    while ((line = in.readLine()) != null) { // read
                        System.out.println(" <<< " + line); // log

                        if (line.matches("GET\\s+.*")) {
                            // process the GET request
                            System.out.println("get");
                        } else if (line.matches("POST\\s+.*")) {
                            // process the POST request
                            System.out.println("post");
                        }
                    }

                    System.out.println(" >>> " + "HTTP RESPONSE"); // log
                    out.write("HTTP RESPONSE"); // write
                    out.flush(); // flush

                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }

            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.err.println("Could not listen on port: " + this.port);
            System.exit(1);
        }
    }

}
