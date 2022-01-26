
// klass från Canvas

import java.io.*;
import java.net.*;
import java.util.Objects;
import java.util.Random;

public class Server {

    private final int port = 8989;
    private String startHead = "<!DOCTYPE html> <html lang=\"En\"><head><meta charset=\"UTF-8\"><title>Number guess game</title></head>";
    //private String gameHead = "<!DOCTYPE html> <html lang=\"En\"><head><meta charset=\"UTF-8\"><title>Nope, guess a number between €</title></head>";
    private String startBody = "<body>%s<br>%s" ;
    private String startForm = "<form name=\"guessform\" method=\"POST\" onsubmit=\"setTimeout(function(){window.location.reload();},10);\"> <input type=\"text\" name=\"gissadeTalet\"" +
            "autofocus=\"\"><input type=\"submit\" value=\"Guess\">";
    private String end = "</form> </body> </html>";
    private String curHTML;
    private int lowerBound = 1;
    private int upperBound = 100;
    private int numGuesses = 0;
    private boolean correctGuess = false;
    private boolean outOfBounds = false;
    private final int correctNumber;

    public static void main(String[] args) {
        new Server();
    }

    public Server() {

        correctNumber = new Random().nextInt(lowerBound+upperBound)+lowerBound;
        System.out.println(correctNumber);
        try (ServerSocket serverSocket = new ServerSocket(this.port)) {
            System.out.println("Listening on port: " + this.port);
            createHomePageHTML();

            while (true) {

                try (Socket socket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {


                    String line;
                    boolean postVariable = false;
                    while (!Objects.equals(line = in.readLine(), "")) { // read

                        System.out.println(" <<< " + line); // log

                        if (line.matches("GET\\s+.*")) {
                            System.out.println("GET");
                            // process the GET request
                            //postVariable = false;
                        } else if (line.matches("POST\\s+.*")) {
                            System.out.println("POST");
                            // process the POST request
                            numGuesses++;
                            postVariable = true;
                        }
                    }
                    if (postVariable) {

                        int gissadeTalet = Integer.parseInt(in.readLine().split("=")[1]);

                        if (gissadeTalet < lowerBound || gissadeTalet > upperBound) {
                            outOfBounds = true;
                        }
                        else {
                            outOfBounds = false;
                            if (gissadeTalet < correctNumber) {
                                lowerBound = gissadeTalet;
                            }
                            else if (gissadeTalet > correctNumber) {
                                upperBound = gissadeTalet;
                            }
                            else { //korrekt gissning
                                System.out.println("korrekt!");
                                correctGuess = true;
                            }
                        }
                        updateHTML();
                    }
                    System.out.println(" >>> " + "HTTP RESPONSE"); // log
                    //out.write("HTTP RESPONSE"); // write
                    String response = "HTTP/1.1 200 OK\nDate: Mon, 15 Jan 2018 22:14:15 GMT\nContent-Length: " + curHTML.length() + "\nConnection: close\nContent-Type: text/html\n\n";
                    response += curHTML;
                    out.write(response);
                    out.flush(); // flush

                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            System.err.println("Could not listen on port: " + this.port);
            System.exit(1);
        }
    }

    private void updateHTML() {

        String body;
        if (correctGuess) {
            body = String.format(startBody,"Correct, the correct number was " + correctNumber + " and made it in " + numGuesses + " guesses!","<br><a href=\"localhost:"+port+"\"> New Game</a");
            }
        else{
            if (outOfBounds){
                body = String.format(startBody, "Only numbers between " + lowerBound + " and " + upperBound, "");
            }
            else {
                body = String.format(startBody, "Nope, guess a number between " + lowerBound + " and " + upperBound, "You have made " + numGuesses + " guesses!");
            }
        }

        curHTML = startHead + body + startForm + end;
        writeHTML();
    }

    private void createHomePageHTML() {
        String body = String.format(startBody, "Welcome to the Number Guess Game.", "Guess a number between " + lowerBound+ " and " + upperBound);
        curHTML = startHead + body + startForm + end;
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
