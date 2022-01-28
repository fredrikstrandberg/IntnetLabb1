

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
//    private String startForm = "<form name=\"guessform\" method=\"POST\" onsubmit=\"setTimeout(function(){window.location.reload();},10);\"> <input type=\"text\" name=\"gissadeTalet\"" +
//            "autofocus=\"\"><input type=\"submit\" value=\"Guess\">";
    private String startForm = "<form name=\"guessform\" method=\"POST\"> <input type=\"text\" name=\"gissadeTalet\"" +
        "autofocus=\"\"><input type=\"submit\" value=\"Guess\">";
    private String end = "</form> </body> </html>";
    private String curHTML;
    private Session curSession;
    //private final int correctNumber;
    private InetAddress serverIP = InetAddress.getLocalHost();
    HashMap<String, InetAddress> cookieCop;

    public static void main(String[] args) throws UnknownHostException {
        new Server();
    }

    public Server() throws UnknownHostException {


        HashMap<String, Session> cookieMap = new HashMap<String, Session>();   //nytt
        cookieCop = new HashMap<String, InetAddress>();

        System.out.println(serverIP);


        try (ServerSocket serverSocket = new ServerSocket(this.port)) {
            System.out.println("Listening on port: " + this.port);
            createHomePageHTML();

            while (true) {
                try (Socket socket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {


                    socket.setSoTimeout(2000);
                    String cookie = "";
                    String contentLength = "";
                    String line;
                    String getHeader = "";
                    boolean postVariable = false;

                    while (!Objects.equals(line = in.readLine(), "") && line!=null) { // read

                        System.out.println(" <<< " + line); // log
                        if (line.startsWith("GET /favicon.ico")){
                            out.close();
                            in.close();
                            continue;
                        }


                        else if (line.matches("GET\\s+.*")) {  //försökt få till detta, funkar inte just nu dock.
                            System.out.println("GET");
                            getHeader = line;
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

                        } else if (line.matches("Content-Length:\\s+.*")){
                            contentLength = line.split(" ")[1];

                    }
                    }
                    //create new user
                    if (!cookie.matches("SESSION.*")){
                        System.out.println("creating new cookie!!!");
                        cookie = createNewCookie();

                        cookieCop.put(cookie, serverIP);
                        cookieMap.put(cookie, curSession);

                    }
//                    else if (cookieMap.get(cookie) == null){   ///om webbbrowsern kommer ihåg sin gamla cookie, men vi vill starta ett nytt spel
//                        curSession = new Session(cookie);
//                        cookieMap.put(cookie, curSession);
//                        System.out.println("cookie utan session");
//                    }


                    curSession = cookieMap.get(cookie);
                    String response;
                    System.out.println(" >>> " + "HTTP RESPONSE"); // log
                    if (postVariable) {  //Hanterar post

                        if (!cookieCopChecker()){
                            in.close();
                        }

                        String payload = readPayload(in, Integer.parseInt(contentLength));

                        int gissadeTalet;
                        try {
                            gissadeTalet = Integer.parseInt(payload.split("=")[1]);
                        }
                        catch (ArrayIndexOutOfBoundsException e){
                            gissadeTalet = 1000;
                        }

                        handlePostMethod(gissadeTalet);
                        System.out.println("handling post");
                        updateHTML();
                        //response = "HTTP/1.1 303 See Other\nLocation: /result \nContent-Length: 0 \nConnection: close\nContent-Type: text/html\n\n";
                        response = "HTTP/1.1 303 See Other\nLocation: /running \nContent-Length: 0 \nConnection: close\nContent-Type: text/html\n\n";
                        if (curSession.getCorrectGuess()){
                            response = "HTTP/1.1 303 See Other\nLocation: /endpage" + curSession.getNumGuesses() + "\nContent-Length: 0 \nConnection: close\nContent-Type: text/html\n\n";
                            cookieMap.remove(curSession.getCookie());
                        }

                    }
                    else { //GET
                        //cookie = curSession.getCookie();
                        if (!cookieCopChecker()){
                            in.close();
                        }
                        if (getHeader.contains("/endpage")){
                            String numGuesses = getHeader.split(" ")[1].substring(8,9);
                            String endBody = String.format(startBody, "Correct, the correct number was guessed in " + numGuesses + " guesses." + "<br>", "<a href=\"http://localhost:8989\"> New game</a>");
                            curHTML = startHead + endBody + "</body></html>";

                            response = "HTTP/1.1 200 OK\nSet-Cookie: ickeFungerandeCookie\nContent-Length: " + curHTML.length() + "\nConnection: close\nContent-Type: text/html\n\n";

                            response += curHTML;
                        }
                        else{
                            updateHTML();
                            response = "HTTP/1.1 200 OK\nSet-Cookie: "+curSession.getCookie()+"\nContent-Length: " + curHTML.length() + "\nConnection: close\nContent-Type: text/html\n\n";
                            response += curHTML;
                        }


                            //"HTTP/1.1 303 See Other\nContent-Length: 0 \nConnection: close\nContent-Type: text/html\n\n";
                            //response = "HTTP/1.1 200 OK\nSet-Cookie: token=deleted\nLocation: /endpage \nContent-Length: " + curHTML.length() + "\nConnection: close\nContent-Type: text/html\n\n";
                            //cookieMap.remove(curSession.getCookie());

                    }


                    //out.write("HTTP RESPONSE"); // write
                    //String response = "HTTP/1.1 200 OK\nSet-Cookie: "+curSession.getCookie()+"\nContent-Length: " + curHTML.length() + "\nConnection: close\nContent-Type: text/html\n\n";
                    System.out.println(response);
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

    private boolean cookieCopChecker() {
        String cookie = curSession.getCookie();
        if(cookieCop.get(cookie) == serverIP){
            return true;
        }
        return false;

    }

    public static String readPayload(BufferedReader scktIn, int contentLength)throws IOException{
        char[] cbuf=new char[contentLength];
        scktIn.read(cbuf, 0, contentLength);
        return new String(cbuf);
    }


    private String getClientIP() {
        return String.valueOf(new Random().nextInt(1000)+1000); //hittar på IP-address
    }

    private String createNewCookie() {
        Random rand = new Random();
        String cookie = "SESSION" + String.valueOf(rand.nextInt(1000));  //tänker en cookie som enbart är ett nummer
        curSession = new Session(cookie);
        System.out.println(cookie);
        return cookie;
    }

    private void handlePostMethod(int gissadeTalet) {

        if (gissadeTalet <= curSession.getLowerBound() || gissadeTalet >= curSession.getUpperBound()) {
            curSession.setOutOfBounds(true);
        }
        else {
            curSession.increaseGuesses();
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

    private void updateHTML() {

        String endBody;
        String body;
        if (curSession.getNumGuesses() == 0){
            createHomePageHTML();
        }
        else {
            if (curSession.getCorrectGuess()) {
                endBody = String.format(startBody, "Correct, the correct number was guessed in " + curSession.getNumGuesses() + " guesses." + "<br>", "<a href=\"https://localhost:8989\"> New game</a>");
                curHTML = startHead + endBody + "</body></html>";
            } else {
                if (curSession.getOutOfBounds()) {
                    body = String.format(startBody, "Only numbers between " + curSession.getLowerBound() + " and " + curSession.getUpperBound(), "");
                } else {
                    body = String.format(startBody, "Nope, guess a number between " + curSession.getLowerBound() + " and " + curSession.getUpperBound(), "You have made " + curSession.getNumGuesses() + " guesses!");
                }
                curHTML = startHead + body + startForm + "</body></html>";
            }
        }
        writeHTML();
    }

    private void createHomePageHTML() {
        String body = String.format(startBody, "Welcome to the Number Guess Game.", "Guess a number between 1 and 100");
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
