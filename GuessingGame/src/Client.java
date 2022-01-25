import java.net.*;
import java.io.*;

public class Client{
    public static void main(String[] args){
        try{
            //	    Socket sckt = new Socket("leguin.csc.kth.se",1234);
            Socket sckt = new Socket(args[0],8989);
            PrintStream out = new PrintStream(sckt.getOutputStream());
            BufferedReader indata =
                    new BufferedReader(new InputStreamReader(System.in));
            String text;
            while( (text = indata.readLine()) != null){
                out.println(text);
            }
            sckt.shutdownOutput();
        }
        catch (Exception e){ System.err.println("Ett fel intraffade!"); }
    }
}
