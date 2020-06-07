import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;

/**
 * Player Class
 *
 * @author Sam Cavenagh
 * @version 13/11/02
 *     <p>Website: http://home.pacific.net.au/~cavenagh/SH/ Email: cavenaghweb@hotmail.com
 */
class Player extends PlayerBase {
  // has the player done swap
  boolean swapdone = false;

  Socket msgSocket;
  PrintWriter out;
  BufferedReader in;

  Hand hand;
 
  Card faceup[][] = new Card[3][3];
  int carddowncount[] = new int[3];
  int cardcount[] = new int[3];

  int deck; // count of card remaining in deck;

  // for whos turn indicator
  String servername;

  int whosturn = 0;

  Player(SHinterface sh, Graphics g, Hand hand, Score score) {
    super(sh, g, score);
    this.hand = hand;
  }

  public Hand ownHand() {
    return hand;
  }

  protected int deckLength() {
    return deck;
  }

  public void createConnection(String servername, String playersName) {
    this.playersName = playersName;
    score.addName(playersName);
    sh.addMsg("Looking for " + servername + " Dealer");
    try {
      msgSocket = new Socket(servername, 4445);
      out = new PrintWriter(msgSocket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(msgSocket.getInputStream()));
    } catch (UnknownHostException e) {
      sh.addMsg("Server: " + servername + " Could not be Found");
      sh.closeConnection();
    } catch (IOException e2) {
      sh.addMsg("Server not Listening for Connections");
    }

    if (msgSocket != null) {
      out.println(playersName);
      String name = "unknown#$#";
      String fastgameS = "false";
      String sevenS = "false";
      String nineS = "false";
      String swapS = "false";
      try {
        name = in.readLine();
        fastgameS = in.readLine();
        sevenS = in.readLine();
        nineS = in.readLine();
        swapS = in.readLine();
      } catch (IOException e3) {
        sh.addMsg("Getting Otherplayers Name Error " + e3);
      }

      if (!name.equals("unknown#$#")) {
        sh.addMsg("Game Connection Established with " + name);
        this.servername = name;
        // score.addName(servername);//added later ??
        if (fastgameS.equals("true")) {
          fastgame = true;
          sh.addMsg("Fast Game Selected");
        }

        if (sevenS.equals("true")) {
          seven = true;
          sh.addMsg("Must Play Under Seven Selected");
        }

        if (nineS.equals("true")) {
          nine = true;
          sh.addMsg("Nine is Invisible Selected");
        }

        if (swapS.equals("true")) {
          swap = true;
          sh.addMsg("Card Swap at start of game Selected");
        }
        listen = true;
        new WaitforMsg();
      }
    }
  }

  public void sendCommand(String command) {
    out.println(command);
  }

  public void endConnection() {
    listen = false;

    if (msgSocket != null) {
      out.println("end:");
      try {
        msgSocket.close();
      } catch (IOException e) {
      }
    }

    sh.addMsg("Game Connection Closed");
  }

  private void nextTurn() {
    whosturn++;
    if (whosturn >= 4) whosturn = 0;
    while (outofgame[whosturn]) {
      whosturn++;
      if (whosturn >= 4) whosturn = 0;
    }
  }

  protected int handLength(int playerNo) {
    return cardcount[playerNo];
  }

