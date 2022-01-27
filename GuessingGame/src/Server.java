
// klass från Canvas

import java.io.*;
import java.net.*;
import java.util.HashMap;
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
    private Session curSession;
    //private final int correctNumber;

    public static void main(String[] args) {
        new Server();
    }

    public Server() {

        HashMap<String, Session> cookieMap=new HashMap<String, Session>();   //nytt

        try (ServerSocket serverSocket = new ServerSocket(this.port)) {
            System.out.println("Listening on port: " + this.port);
            createHomePageHTML();

            while (true) {
                try (Socket socket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

                    String cookie = "";
                    String line;
                    boolean postVariable = false;
                    while (!Objects.equals(line = in.readLine(), "") && line!=null) { // read

                        System.out.println(" <<< " + line); // log

                        if (line.matches("GET\\s+.*")) {  //försökt få till detta, funkar inte just nu dock.
                            System.out.println("GET");
                            //System.out.println(curSession.getCorrectNumber());
                            // process the GET request
                            //postVariable = false;
                        } else if (line.matches("POST\\s+.*")) {
                            System.out.println("POST");
                            // process the POST request

                            postVariable = true;
                        } else if (line.matches("Cookie:\\s+.*")){
                            cookie = line.split(" ")[1];
                            System.out.println("Found cookie: " + cookie);
                        }
                    }
                    //create new user
                    if (!cookie.matches("SESSION.*")){
                        System.out.println("creating new cookie!!!");
                        Random rand = new Random();
                        cookie = "SESSION" + String.valueOf(rand.nextInt(1000));  //tänker en cookie som enbart är ett nummer
                        curSession = new Session(cookie);
                        cookieMap.put(cookie, curSession);
                        System.out.println(cookie);
                    } else if (cookieMap.get(cookie) == null){
                        curSession = new Session(cookie);
                        cookieMap.put(cookie, curSession);
                    }
                    curSession = cookieMap.get(cookie);
                    if (postVariable) {
                        int gissadeTalet;
                        try {
                             gissadeTalet = Integer.parseInt(in.readLine().split("=")[1]);
                        }
                        catch (ArrayIndexOutOfBoundsException e){
                             gissadeTalet = 1000;
                        }

                        //curSession = cookieMap.get(cookie);
                        curSession.increaseGuesses();

                        if (gissadeTalet < curSession.getLowerBound() || gissadeTalet > curSession.getUpperBound()) {
                            curSession.setOutOfBounds(true);
                        }
                        else {
                            curSession.setOutOfBounds(false);
                            if (gissadeTalet < curSession.getCorrectNumber()) {
                                curSession.setLowerBound(gissadeTalet);
                            }
                            else if (gissadeTalet > curSession.getCorrectNumber()) {
                                curSession.setUpperBound(gissadeTalet);
                            }
                            else { //korrekt gissning
                                System.out.println("korrekt!");
                                curSession.setCorrectGuess();
                            }
                        }
                    }
                    updateHTML();
                    System.out.println(" >>> " + "HTTP RESPONSE"); // log
                    //out.write("HTTP RESPONSE"); // write
                    String response = "HTTP/1.1 200 OK\nSet-Cookie: "+curSession.getCookie()+"\nContent-Length: " + curHTML.length() + "\nConnection: close\nContent-Type: text/html\n\n";
                    //if (curSession.getCorrectGuess()){
                        //response = "HTTP/1.1 200 OK\nSet-Cookie: 2r2f\nContent-Length: " + curHTML.length() + "\nConnection: close\nContent-Type: text/html\n\n";
                        //cookieMap.remove(curSession.getCookie());
                    //}

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

        String endBody;
        String body;
        if (curSession.getCorrectGuess()) {
            endBody = String.format(startBody,"Correct, the correct number was " + curSession.getCorrectNumber() + " and made it in " + curSession.getNumGuesses() + "<br>", "");
            String endForm = "<form name=\"EndForm\" method=\"GET\"><input type=\"submit\" value=\"New Game\">";
            curHTML = startHead + endBody + endForm + end;
        }
        else{
            if (curSession.getOutOfBounds()){
                body = String.format(startBody, "Only numbers between " + curSession.getLowerBound() + " and " + curSession.getUpperBound(), "");
            }
            else {
                body = String.format(startBody, "Nope, guess a number between " + curSession.getLowerBound() + " and " + curSession.getUpperBound(), "You have made " + curSession.getNumGuesses() + " guesses!");
            }
            curHTML = startHead + body + startForm + end;
        }
        writeHTML();
    }

    private void createHomePageHTML() {
        String body = String.format(startBody, "Welcome to the Number Guess Game.", "Guess a number between " + lowerBound + " and " + upperBound);
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
