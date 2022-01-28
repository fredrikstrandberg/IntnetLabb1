

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class Server {

    private final String startHead = "<!DOCTYPE html> <html lang=\"En\"><head><meta charset=\"UTF-8\"><title>Number guess game</title></head>";
    private final String startBody = "<body>%s<br>%s" ;
    private final String startForm = "<form name=\"guessform\" method=\"POST\"> <input type=\"text\" name=\"gissadeTalet\"" +
        "autofocus=\"\"><input type=\"submit\" value=\"Guess\">";
    private String curHTML;
    private Session curSession;

    public static void main(String[] args) {
        new Server();
    }

    public Server() {

        HashMap<String, Session> cookieMap=new HashMap<String, Session>();   //nytt
        HashMap<String, String> cookieCop = new HashMap<String, String>();


        int port = 8989;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Listening on port: " + port);
            createHomePageHTML();

            while (true) {
                try (Socket socket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {


                    socket.setSoTimeout(2000);
                    String cookie = "";
                    String contentLength = "";
                    String line;
                    String response;
                    String getHeader = "";  //vill spara första raden i get header då den kan innehålla olika urls
                    boolean postVariable = false;  //initierar till false

                    while (!Objects.equals(line = in.readLine(), "") && line!=null) { // read

                        System.out.println(" <<< " + line); // log
                        if (line.startsWith("GET /favicon.ico")){  //buggig get-request som vi inte vill använda
                            out.close();
                            in.close();
                        }


                        else if (line.matches("GET\\s+.*")) {  //Om get request.
                            System.out.println("GET");
                            getHeader = line;

                        } else if (line.matches("POST\\s+.*")) {
                            System.out.println("POST");
                            postVariable = true;   //sätter post till true för att hantera den senare

                        } else if (line.matches("Cookie:\\s+.*")){   //
                            cookie = line.split(" ")[1];
                            System.out.println("Found cookie: " + cookie);

                        } else if (line.matches("Content-Length:\\s+.*")){
                            contentLength = line.split(" ")[1];

                    }
                    }
                    //create new user
                    if (!cookie.matches("SESSION.*")){  //om vi coookien inte finns, dvs ny användare

                        cookie = createNewCookie();  //skapar en ny cookie
                        curSession = new Session(cookie);  //skapar en ny session med skapad cookie
                        String ip = getClientIP();

                        cookieCop.put(cookie, ip);
                        cookieMap.put(cookie, curSession);  //kopplar cookie till session

                    }

                    curSession = cookieMap.get(cookie);  //curSession är den sessionen kopplad till en cookie
                    System.out.println(" >>> " + "HTTP RESPONSE"); // log
                    if (postVariable) {  //Hanterar post

                        String payload = readPayload(in, Integer.parseInt(contentLength));
                        int gissadeTalet;
                        try {
                            gissadeTalet = Integer.parseInt(payload.split("=")[1]);
                        }
                        catch (ArrayIndexOutOfBoundsException | NumberFormatException e){  //om ogiltig inmatning sätter vi gissadeTalet till 1000 för att få felet "out of bounds".
                            gissadeTalet = 1000;
                        }

                        handlePostMethod(gissadeTalet);  //uppdaterar sessionens paramterar beronde på gissade talet
                        updateHTML();  //uppdatarar nuvarande html för för nästa get-request

                        response = "HTTP/1.1 303 See Other\nLocation: /running \nContent-Length: 0 \nConnection: close\nContent-Type: text/html\n\n";

                        if (curSession.getCorrectGuess()){  //om vi har gissat rätt blir det annan response!
                            response = "HTTP/1.1 303 See Other\nLocation: /endpage" + curSession.getNumGuesses() + "\nContent-Length: 0 \nConnection: close\nContent-Type: text/html\n\n";
                            cookieMap.remove(curSession.getCookie()); //tar bort cookien från mappen
                        }
                    }
                    else { //hanterar en get-request

                        if (getHeader.contains("/endpage")){  //om vi klarat spelet eller uppdaterar sidan på slutsidan
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
                    }

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
            System.err.println("Could not listen on port: " + port);
            System.exit(1);
        }
    }

    public static String readPayload(BufferedReader scktIn, int contentLength)throws IOException{
        char[] cbuf=new char[contentLength];
        scktIn.read(cbuf, 0, contentLength);  //läser in payload
        return new String(cbuf);
    }

    private String getClientIP() {
        return String.valueOf(new Random().nextInt(1000)+1000); //hittar på IP-address
    }

    private String createNewCookie() {
        System.out.println("creating new cookie!!!");

        Random rand = new Random();
        String cookie = "SESSION" + String.valueOf(rand.nextInt(1000));  //ny cookie
        System.out.println(cookie);
        return cookie;
    }

    private void handlePostMethod(int gissadeTalet) {
        System.out.println("handling post");

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
        String end = "</form> </body> </html>";
        curHTML = startHead + body + startForm + end;
        writeHTML();
    }

    private void writeHTML() {  //skriver htlm till files guess.html
        FileWriter fWriter;
        BufferedWriter writer;
        try {
            fWriter = new FileWriter("GuessingGame/src/guess.html");
            writer = new BufferedWriter(fWriter);
            writer.write(curHTML);
            writer.newLine();
            writer.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