  public void displayTable() {
    ImageManager im = sh.getImageManager();
    BufferedImage back = im.getCardBack();
    BufferedImage backSW = im.getCardBackSideways();

    g.setColor(Color.black);
    fillRect(0, 0, 450, 550);
    g.setColor(Color.white);
    drawLine(0, 450, 450, 450);
    g.setColor(Color.red);
    drawRoundRect(355, 5, 90, 40, 15, 15);
    g.setColor(Color.white);
    drawString("Deck: " + deckLength(), 365, 20);
    drawString("Pile: " + pilelength(), 365, 40);
    ownHand().showHand();

    drawRoundRect(5, 360, 90, 40, 15, 15);
    drawString("Name: " + otherNames[0], 10, 375);
    drawString("Cards: " + handLength(0), 10, 395);
    drawImage(im.getPointer(1), 68, 380);

    // -- above this point is identical in both

    if (faceup[0][0] != null) faceup[0][0].drawSideWays(tableposition[0][0]);
    else if (carddowncount[0] >= 3)
      g.drawImage(backSW, (int) tableposition[0][0].getX(), (int) tableposition[0][0].getY(), sh);
    if (faceup[0][1] != null) faceup[0][1].drawSideWays(tableposition[0][1]);
    else if (carddowncount[0] >= 2)
      g.drawImage(backSW, (int) tableposition[0][1].getX(), (int) tableposition[0][1].getY(), sh);
    if (faceup[0][2] != null) faceup[0][2].drawSideWays(tableposition[0][2]);
    else if (carddowncount[0] >= 1)
      g.drawImage(backSW, (int) tableposition[0][2].getX(), (int) tableposition[0][2].getY(), sh);

    drawRoundRect(5, 5, 90, 40, 15, 15);
    drawString("Name: " + otherNames[1], 10, 20);
    drawString("Cards: " + handLength(1), 10, 40);
    drawImage(im.getPointer(2), 70, 25);

    if (faceup[1][0] != null) faceup[1][0].drawCard(tableposition[1][0]);
    else if (carddowncount[1] >= 3)
      g.drawImage(back, (int) tableposition[1][0].getX(), (int) tableposition[1][0].getY(), sh);
    if (faceup[1][1] != null) faceup[1][1].drawCard(tableposition[1][1]);
    else if (carddowncount[1] >= 2)
      g.drawImage(back, (int) tableposition[1][1].getX(), (int) tableposition[1][1].getY(), sh);
    if (faceup[1][2] != null) faceup[1][2].drawCard(tableposition[1][2]);
    else if (carddowncount[1] >= 1)
      g.drawImage(back, (int) tableposition[1][2].getX(), (int) tableposition[1][2].getY(), sh);

    drawRoundRect(355, 360, 90, 40, 15, 15);
    drawString("Name: " + otherNames[2], 360, 375);
    drawString("Cards: " + handLength(2), 360, 395);
    drawImage(im.getPointer(1), 423, 380);

    if (faceup[2][0] != null) faceup[2][0].drawSideWays(tableposition[2][0]);
    else if (carddowncount[2] >= 3)
      g.drawImage(backSW, (int) tableposition[2][0].getX(), (int) tableposition[2][0].getY(), sh);
    if (faceup[2][1] != null) faceup[2][1].drawSideWays(tableposition[2][1]);
    else if (carddowncount[2] >= 2)
      g.drawImage(backSW, (int) tableposition[2][1].getX(), (int) tableposition[2][1].getY(), sh);
    if (faceup[2][2] != null) faceup[2][2].drawSideWays(tableposition[2][2]);
    else if (carddowncount[2] >= 1)
      g.drawImage(backSW, (int) tableposition[2][2].getX(), (int) tableposition[2][2].getY(), sh);

    // drawing pile
    if (pile[0] != null) {
      // determining how many cards of the same value are ontop of each other
      int top = 0;
      if (nine == true && pile[0].getValue() == 9) {
        top = 1;
        if (pile[1] != null)
          if (pile[1].getValue() == 9) {
            top = 2;
            if (pile[2] != null) if (pile[2].getValue() == 9) top = 3;
          }
      }
      int samecount = 1;
      for (int n = top + 1; n < top + 4; n++) {
        if (pile[n] == null) break;
        if (pile[n].getValue() == pile[top].getValue()) samecount++;
        else break;
      }
      if (samecount == 1) { // one of a kind
        if (pile[top] != null) pile[top].drawCard(centre1);
      } else if (samecount == 2) { // 2 of a kind
        pile[top + 1].drawCard((int) centre1.getX(), (int) centre1.getY() - 10);
        pile[top].drawCard((int) centre1.getX(), (int) centre1.getY() + 10);
      } else if (samecount >= 3) { // 3 of a kind
        pile[top + 2].drawCard((int) centre1.getX(), (int) centre1.getY() - 20);
        pile[top + 1].drawCard((int) centre1.getX(), (int) centre1.getY());
        pile[top].drawCard((int) centre1.getX(), (int) centre1.getY() + 20);
      }
      if (nine == true && pile[0].getValue() == 9)
        if (top == 1) // one nine
        pile[0].drawSideways((int) centre1.getX() - 15, (int) centre1.getY() + 40);
      if (top == 2) { // 2 nines
        pile[1].drawSideways((int) centre1.getX() - 15, (int) centre1.getY() + 40);
        pile[0].drawSideways((int) centre1.getX() - 15, (int) centre1.getY() + 50);
      }
      if (top == 3) { // 3 nines
        pile[2].drawSideways((int) centre1.getX() - 15, (int) centre1.getY() + 40);
        pile[1].drawSideways((int) centre1.getX() - 15, (int) centre1.getY() + 50);
        pile[0].drawSideways((int) centre1.getX() - 15, (int) centre1.getY() + 60);
      }
    } else if (burnt) {
      BufferedImage burnBang = im.getBurnt();
      // Draw in centre of play area. Coords are already scaled
      Point xy = sh.getCoordsForCentredImage(burnBang);
      g.drawImage(burnBang, xy.x, xy.y, sh);
      burnt = false;
    }
    g.drawImage(
        im.getPointer(whosturn),
        (int) pointerpoints[whosturn].getX(),
        (int) pointerpoints[whosturn].getY(),
        sh);
    sh.repaint();
  }

  public void cardSelection(int cardno) {
    Card card;
    String command;
    if (hand.getCard(0) == null) { // if player only has cards on table
      if (hand.isFaceUp()) { // if cards still faceup on table
        card = hand.getFaceUp(cardno);
        command = "turn:faceup:";
        if (isValidCard(card, command))
          for (int i = 0; i < 3; i++)
            if (hand.getFaceUp(i) != null)
              if (hand.getFaceUp(i).getNumber() == card.getNumber()) {
                hand.removeFaceUp(i);
                break;
              }
      } else { // if only cards down
        card = hand.getFaceDown(cardno);
        command = "turn:facedown:";
        if (isValidCard(card, command)) hand.removeFaceDown(cardno);
        else { // player must pick up the pile if not valide
          hand.addCard(hand.getFaceDown(cardno));
          sendCommand("turn:facedown:pickup:" + hand.getFaceDown(cardno).getNumber() + ":");
          sh.addMsg(
              "The card you played was a "
                  + hand.getFaceDown(cardno).getStringValue()
                  + " you had to pick up the pile. BLAOW");
          for (int n = 0; n < 52; n++) {
            if (pile[n] == null) break;
            hand.addCard(pile[n]);
            pile[n] = null;
          }
          hand.removeFaceDown(cardno);
          sh.setmyTurn(false);
          nextTurn();
        }
      }
    } else { // if player still has cards in hand
      card = hand.getCard(cardno);
      command = "turn:";
      if (isValidCard(card, command))
        for (int n = 0; n < hand.length() - 1; n++)
          if (card.getNumber() == hand.getCard(n).getNumber()) {
            hand.removeCard(n);
            break;
          }
    }
    if (deckLength() <= 0 || hand.length() > 3) displayTable();
  }

