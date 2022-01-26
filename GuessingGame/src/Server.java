
// klass från Canvas

import java.io.*;
import java.net.*;
import java.util.Objects;
import java.util.Random;

public class Server {

    private final int port = 8989;
    private String startHead = "<!DOCTYPE html> <html lang=\"En\"><head><meta charset=\"UTF-8\"><title>%s</title></head>";
    private String startBody = "<body>%s<br>%s %d %s %d";
    private String startForm = "<form name=\"guessform\" method=\"POST\"> <input type=\"text\" name=\"gissadeTalet\"" +
            "autofocus=\"\"><input type=\"submit\" value=\"Guess\">";
    private String end = "</form> </body> </html>";
    private String curHTML;
    private int lowerBound = 1;
    private int upperBound = 100;
    private int numGuesses = 0;
    private boolean correctGuess = false;
    private int correctNumber;

    public static void main(String[] args) {
        new Server();
    }

    public Server() {

        correctNumber = new Random().nextInt(lowerBound+upperBound)+lowerBound;
        System.out.println(correctNumber);
        try (ServerSocket serverSocket = new ServerSocket(this.port)) {
            System.out.println("Listening on port: " + this.port);
            createHTML();

            while (true) {

                try (Socket socket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

                    boolean postVaraible = false;
                    String line;
                    while (!Objects.equals(line = in.readLine(), "")) { // read

                        System.out.println(" <<< " + line); // log

                        if (line.matches("GET\\s+.*")) {
                            System.out.println("hej");
                            // process the GET request
                            postVaraible = false;
                        } else if (line.matches("POST\\s+.*")) {
                            System.out.println("post");
                            // process the POST request
                            numGuesses++;
                            postVaraible = true;
                        }
                    }
                    if (postVaraible){
                        int gissadeTalet = Integer.parseInt(in.readLine().split("=")[1]);
                        if (gissadeTalet < correctNumber && gissadeTalet > lowerBound) {
                            lowerBound = gissadeTalet;
                        }
                        else if (gissadeTalet > correctNumber && gissadeTalet < upperBound) {
                            upperBound = gissadeTalet;
                        }
                        else if (gissadeTalet == correctNumber){ //korrekt gissning
                            System.out.println("korrekt!");
                            correctGuess = true;
                        }
                        updateHTML();
                    }

                    System.out.println("slut");

                    System.out.println(" >>> " + "HTTP RESPONSE"); // log
                    //out.write("HTTP RESPONSE"); // write
                    String response = "HTTP/1.1 200 OK\nDate: Mon, 15 Jan 2018 22:14:15 GMT\nContent-Length: "+curHTML.length()+"\nConnection: close\nContent-Type: text/html\n\n";
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

    private void updateHTML() {
        String head = String.format(startHead, "Number guess game");
        String body = String.format(startBody, "Welcome to the Number Guess Game.", "Guess a number between", lowerBound, "and", upperBound);
        //funkar inte än
        //String body = String.format(startBody, "Nope, guess a number between", lowerBound, "and", upperBound, "You have made", numGuesses, "guesses");
        if (correctGuess) {
            //funkar inte än
            body = String.format(startBody, "Correct, the correct number was " + String.valueOf(correctNumber), "Guess a number between", lowerBound, "and", upperBound);
        }
        curHTML = head + body + startForm + end;
        writeHTML();
    }

    private void createHTML() {
        String head = String.format(startHead, "Number guess game");
        String body = String.format(startBody, "Welcome to the Number Guess Game.", "Guess a number between", lowerBound, "and", upperBound);
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
