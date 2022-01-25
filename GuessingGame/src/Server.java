
// klass från Canvas

import java.io.*;
import java.net.*;
import java.util.Objects;

public class Server {

    private final int port = 8989;
    private String startHead = "<!DOCTYPE html> <html lang=\"En\"><head><meta charset=\"UTF-8\"><title>%s</title></head>";
    private String startBody = "<body>%s<br>%s %d %s %d" ;
    private String startForm = "<form name=\"guessform\" method=\"POST\"> <input type=\"text\" name=\"gissadeTalet\"" +
            "autofocus=\"\"><input type=\"submit\" value=\"Guess\">";
    private String end = "</form> </body> </html>";
    private String curHTML;

    public static void main(String[] args) {
        new Server();
    }

    public Server() { 
        try (ServerSocket serverSocket = new ServerSocket(this.port)) {
            System.out.println("Listening on port: " + this.port);
            createFirstHTML();

            while (true) {

                try (Socket socket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

                    String line;
                    while (!Objects.equals(line = in.readLine(), "")) { // read
                        System.out.println(" <<< " + line); // log

                        if (line.matches("GET\\s+.*")) {
                            System.out.println("hej");
                            // process the GET request
                        } else if (line.matches("POST\\s+.*")) {
                            System.out.println("post");
                            // process the POST request
                        }
                    }
                    System.out.println("slut");

                    System.out.println(" >>> " + "HTTP RESPONSE"); // log
                    //out.write("HTTP RESPONSE"); // write
                    String response= "HTTP/1.1 200 OK\nDate: Mon, 15 Jan 2018 22:14:15 GMT\nContent-Length: "+curHTML.length()+"\nConnection: close\nContent-Type: text/html\n\n";
                    response += curHTML;
                    out.write(response);
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

    private void createFirstHTML() {
        String head = String.format(startHead, "Number guess game");
        String body = String.format(startBody, "Welcome to the Number Guess Game.", "Guess a number between", 1, "and", 100);
        curHTML = head + body + startForm + end;
        writeHTML();
    }

    private void writeHTML() {
        FileWriter fWriter;
        BufferedWriter writer;
        try {
            fWriter = new FileWriter("GuessingGame/src/guess.html");
            writer = new BufferedWriter(fWriter);
            writer.write(curHTML);
            writer.newLine(); //this is not actually needed for html files - can make your code more readable though
            writer.close(); //make sure you close the writer object
        } catch (Exception e) {
            //catch any exceptions here
        }
    }
}