  private boolean isValidCard(Card card, String command) {
    int top = 0;
    boolean multi = false;
    if (hand.getCard(0) != null) multi = checkformulti(card);
    else if (hand.isFaceUp()) multi = checkformultiFaceUp(card);

    if (multi) return true;

    if (card.getValue() == 2) { // 2 can be played at anytime
      cardAccepted(card, command);
      return true;
    } else if (card.getValue() == 10
        || fourOfAKind(card) == true) { // pile is burn and its players turn again
      burnt = true;
      for (int n = 0; n < 51; n++) pile[n] = null;
      sendCommand(command + card.getNumber() + ":");
      sh.addMsg("You have burnt the pile, its your turn again");
      return true;
    } else if (pile[0] == null) {
      cardAccepted(card, command);
      return true;
    } else if (nine == true && card.getValue() == 9) {
      cardAccepted(card, command);
      return true;
    }
    if (nine == true && pile[0].getValue() == 9) {
      for (int i = 0; i < 52; i++) {
        if (pile[i] == null) {
          cardAccepted(card, command);
          return true;
        }
        if (pile[i].getValue() == 9) top++;
        else break;
      }
    }
    if (seven == true && pile[top].getValue() == 7) {
      if (card.getValue() >= 7) {
        sh.addMsg("You Must Play Less Than a Seven");
        sh.soundManager.playTwang();
        return false;
      } else {
        cardAccepted(card, command);
        return true;
      }
    }
    if (pile[top].getValue() <= card.getValue()) {
      cardAccepted(card, command);
      return true;
    }
    if (hand.isFaceUp() == true || hand.length() > 1)
      sh.addMsg("You can't play a " + card.getStringValue() + " please select another card");
    return false;
  }

  private boolean checkformulti(Card card) {
    // checking if card selection is valid
    if (pile[0] != null) {
      if (nine == true && pile[0].getValue() == 9) {
        int count = 0; // determining the number of nines on top of pile
        for (int i = 0; i < 52; i++) {
          if (pile[i] == null) break;
          if (pile[i].getValue() == 9) count++;
          else break;
        }
        if (pile[count] != null) {
          if (card.getValue() == 9) {
            // do nothing as valid card
          } else if (seven == true && pile[count].getValue() == 7) {
            if (card.getValue() >= 7) return false;
          } else if (!(card.getValue() == 2
              || card.getValue() == 10
              || card.getValue() >= pile[count].getValue())) return false;
        }
      } else if (card.getValue() == 9 && nine == true) {
        // do nothing as valid card
      } else if (seven == true && pile[0].getValue() == 7) {
        if (card.getValue() >= 7) return false;
      } else if (!(card.getValue() == 2
          || card.getValue() == 10
          || card.getValue() >= pile[0].getValue())) return false;
    }

    // checking how many card of the same value as card played are in players hand
    int amountinhand = 0;
    for (int n = 0; n < hand.length(); n++) {
      if (hand.getCard(n) == null) break;
      if (hand.getCard(n).getValue() == card.getValue()) amountinhand++;
    }
    if (amountinhand <= 1) return false;
    MultiCardD dialog = new MultiCardD(sh, amountinhand);
    int numbertoplay = dialog.getChoice();
    if (numbertoplay <= 1) return false;
    String command = "turn:multi:" + numbertoplay + ":" + card.getNumber() + ":";
    addcardtopile(card);
    numbertoplay--;
    int toberemovedcount = 0;
    int toberemoved[] = new int[3];
    for (int n = 0; n < 3; n++) toberemoved[n] = -1;
    for (int n = 0; n < hand.length() - 1; n++) {
      if (hand.getCard(n) == null) break;
      if (numbertoplay <= 0) break;
      if (card.getValue() == hand.getCard(n).getValue()
          && card.getNumber() != hand.getCard(n).getNumber()) {
        command = command.concat(hand.getCard(n).getNumber() + ":");
        addcardtopile(hand.getCard(n));
        // storing which card are to be removed
        toberemoved[toberemovedcount] = hand.getCard(n).getNumber();
        toberemovedcount++;
        numbertoplay--;
      }
    }
    // removing card from hand
    for (int n = 0; n < 3; n++) {
      if (toberemoved[n] == -1) break;
      for (int i = 0; i < hand.length() - 1; i++)
        if (hand.getCard(i).getNumber() == toberemoved[n]) {
          hand.removeCard(i);
          break;
        }
    }
    // sending command
    sendCommand(command);
    // checking for 4 of a kind
    if (fourOfAKind(pile[3]) || pile[0].getValue() == 10) {
      burnt = true;
      for (int n = 0; n < 51; n++) pile[n] = null;
      sh.addMsg("You burn the pile is your turn again");
    } else {
      sh.setmyTurn(false);
      nextTurn();
    }
    return true;
  }

