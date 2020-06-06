import java.net.*;
import java.io.*;
/**
 * Class for sending messages
 *
 * @author Sam Cavenagh
 * @version 5/11/02
 * 
 * Website: http://home.pacific.net.au/~cavenagh/SH/
 * Email: cavenaghweb@hotmail.com
 */
class Message{
    
    Socket msgSocket;
    PrintWriter out;
    BufferedReader in;

    SHinterface sh;

    String playersName;

    boolean listen = true;

    Message(SHinterface sh)
    {
        this.sh = sh;
    }

    public void createConnection(String servername, String playersName)
    {
    this.playersName = playersName;
    try{
        msgSocket = new Socket(servername, 4444);
        out = new PrintWriter(msgSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(msgSocket.getInputStream()));
    }catch(UnknownHostException e) {
        //sh.addMsg("Server: " + servername + " Could not be Found");
    }catch(IOException e2){
        //sh.addMsg("Server not Listening for Connections");
    }

    if (msgSocket != null){
        out.println(playersName);
        String name = "unknown#$#";
        try{
        name = in.readLine();
        }catch (IOException e3) {
        sh.addMsg("Getting Otherplayers Name Error " + e3);
        }

        if(!name.equals("unknown#$#")){
            listen = true;
            new WaitforMsg();
        }
    }
    }

    public void sendMsg(String msg)
    {
    out.println(playersName + ": " + msg);
    }

    public void endConnection()
    {
    listen = false;

    if(msgSocket != null){
    out.println("end");
    try{
    msgSocket.close();
    }catch(IOException e){}
    }

    sh.addMsg("Connection Closed");
    }


class WaitforMsg implements Runnable{
    
    Thread wt; //Wait Thread

    WaitforMsg()
    {
        wt = new Thread(this, "Wait");
        wt.start(); // Starting thread
    }

    public void run()
    {
    do{
    String otherplayermsg = "Message Error";
    try{
        otherplayermsg = in.readLine();
    }catch(IOException e) {
        sh.addMsg("Read Error: " + e);
        sh.addMsg("Server Disconnection");
        listen = false;
    }
    if(otherplayermsg.equals("end"))
    endConnection();
    else
    if(listen)
    sh.addMsg(otherplayermsg);

    }while(listen);
    }
}

}