import java.io.*;
import java.net.*;

/**
 * Class for sending/relaying messages
 *
 * @author Sam Cavenagh
 * @version 6/11/02
 *     <p>Website: http://home.pacific.net.au/~cavenagh/SH/ Email: cavenaghweb@hotmail.com
 */
class ServerMessage {

  ServerSocket listenSocket;
  Socket msgSocket[] = new Socket[3];
  PrintWriter out[] = new PrintWriter[3];
  BufferedReader in[] = new BufferedReader[3];

  SHinterface sh;

  String playersName;

  boolean listen = true;

  int socketCount = 0;

  ServerMessage(SHinterface sh) {
    this.sh = sh;
  }

  public void createConnection(String playersName) {
    this.playersName = playersName;
    sh.addMsg("Listening for other players");
    new ListenThread();
  }

  public void sendMsg(String msg) {
    for (int n = 0; n < 3; n++) {
      if (out[n] != null) out[n].println(playersName + ": " + msg);
    }
  }

  public void endConnection() {
    listen = false;

    if (listenSocket != null)
      try {
        listenSocket.close();
      } catch (IOException e) {
        sh.addMsg("Error Closing Listen " + e);
      }

    for (int n = 0; n < 3; n++) {
      if (msgSocket[n] != null) {
        try {
          out[n].println("end");
          msgSocket[n].close();
        } catch (Exception e2) {
          sh.addMsg("Error Closing Msg " + e2);
        }
      }
    }

    socketCount = 0;
    sh.addMsg("Connections Closed");
  }

  class WaitforMsg implements Runnable {

    Thread wt; // Wait Thread
    int socketNumber; // With of the 3 sockets is this socket listening to ?
    boolean socketOK = true;
    String name; // Name of player at other end of socket.

    WaitforMsg(int socketNumber, String name) {
      this.socketNumber = socketNumber;
      this.name = name;

      wt = new Thread(this, "Wait");
      wt.start(); // Starting thread
    }

    public void run() {
      do {
        String otherplayermsg = "Message Error";
        try {
          otherplayermsg = in[socketNumber].readLine();
        } catch (IOException e) {
          sh.addMsg("Read Error: " + e);
          disconnect();
        }

        if (socketOK) {
          if (otherplayermsg.equals("end")) {
            disconnect();
          } else {
            // Displaying msg to server player
            sh.addMsg(otherplayermsg);

            // Sending message to other players
            for (int n = 0; n < 3; n++) {
              if (out[n] != null) if (socketNumber != n) out[n].println(otherplayermsg);
            }
          }
        }

      } while (listen && socketOK);
      sh.addMsg("Player " + name + " Has Disconnected");
      sendMsg("Player " + name + " Has Disconnected");
    }

    private void disconnect() {
      try {
        msgSocket[socketNumber].close();
      } catch (IOException e) {
        sh.addMsg("Error Closing Listen " + e);
      }
      msgSocket[socketNumber] = null;
      in[socketNumber] = null;
      out[socketNumber] = null;
      socketOK = false;
    }
  }

  class ListenThread implements Runnable {

    Thread lt; // Listen Thread

    ListenThread() {
      lt = new Thread(this, "Listen");
      lt.start(); // Starting thread
    }

    public void run() {
      // Opening listening Socket
      listenSocket = null;
      try {
        listenSocket = new ServerSocket(4444);
      } catch (IOException e) {
        // sh.addMsg("Could not listen " + e);
      }

      boolean endlook = false;
      // Waiting for connection
      do {
        msgSocket[socketCount] = null;
        try {
          msgSocket[socketCount] = listenSocket.accept();
        } catch (IOException e2) {
          sh.addMsg("Error Accept " + e2);
          endlook = true;
        }

        if (!endlook) {
          try {
            out[socketCount] = new PrintWriter(msgSocket[socketCount].getOutputStream(), true);
            in[socketCount] =
                new BufferedReader(new InputStreamReader(msgSocket[socketCount].getInputStream()));
          } catch (IOException e) {
            sh.addMsg("Error Out / In problem." + e);
          }
          String name = "unknown";
          try {
            name = in[socketCount].readLine();
          } catch (IOException e3) {
            sh.addMsg("Getting Otherplayers Name Error " + e3);
          }
          out[socketCount].println(playersName);
          sh.addMsg("Connection established with " + name);
          new WaitforMsg(socketCount, name);
          socketCount++;
        }
      } while (listen && socketCount < 3 && !endlook);

      if (listenSocket != null)
        try {
          listenSocket.close();
        } catch (IOException e) {
          sh.addMsg("Error Closing Listen " + e);
        }
    }
  }
}