  private boolean checkformultiFaceUp(Card card) {
    // checking if card selection is valid
    if (pile[0] != null) {
      if (card.getValue() == 9 && nine == true) {
        // do nothing as valid card
      } else if (card.getValue() == 10 || card.getValue() == 2) {
        // do nothing as valid card
      } else if (nine == true && pile[0].getValue() == 9) {
        int count = 0; // determining the number of nines on top of pile
        for (int i = 0; i < 52; i++) {
          if (pile[i] == null) break;
          if (pile[i].getValue() == 9) count++;
          else break;
        }
        if (pile[count] != null) {
          if (seven == true && pile[count].getValue() == 7) {
            if (card.getValue() >= 7) return false;
          } else if (!(card.getValue() == 2
              || card.getValue() == 10
              || card.getValue() >= pile[count].getValue())) return false;
        }
      } else if (seven == true && pile[0].getValue() == 7) {
        if (card.getValue() >= 7) return false;
      } else if (!(card.getValue() == 2
          || card.getValue() == 10
          || card.getValue() >= pile[0].getValue())) return false;
    }

    // checking how many card of the same value as card played are in players hand
    int amountinhand = 0;
    for (int n = 0; n < 3; n++) {
      if (hand.getFaceUp(n) != null)
        if (hand.getFaceUp(n).getValue() == card.getValue()) amountinhand++;
    }
    if (amountinhand <= 1) return false;
    MultiCardD dialog = new MultiCardD(sh, amountinhand);
    int numbertoplay = dialog.getChoice();
    if (numbertoplay <= 1) return false;
    String command = "turn:faceup:multi:" + numbertoplay + ":" + card.getNumber() + ":";
    addcardtopile(card);
    numbertoplay--;
    int toberemovedcount = 0;
    int toberemoved[] = new int[3];
    for (int n = 0; n < 3; n++) toberemoved[n] = -1;
    for (int n = 0; n < 3; n++)
      if (hand.getFaceUp(n) != null) {
        if (numbertoplay <= 0) break;
        if (card.getValue() == hand.getFaceUp(n).getValue()
            && card.getNumber() != hand.getFaceUp(n).getNumber()) {
          command = command.concat(hand.getFaceUp(n).getNumber() + ":");
          addcardtopile(hand.getFaceUp(n));
          // storing which card are to be removed
          toberemoved[toberemovedcount] = hand.getFaceUp(n).getNumber();
          toberemovedcount++;
          numbertoplay--;
        }
      }
    // removing card from hand
    for (int n = 0; n < 3; n++) {
      if (toberemoved[n] == -1) break;
      for (int i = 0; i < 3; i++)
        if (hand.getFaceUp(i) != null)
          if (hand.getFaceUp(i).getNumber() == toberemoved[n]) {
            hand.removeFaceUp(i);
            break;
          }
    }
    // sending command
    sendCommand(command);
    // checking for 4 of a kind
    if (fourOfAKind(pile[3]) || pile[0].getValue() == 10) {
      burnt = true;
      for (int n = 0; n < 51; n++) pile[n] = null;
      sh.addMsg("You burn the pile is your turn again");
    } else {
      sh.setmyTurn(false);
      nextTurn();
    }
    return true;
  }

  private void cardAccepted(Card card, String command) {
    sendCommand(command + card.getNumber() + ":");
    // adding card to pile
    for (int n = 51; n > 0; n--) pile[n] = pile[n - 1];
    pile[0] = card;
    sh.setmyTurn(false);
    nextTurn();
  }

  class WaitforMsg implements Runnable {

    Thread wt; // Wait Thread

    WaitforMsg() {
      wt = new Thread(this, "Wait");
      wt.start(); // Starting thread
    }

    public void run() {
      do {
        String otherplayermsg = "Message Error";
        try {
          otherplayermsg = in.readLine();
        } catch (IOException e) {
          sh.addMsg("Read Error: " + e);
          sh.addMsg("Server Disconnection");
          listen = false;
        }

        if (otherplayermsg == null) endConnection();
        else if (otherplayermsg.equals("end:")) endConnection();
        else if (listen) {
          // sh.addMsg("Msg: " + otherplayermsg);//------------------------------------msg from
          // server

          int commandlength = 0;
          // decode message and perform function required
          for (int n = 0; n < otherplayermsg.length(); n++) {
            char extract = otherplayermsg.charAt(n);
            if (extract == (':')) {
              commandlength = n;
              break;
            }
          }
          String command = otherplayermsg.substring(0, commandlength);

          if (command.equals("deal")) deal(otherplayermsg, commandlength);

          if (command.equals("otherdetails")) otherdetails(otherplayermsg, commandlength);

          if (command.equals("yourturn")) yourturn();

          if (command.equals("getcard")) getcard(otherplayermsg, commandlength);

          if (command.equals("othersturn")) othersturn(otherplayermsg, commandlength);

          if (command.equals("msg"))
            sh.addMsg(otherplayermsg.substring(commandlength, otherplayermsg.length()));

          if (command.equals("out")) out(otherplayermsg, commandlength);

          if (command.equals("otherout")) otherout(otherplayermsg, commandlength);

          if (command.equals("lost")) lost(otherplayermsg, commandlength);

          if (command.equals("reset")) reset();
        }
      } while (listen);
    }

    private void reset() {
      swapdone = false;
      for (int n = 0; n < 4; n++) {
        // setting scoreboard if game finishes early
        if (n < 3) {
          if (outofgame[n] == false) score.addScore(otherNames[n], position);
        } else {
          if (outofgame[n] == false) score.addScore(playersName, position);
        }
        outofgame[n] = false;
      }
      position = 1;
      for (int n = 0; n < 3; n++) if (otherNames[n].equals(servername)) whosturn = n;
      hand.removeAll();
      for (int n = 0; n < 3; n++) {
        for (int i = 0; i < 3; i++) faceup[n][i] = null;
        carddowncount[n] = 3;
        cardcount[n] = 3;
        deck = 16;
      }
      for (int n = 0; n < 52; n++) pile[n] = null;
    }

    // msg telling player they are out of the game and what posititon they came
    private void out(String otherplayermsg, int commandlength) {

      // decode variable that came with message
      int varlength = 0;
      for (int n = commandlength + 1; n < otherplayermsg.length(); n++) {
        char extract = otherplayermsg.charAt(n);
        if (extract == (':')) {
          varlength = n;
          break;
        }
      }
      String positionString = otherplayermsg.substring(commandlength + 1, varlength);
      int playerPosition = 0;
      try {
        playerPosition = Integer.parseInt(positionString);
      } catch (NumberFormatException b) {
        sh.addMsg("Otherplayer - out -  variable to Int error: " + b);
      }

      boolean gameover = false;
      if (playerPosition == 0) sh.addMsg("Position Error");
      if (playerPosition == 1) sh.addMsg("Well done you have won the game your the first out !!");
      else if (playerPosition == 2) sh.addMsg("You've done alright you the second out of the game");
      else if (playerPosition == 3) sh.addMsg("Just made it, congrats your not a ShitHead !");
      else if (playerPosition == 4) {
        sh.addMsg("You Lost ShitHead !!!");
        gameover = true;
      }

      outofgame[3] = true;

      score.addScore(playersName, position);

      if (playerPosition == 4) score.display();
      else position++;

      if (whosturn == 3 && !gameover) {
        nextTurn();
        displayTable();
      }
    }

    // msg stating another player is out of the game
    private void otherout(String otherplayermsg, int commandlength) {

      // decode variable that came with message
      int varlength = 0;
      for (int n = commandlength + 1; n < otherplayermsg.length(); n++) {
        char extract = otherplayermsg.charAt(n);
        if (extract == (':')) {
          varlength = n;
          break;
        }
      }
      String name = otherplayermsg.substring(commandlength + 1, varlength);

      sh.addMsg(name + " is out of the game");

      score.addScore(name, position);
      position++;

      for (int n = 0; n < 3; n++)
        if (otherNames[n].equals(name)) {
          outofgame[n] = true;
          if (whosturn == n) {
            nextTurn();
            displayTable();
          }
        }
    }

    // telling player who lost.
    private void lost(String otherplayermsg, int commandlength) {

      // decode variable that came with message
      int varlength = 0;
      for (int n = commandlength + 1; n < otherplayermsg.length(); n++) {
        char extract = otherplayermsg.charAt(n);
        if (extract == (':')) {
          varlength = n;
          break;
        }
      }
      String name = otherplayermsg.substring(commandlength + 1, varlength);

      sh.addMsg(name + " is the ShitHead");
      sh.addMsg("Game Over");

      score.addScore(name, 4);

      for (int n = 0; n < 3; n++) if (otherNames[n].equals(name)) outofgame[n] = true;

      score.display();
    }

    /*--------------------------------
     *   Othersturn msg format
     *   command: - 'otherplayer'
     *   name: players name
     *   cardno:(move) - 'pickup'
     *                 - 'burn'
     *                 - 'faceup'   - cardno2:  - a card number
     *                                          - 'multi' - numberplayed - up to 3 card numbers
     *                 - 'facedown' - cardno2:  - 'pickup'
     *                                          - a card number
     *                 -  'multi' - numberplayed - up to 4 card numbers
     *                 - a card number
     *-------------------------------*/
    private void othersturn(String otherplayermsg, int commandlength) {
      boolean burn = false;
      // decode variable that came with message
      int varlength = 0;
      for (int n = commandlength + 1; n < otherplayermsg.length(); n++) {
        char extract = otherplayermsg.charAt(n);
        if (extract == (':')) {
          varlength = n;
          break;
        }
      }
      String name = otherplayermsg.substring(commandlength + 1, varlength);
      int varlength2 = 0;
      for (int n = varlength + 1; n < otherplayermsg.length(); n++) {
        char extract = otherplayermsg.charAt(n);
        if (extract == (':')) {
          varlength2 = n;
          break;
        }
      }
      String cardno = otherplayermsg.substring(varlength + 1, varlength2);

      // determining which player just had a turn
      int playernumber = 0;
      for (int n = 0; n < 3; n++)
        if (name.equals(otherNames[n])) {
          playernumber = n;
          break;
        }

      if (cardno.equals("pickup")) { // other players picks up pile
        cardcount[playernumber] = cardcount[playernumber] + pilelength();
        for (int n = 0; n < 52; n++) pile[n] = null;
        sh.addMsg(otherNames[playernumber] + " picked up the pile");
      } else if (cardno.equals("burn")) { // other player burns the pile
        burnt = true;
        burn = true;
        // removing cards from pile
        sh.addMsg(name + " burnt the pile.");
        for (int n = 0; n < 51; n++) pile[n] = null;
        if (deck == 0 || cardcount[playernumber] > 3) {
          if (cardcount[playernumber] > 0) cardcount[playernumber]--;
        } else deck--;
      } else if (cardno.equals("faceup")) { // otherplayer plays a faceup card
        int varlength3 = 0;
        for (int n = varlength2 + 1; n < otherplayermsg.length(); n++) {
          char extract = otherplayermsg.charAt(n);
          if (extract == (':')) {
            varlength3 = n;
            break;
          }
        }
        String cardno2 = otherplayermsg.substring(varlength2 + 1, varlength3);
        if (cardno2.equals("multi")) {
          burn = faceupmulti(otherplayermsg, varlength3, playernumber);
        } else {
          try {
            Card card = new Card(Integer.parseInt(cardno2), sh, g);
            // adding card to pile
            for (int n = 51; n > 0; n--) pile[n] = pile[n - 1];
            pile[0] = card;
            // burning pile if a 10 is played
            if (pile[0].getValue() == 10 || fourOfAKind(pile[3]) == true) {
              burnt = true;
              burn = true;
              // removing cards from pile
              sh.addMsg(name + " burnt the pile.");
              for (int n = 0; n < 51; n++) pile[n] = null;
            }
            // removing card from table
            for (int n = 0; n < 3; n++)
              if (faceup[playernumber][n] != null)
                if (faceup[playernumber][n].getNumber() == card.getNumber()) {
                  faceup[playernumber][n] = null;
                  break;
                }
          } catch (NumberFormatException b) {
            sh.addMsg("Otherplayer - variable to Int error: " + b);
          }
        }
      } else if (cardno.equals("facedown")) { // if player plays one of there face down cards
        int varlength3 = 0;
        for (int n = varlength2 + 1; n < otherplayermsg.length(); n++) {
          char extract = otherplayermsg.charAt(n);
          if (extract == (':')) {
            varlength3 = n;
            break;
          }
        }
        String cardno2 = otherplayermsg.substring(varlength2 + 1, varlength3);
        if (cardno2.equals("pickup")) {
          for (int n = varlength3 + 1; n < otherplayermsg.length(); n++) {
            char extract = otherplayermsg.charAt(n);
            if (extract == (':')) {
              varlength2 = n;
              break;
            }
          }
          String cardplayed = otherplayermsg.substring(varlength3 + 1, varlength2);
          int numPlayed = 0;
          try {
            numPlayed = Integer.parseInt(cardplayed);
          } catch (NumberFormatException b) {
            sh.addMsg("processTurn - facedown pickup - variable to Int error: " + b);
          }
          cardcount[playernumber] = cardcount[playernumber] + pilelength() + 1;
          for (int n = 0; n < 52; n++) pile[n] = null;
          sh.addMsg(
              otherNames[playernumber]
                  + " played a "
                  + Card.getCardStringValue(numPlayed)
                  + " and had to picked up the pile");
        } else {
          try {
            Card card = new Card(Integer.parseInt(cardno2), sh, g);
            for (int n = 51; n > 0; n--) pile[n] = pile[n - 1];
            pile[0] = card;
            // burning pile if a 10 is played
            if (pile[0].getValue() == 10 || fourOfAKind(pile[3]) == true) {
              burnt = true;
              burn = true;
              // removing cards from pile
              sh.addMsg(name + " burnt the pile.");
              for (int n = 0; n < 51; n++) pile[n] = null;
            }
          } catch (NumberFormatException b) {
            sh.addMsg("Otherplayer - variable to Int error: " + b);
          }
        }
        carddowncount[playernumber]--;
      } else if (cardno.equals("multi")) { // if more than 1 card is played at a time
        // determining how many card where played
        int varlength3 = 0;
        for (int n = varlength2 + 1; n < otherplayermsg.length(); n++) {
          char extract = otherplayermsg.charAt(n);
          if (extract == (':')) {
            varlength3 = n;
            break;
          }
        }
        String numPlayedString = otherplayermsg.substring(varlength2 + 1, varlength3);
        // converting string to int for processing
        int numPlayed = 0;
        try {
          numPlayed = Integer.parseInt(numPlayedString);
        } catch (NumberFormatException b) {
          sh.addMsg("processTurn - multi - variable to Int error: " + b);
        }
        for (int n = 0; n < numPlayed; n++) {
          varlength2 = varlength3;
          // determining how many card where played
          varlength3 = 0;
          for (int i = varlength2 + 1; i < otherplayermsg.length(); i++) {
            char extract = otherplayermsg.charAt(i);
            if (extract == (':')) {
              varlength3 = i;
              break;
            }
          }
          String cardnoString = otherplayermsg.substring(varlength2 + 1, varlength3);
          // converting string to int for processing
          try {
            Card card = new Card(Integer.parseInt(cardnoString), sh, g);
            addcardtopile(card);
          } catch (NumberFormatException b) {
            sh.addMsg("processTurn - multi - variable to Int error: " + b);
          }

          if (deck == 0 || cardcount[playernumber] > 3) {
            if (cardcount[playernumber] > 0) cardcount[playernumber]--;
          } else deck--;
        }

        // burning pile if a 10 is played or 4 of a kind
        if (pile[0].getValue() == 10 || fourOfAKind(pile[3]) == true) {
          burnt = true;
          burn = true;
          // removing cards from pile
          sh.addMsg(name + " burnt the pile.");
          for (int i = 0; i < 51; i++) pile[i] = null;
        }

      } else {
        // adding card to pile
        try {
          Card card = new Card(Integer.parseInt(cardno), sh, g);
          for (int n = 51; n > 0; n--) pile[n] = pile[n - 1];
          pile[0] = card;
          // burning pile if a 10 is played
          if (pile[0].getValue() == 10 || fourOfAKind(pile[3]) == true) {
            burn = true;
            burnt = true;
            // removing cards from pile
            sh.addMsg(name + " burnt the pile.");
            for (int n = 0; n < 51; n++) pile[n] = null;
          }
        } catch (NumberFormatException b) {
          sh.addMsg("Otherplayer else - variable to Int error: " + b);
        }
        if (deck == 0 || cardcount[playernumber] > 3) {
          if (cardcount[playernumber] > 0) cardcount[playernumber]--;
        } else deck--;
      }
      if (!burn) nextTurn();
      displayTable();
    }

    private boolean faceupmulti(String otherplayermsg, int varlength2, int playernumber) {
      boolean burn = false;
      // determining how many card where played
      int varlength3 = 0;
      for (int n = varlength2 + 1; n < otherplayermsg.length(); n++) {
        char extract = otherplayermsg.charAt(n);
        if (extract == (':')) {
          varlength3 = n;
          break;
        }
      }
      String numPlayedString = otherplayermsg.substring(varlength2 + 1, varlength3);
      // converting string to int for processing
      int numPlayed = 0;
      try {
        numPlayed = Integer.parseInt(numPlayedString);
      } catch (NumberFormatException b) {
        sh.addMsg("processTurn - multi face up - variable to Int error: " + b);
      }
      for (int n = 0; n < numPlayed; n++) {
        varlength2 = varlength3;
        // determining how many card where played
        varlength3 = 0;
        for (int i = varlength2 + 1; i < otherplayermsg.length(); i++) {
          char extract = otherplayermsg.charAt(i);
          if (extract == (':')) {
            varlength3 = i;
            break;
          }
        }
        String cardnoString = otherplayermsg.substring(varlength2 + 1, varlength3);
        // converting string to int for processing
        try {
          Card card = new Card(Integer.parseInt(cardnoString), sh, g);
          addcardtopile(card);

          for (int i = 0; i < 3; i++)
            if (faceup[playernumber][i] != null)
              if (faceup[playernumber][i].getNumber() == card.getNumber()) {
                faceup[playernumber][i] = null;
                break;
              }
        } catch (NumberFormatException b) {
          sh.addMsg("processTurn - multi - variable to Int error: " + b);
        }
      }
      // burning pile if a 10 is played or 4 of a kind
      if (pile[0].getValue() == 10 || fourOfAKind(pile[3]) == true) {
        burnt = true;
        burn = true;
        // removing cards from pile
        sh.addMsg(otherNames[playernumber] + " burnt the pile.");
        for (int i = 0; i < 51; i++) pile[i] = null;
      }
      return burn;
    }

    private void yourturn() {
      // making sure turn arrow is pointed correctly
      if (whosturn != 3) {
        whosturn = 3;
        displayTable();
      }

      int top = 0;
      // testing is player has a card they can play
      if (pile[0] != null) {
        boolean canplay = false;
        if (hand.getCard(0) == null) { // if player only has card on the table
          if (hand.isFaceUp()) // if player has faceup card on the table
          {
            for (int n = 0; n < 3; n++) {
              if (hand.getFaceUp(n) != null) {
                if (nine == true && pile[0].getValue() == 9) {
                  top = 0;
                  for (int i = 0; i < 52; i++) {
                    if (pile[i] == null) {
                      canplay = true;
                      break;
                    }
                    if (pile[i].getValue() == 9) top++;
                    else break;
                  }
                }
                if (canplay) break;
                if (seven == true
                    && pile[top].getValue() == 7
                    && hand.getFaceUp(n).getValue() < 7) {
                  canplay = true;
                  break;
                } else if (hand.getFaceUp(n).getValue() == 2
                    || hand.getFaceUp(n).getValue() == 10) {
                  canplay = true;
                  break;
                } else if (nine == true && hand.getFaceUp(n).getValue() == 9) {
                  canplay = true;
                  break;
                } else if (seven != true || pile[top].getValue() != 7) {
                  if (pile[top].getValue() <= hand.getFaceUp(n).getValue()) {
                    canplay = true;
                    break;
                  }
                }
              }
            }
          } else // if player only has facedown cards
          canplay = true;
        } else {
          for (int n = 0; n < hand.length() - 1; n++) {
            if (hand.getCard(n) == null) break;
            if (nine == true && pile[0].getValue() == 9) {
              top = 0;
              for (int i = 0; i < 52; i++) {
                if (pile[i] == null) {
                  canplay = true;
                  break;
                }
                if (pile[i].getValue() == 9) top++;
                else break;
              }
            }
            if (canplay) break;
            if (hand.getCard(n).getValue() == 2 || hand.getCard(n).getValue() == 10) {
              canplay = true;
              break;
            }
            if (nine == true && hand.getCard(n).getValue() == 9) {
              canplay = true;
              break;
            }
            if (seven == true && pile[top].getValue() == 7 && hand.getCard(n).getValue() < 7) {
              canplay = true;
              break;
            } else if (seven != true || pile[top].getValue() != 7) {
              if (pile[top].getValue() <= hand.getCard(n).getValue()) {
                canplay = true;
                break;
              }
            }
          }
        }
        if (canplay) {
          // sh.addMsg("Its Your Turn");
          sh.setmyTurn(true);
        } else { // cant play then must pick up the pile
          sh.addMsg(
              "The card played was a "
                  + pile[top].getStringValue()
                  + " you had to pick up the pile. BLAOW");
          for (int n = 0; n < 52; n++) {
            if (pile[n] == null) break;
            hand.addCard(pile[n]);
            pile[n] = null;
          }
          sendCommand("turn:pickup:");
          nextTurn();
          displayTable();
        }
      } else {
        // sh.addMsg("Its Your Turn");
        sh.setmyTurn(true);
      }
    }

    private void getcard(String otherplayermsg, int commandlength) {
      // decode variable that came with message
      int varlength = 0;
      for (int n = commandlength + 1; n < otherplayermsg.length(); n++) {
        char extract = otherplayermsg.charAt(n);
        if (extract == (':')) {
          varlength = n;
          break;
        }
      }
      String variable = otherplayermsg.substring(commandlength + 1, varlength);

      // Adding card to hand
      try {
        Card card = new Card(Integer.parseInt(variable), sh, g);
        hand.addCard(card);
      } catch (NumberFormatException b) {
        sh.addMsg("Deal - variable to Int error: " + b);
      }
      deck--;
      displayTable();
    }

    private void deal(String otherplayermsg, int commandlength) {
      // decode variable that came with message
      int varlength = 0;
      for (int n = commandlength + 1; n < otherplayermsg.length(); n++) {
        char extract = otherplayermsg.charAt(n);
        if (extract == (':')) {
          varlength = n;
          break;
        }
      }
      String variable = otherplayermsg.substring(commandlength + 1, varlength);

      // Adding card to deck
      try {
        Card card = new Card(Integer.parseInt(variable), sh, g);
        hand.deal(card);
      } catch (NumberFormatException b) {
        sh.addMsg("Deal - variable to Int error: " + b);
      }
    }

    // getting the details of other players
    private void otherdetails(String otherplayermsg, int commandlength) {
      // decode variable that came with message
      String variables[] = new String[12];
      for (int i = 0; i < 12; i++) {
        int varlength = 0;
        for (int n = commandlength + 1; n < otherplayermsg.length(); n++) {
          char extract = otherplayermsg.charAt(n);
          if (extract == (':')) {
            varlength = n;
            break;
          }
        }
        variables[i] = otherplayermsg.substring(commandlength + 1, varlength);
        commandlength = varlength;
      }

      // adding other players details to storage arrays
      for (int n = 0; n < 3; n++) {
        otherNames[n] = variables[4 * n];
        score.addName(variables[4 * n]);
        // sh.addMsg("Test - othername " + otherNames[n] + " severname " +
        // servername);//--------------------TEST
        if (otherNames[n].equals(servername)) whosturn = n;
        cardcount[n] = 3;
        carddowncount[n] = 3;
        try {
          for (int i = 0; i < 3; i++) {
            Card card = new Card(Integer.parseInt(variables[4 * n + 1 + i]), sh, g);
            faceup[n][i] = card;
          }
        } catch (NumberFormatException b) {
          sh.addMsg("otherdetails - variable to Int error: " + b);
        }
      }
      deck = 16;
      if (fastgame) deck = 0;
      displayTable();
      if (swap == true && swapdone == false) { // if performing card swap
        Card inhand[] = new Card[3];
        Card ontable[] = new Card[3];
        for (int n = 0; n < 3; n++) {
          inhand[n] = hand.getCard(n);
          ontable[n] = hand.getFaceUp(n);
        }
        SwapD swapD = new SwapD(sh, inhand, ontable);
        if (swapD.display()) {
          inhand = swapD.getInHand();
          ontable = swapD.getOnTable();
          hand.swap(inhand, ontable);
          displayTable();
        }
        sendCommand(
            "swap:"
                + inhand[0].getNumber()
                + ":"
                + inhand[1].getNumber()
                + ":"
                + inhand[2].getNumber()
                + ":"
                + ontable[0].getNumber()
                + ":"
                + ontable[1].getNumber()
                + ":"
                + ontable[2].getNumber()
                + ":");
        swapdone = true;
      }
    }
  }
}
