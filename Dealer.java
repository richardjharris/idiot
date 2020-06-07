import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;

/**
 * Class for controlling game play ie game dealer
 * 
 * The Dealer class is the host of network games. Non-host players use the
 * Player class.
 *
 * @author Sam Cavenagh
 * @version 6/11/02
 *     <p>Website: http://home.pacific.net.au/~cavenagh/SH/ Email: cavenaghweb@hotmail.com
 */
class Dealer extends PlayerBase {
  // how long ai pause for before making move(int ms)
  int aipause = 1000;

  int swapcount = 0;

  ServerSocket listenSocket;
  Socket gameSocket[] = new Socket[3];
  PrintWriter out[] = new PrintWriter[3];
  BufferedReader in[] = new BufferedReader[3];

  // Excludes dealer
  boolean waitformsg[] = { false, false, false };
  boolean aiPlayer[] = { false, false, false };

  int socketCount = 0;

  Card deck[] = new Card[52];
  Hand hands[] = new Hand[4];
  GameAI ai;

  int gameID = 1; // Stops AI playing a move in wrong game

  private final String[] aiNames = {
    "Dan",
    "Greg",
    "Nick",
  };

  Dealer(
      SHinterface sh,
      Graphics g,
      Hand hand,
      boolean fastgame,
      boolean seven,
      boolean nine,
      boolean swap,
      Score score) {
    super(sh, g, score);
    this.fastgame = fastgame;
    this.seven = seven;
    this.nine = nine;
    this.swap = swap;
    this.whosturn = 2;

    score.imDealer(this);

    BufferedImage back = sh.getImageManager().getCardBack();
    hands[0] = new Hand(sh, back, g);
    hands[1] = new Hand(sh, back, g);
    hands[2] = new Hand(sh, back, g);
    hands[3] = hand;

    ai = new GameAI(seven, nine);
  }

  public Hand ownHand() {
    return hands[3];
  }

  public void onePlayer(String playersName) {
    this.playersName = playersName;
    score.addName(playersName);
    for (int n = 0; n < 3; n++) {
      otherNames[n] = aiNames[n];
      score.addName(otherNames[n]);
      aiPlayer[n] = true;
      socketCount++;
    }
    deal();
  }

  public void start() {
    if (listen && socketCount < 3) {
      for (int n = 0; n < 3; n++)
        if (gameSocket[n] == null) {
          otherNames[n] = "AI" + (n + 1);
          score.addName(otherNames[n]);
          aiPlayer[n] = true;
          socketCount++;
        }

      try {
        listenSocket.close();
      } catch (IOException e) {
        sh.addMsg("Error Closing Listen " + e);
      }
    }
  }

  public int getAIPause() {
    return aipause;
  }

  public void setAIPause(int aipause) {
    this.aipause = aipause;
  }

  private void shuffle() {
    for (int r = 0; r < 2; r++) // number of times pile shuffled
    for (int n = 0; n < 52; n++) {
        int newlocation = (int) Math.round(Math.random() * 51);
        Card temp = deck[newlocation];
        deck[newlocation] = deck[n];
        deck[n] = temp;
      }
  }

  public void createConnection(String playersName) {
    this.playersName = playersName;
    score.addName(playersName);
    new ListenThread();
  }

  private void sendCommand(String command, int socNumber) {
    // sh.addMsg("msg:" + command + " socket:" + socNumber);
    if (socNumber < 3) if (gameSocket[socNumber] != null) out[socNumber].println(command);
  }

  private void sendMsg(String msg) {
    for (int n = 0; n < 3; n++) {
      if (out[n] != null) out[n].println("msg:" + msg);
    }
  }

  private void deal() {
    // adding card to deck
    for (int n = 0; n < 52; n++) {
      Card card = new Card(n + 1, sh, g);
      deck[n] = card;
    }
    shuffle(); // shuffling card deck
    int player = whosturn;
    for (int n = 0; n < 36; n++) {
      // dealing first card in deck
      if (player != 3) // player 3 is dealer no need to send command
      sendCommand("deal:" + deck[0].getNumber() + ":", player);
      // placing cards in players hand, facedown then faceup.
      hands[player].deal(deck[0]);

      // moving cards in deck forwards
      for (int w = 0; w < 51; w++) deck[w] = deck[w + 1];

      // removing card from deck
      deck[51] = null;

      // changing player being dealt to
      player++;
      if (player >= 4) player = 0;
    }
    sendDetails();

    if (fastgame) for (int n = 0; n < 52; n++) deck[n] = null;

    if (swap) { // if performing card swap
      displayTable();
      Card inhand[] = new Card[3];
      Card ontable[] = new Card[3];
      for (int n = 0; n < 3; n++) {
        waitformsg[n] = true;
        inhand[n] = ownHand().getCard(n);
        ontable[n] = ownHand().getFaceUp(n);
      }
      SwapD swapD = new SwapD(sh, inhand, ontable);
      if (swapD.display()) {
        ownHand().swap(swapD.getInHand(), swapD.getOnTable());
        displayTable();
      }
      swapcount++;

      // if dealer is last to make the swap
      if (swapcount == 4) {
        sendDetails();
        nextTurn();
        displayTable();
      }

      // swap for ai players
      for (int n = 0; n < 3; n++) if (aiPlayer[n]) processSwap(ai.swap(hands[n], nine), 4, n);

    } else { // else start game
      nextTurn();
      displayTable();
    }
  }

  /*---------------------------------------------
  * sending detail of other players to other players
  * table layout
  *     1
  *  0     2
        3 <- dealer
  *--------------------------------------------*/
  private void sendDetails() {
    String command =
        "otherdetails:"
            + otherNames[1]
            + ":"
            + hands[1].getFaceUp(0).getNumber()
            + ":"
            + hands[1].getFaceUp(1).getNumber()
            + ":"
            + hands[1].getFaceUp(2).getNumber()
            + ":"
            + otherNames[2]
            + ":"
            + +hands[2].getFaceUp(0).getNumber()
            + ":"
            + hands[2].getFaceUp(1).getNumber()
            + ":"
            + hands[2].getFaceUp(2).getNumber()
            + ":"
            + playersName
            + ":"
            + hands[3].getFaceUp(0).getNumber()
            + ":"
            + +hands[3].getFaceUp(1).getNumber()
            + ":"
            + hands[3].getFaceUp(2).getNumber()
            + ":";
    sendCommand(command, 0);

    command =
        "otherdetails:"
            + otherNames[2]
            + ":"
            + hands[2].getFaceUp(0).getNumber()
            + ":"
            + hands[2].getFaceUp(1).getNumber()
            + ":"
            + hands[2].getFaceUp(2).getNumber()
            + ":"
            + playersName
            + ":"
            + +hands[3].getFaceUp(0).getNumber()
            + ":"
            + hands[3].getFaceUp(1).getNumber()
            + ":"
            + hands[3].getFaceUp(2).getNumber()
            + ":"
            + otherNames[0]
            + ":"
            + hands[0].getFaceUp(0).getNumber()
            + ":"
            + +hands[0].getFaceUp(1).getNumber()
            + ":"
            + hands[0].getFaceUp(2).getNumber()
            + ":";
    sendCommand(command, 1);

    command =
        "otherdetails:"
            + playersName
            + ":"
            + hands[3].getFaceUp(0).getNumber()
            + ":"
            + hands[3].getFaceUp(1).getNumber()
            + ":"
            + hands[3].getFaceUp(2).getNumber()
            + ":"
            + otherNames[0]
            + ":"
            + +hands[0].getFaceUp(0).getNumber()
            + ":"
            + hands[0].getFaceUp(1).getNumber()
            + ":"
            + hands[0].getFaceUp(2).getNumber()
            + ":"
            + otherNames[1]
            + ":"
            + hands[1].getFaceUp(0).getNumber()
            + ":"
            + +hands[1].getFaceUp(1).getNumber()
            + ":"
            + hands[1].getFaceUp(2).getNumber()
            + ":";
    sendCommand(command, 2);
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
      if (gameSocket[n] != null) {
        out[n].println("end");
        try {
          gameSocket[n].close();
        } catch (IOException e2) {
          sh.addMsg("Error Closing Msg " + e2);
        }
      }
    }

    socketCount = 0;
    sh.addMsg("Game Connections Closed");
  }

  public void destroy() {
    gameID++;
    endConnection();
  }

  protected int handLength(int playerNo) {
    return hands[playerNo].length() - 1;
  }

  protected Card getFaceUpCard(int playerNo, int cardIndex) {
    return hands[playerNo].getFaceUp(cardIndex);
  }

  protected boolean hasFaceDownCard(int playerNo, int cardIndex) {
    return hands[playerNo].getFaceDown(cardIndex) != null;
  }

  public void redeal() {

    if (socketCount >= 3) {

      for (int n = 0; n < 4; n++) {
        // setting scoreboard if game finishes early
        if (n < 3) {
          if (outofgame[n] == false) score.addScore(otherNames[n], position);
        } else {
          if (outofgame[n] == false) score.addScore(playersName, position);
        }

        outofgame[n] = false;
        if (n < 3) waitformsg[n] = false;
        hands[n].removeAll();
      }

      whosturn = 2;
      position = 1;
      swapcount = 0;

      pile.clear();
      for (int n = 0; n < 3; n++) sendCommand("reset:", n);
      deal();
    }
  }

  protected int deckLength() {
    int cardCount = 0;
    for (int n = 0; n < 52; n++) {
      if (deck[n] == null) break;
      cardCount++;
    }
    return cardCount;
  }

  private void outofgame(int playerout) {

    String name = "";
    // telling whos out of the game there are out
    if (playerout == 3) { // dealer out of game
      if (position == 1) sh.addMsg("Well done you have won the game your the first out !!");
      else if (position == 2) sh.addMsg("You've done alright you the second out of the game");
      else if (position == 3) sh.addMsg("Just made it, congrats your not a ShitHead !");
      name = playersName;
    } else {
      sendCommand("out:" + position + ":", playerout);
      name = otherNames[playerout];
      sh.addMsg(name + " is out of the game");
    }

    // updating scoreboard
    score.addScore(name, position);

    // sending message to everyone else
    for (int n = 0; n < 3; n++) if (n != playerout) sendCommand("otherout:" + name + ":", n);

    outofgame[playerout] = true;
    position++;

    // ending game if over
    if (position == 4) {
      gameID++;
      int wholost = 0;
      // finding out which player is still in the game
      for (int n = 0; n < 4; n++)
        if (!outofgame[n]) {
          wholost = n;
          outofgame[n] = true;
          break;
        }
      if (wholost == 3) {
        sh.addMsg("You Lost ShitHead !!!");
        sh.addMsg("Game Over");
        name = playersName;
        displayTable();
      } else {
        sendCommand("out:" + position + ":", wholost);
        name = otherNames[wholost];
        sh.addMsg(name + " is the Shithead");
        sh.addMsg("Game Over");
        whosturn = 3;
        displayTable();
      }

      // updating scoreboard
      score.addScore(name, 4);
      new ScoreThread();

      // sending message to everyone else
      for (int n = 0; n < 3; n++) if (otherNames[n] != name) sendCommand("lost:" + name + ":", n);
      outofgame[wholost] = true;
    }
  }

  public void cardSelection(int cardno) {
    Card card;
    String command;
    if (ownHand().getCard(0) == null) { // if player only has cards on table
      if (ownHand().getFaceUp(0) != null
          || ownHand().getFaceUp(1) != null
          || ownHand().getFaceUp(2) != null) { // if cards still faceup on table
        card = ownHand().getFaceUp(cardno);
        command = "othersturn:" + playersName + ":faceup:";
        if (isValidCard(card, command)) ownHand().removeFaceUp(cardno);
      } else { // if only cards down
        card = ownHand().getFaceDown(cardno);
        command = "othersturn:" + playersName + ":facedown:";
        if (isValidCard(card, command)) {
          ownHand().removeFaceDown(cardno);
          // checking if player is out of the game
          if (ownHand().getFaceDown(0) == null
              && ownHand().getFaceDown(1) == null
              && ownHand().getFaceDown(2) == null) outofgame(3);
        } else { // player must pick up the deck if not valid
          sh.addMsg(
              "The card you played was a "
                  + ownHand().getFaceDown(cardno).getStringValue()
                  + " you had to pick up the pile. BLAOW");
          ownHand().addCard(ownHand().getFaceDown(cardno));
          for (int n = 0; n < 3; n++)
            sendCommand(
                "othersturn:"
                    + playersName
                    + ":facedown:pickup:"
                    + ownHand().getFaceDown(cardno).getNumber()
                    + ":",
                n);
          pile.moveToHand(ownHand());
          ownHand().removeFaceDown(cardno);
          nextTurn();
        }
      }
    } else { // if player still has cards in hand
      card = ownHand().getCard(cardno);
      command = "othersturn:" + playersName + ":";
      if (isValidCard(card, command))
        for (int n = 0; n < ownHand().length() - 1; n++)
          if (card.getNumber() == ownHand().getCard(n).getNumber()) {
            ownHand().removeCard(n);
            break;
          }
      // checking if player is out of the game
      if (ownHand().getFaceDown(0) == null
          && ownHand().getFaceDown(1) == null
          && ownHand().getFaceDown(2) == null
          && ownHand().getCard(0) == null) {
        outofgame(3);
        if (whosturn == 3) nextTurn();
      }
    }
    // if players hand is less than 3 card and card still in deck give another card to player
    while (deck[0] != null && ownHand().length() <= 3) {
      ownHand().addCard(deck[0]);
      // removing card from deck
      deck[51] = null;
      for (int w = 0; w < 51; w++) deck[w] = deck[w + 1];
    }
    displayTable();
  }

  private boolean checkformultiFaceUp(Card card) {
    if (!canAddToPile(card.getValue())) {
      return false;
    }

    // checking how many card of the same value as card played are in players hand
    int amountinhand = 0;
    for (int n = 0; n < 3; n++) {
      if (ownHand().getFaceUp(n) != null)
        if (ownHand().getFaceUp(n).getValue() == card.getValue()) amountinhand++;
    }
    if (amountinhand <= 1) return false;
    MultiCardD dialog = new MultiCardD(sh, amountinhand);
    int numbertoplay = dialog.getChoice();
    if (numbertoplay <= 1) return false;
    String command =
        "othersturn:"
            + playersName
            + ":faceup:multi:"
            + numbertoplay
            + ":"
            + card.getNumber()
            + ":";
    pile.add(card);
    numbertoplay--;
    for (int n = 0; n < 3; n++)
      if (ownHand().getFaceUp(n) != null) {
        if (numbertoplay <= 0) break;
        if (card.getValue() == ownHand().getFaceUp(n).getValue()
            && card.getNumber() != ownHand().getFaceUp(n).getNumber()) {
          command = command.concat(ownHand().getFaceUp(n).getNumber() + ":");
          pile.add(ownHand().getFaceUp(n));
          // storing which card are to be removed
          ownHand().removeFaceUp(n);
          numbertoplay--;
        }
      }
    // sending command
    for (int n = 0; n < 3; n++) sendCommand(command, n);
    // checking for 4 of a kind
    if (pile.isFourOfAKind() || pile.topValue() == 10) {
      burnPile();
      sh.addMsg("You burn the pile is your turn again");
    } else {
      sh.setmyTurn(false);
      nextTurn();
    }
    return true;
  }

  private boolean checkformulti(Card card) {
    if (!canAddToPile(card.getValue())) {
      return false;
    }

    // checking how many card of the same value as card played are in players hand
    int amountinhand = 0;
    for (int n = 0; n < ownHand().length(); n++) {
      if (ownHand().getCard(n) == null) break;
      if (ownHand().getCard(n).getValue() == card.getValue()) amountinhand++;
    }
    if (amountinhand <= 1) return false;
    MultiCardD dialog = new MultiCardD(sh, amountinhand);
    int numbertoplay = dialog.getChoice();
    if (numbertoplay <= 1) return false;
    String command =
        "othersturn:" + playersName + ":multi:" + numbertoplay + ":" + card.getNumber() + ":";
    pile.add(card);
    numbertoplay--;
    int toberemovedcount = 0;
    int toberemoved[] = new int[3];
    for (int n = 0; n < 3; n++) toberemoved[n] = -1;
    for (int n = 0; n < ownHand().length() - 1; n++) {
      if (ownHand().getCard(n) == null) break;
      if (numbertoplay <= 0) break;
      if (card.getValue() == ownHand().getCard(n).getValue()
          && card.getNumber() != ownHand().getCard(n).getNumber()) {
        command = command.concat(ownHand().getCard(n).getNumber() + ":");
        pile.add(ownHand().getCard(n));
        // storing which card are to be removed
        toberemoved[toberemovedcount] = ownHand().getCard(n).getNumber();
        toberemovedcount++;
        numbertoplay--;
      }
    }
    // removing card from hand
    for (int n = 0; n < 3; n++) {
      if (toberemoved[n] == -1) break;
      for (int i = 0; i < ownHand().length() - 1; i++)
        if (ownHand().getCard(i).getNumber() == toberemoved[n]) {
          ownHand().removeCard(i);
          break;
        }
    }
    // sending command
    for (int n = 0; n < 3; n++) sendCommand(command, n);
    // checking for 4 of a kind
    if (pile.isFourOfAKind() || pile.topValue() == 10) {
      burnPile();
      // checking if player is out of game
      if (ownHand().getFaceDown(0) == null
          && ownHand().getFaceDown(1) == null
          && ownHand().getFaceDown(2) == null
          && ownHand().getCard(0) == null) nextTurn();
      else // else there go again
      sh.addMsg("You burn the pile is your turn again");
    } else {
      sh.setmyTurn(false);
      nextTurn();
    }
    return true;
  }

  private void nextTurn() {
    whosturn++;
    if (whosturn >= 4) whosturn = 0;
    int loop = 0; // stop program becoming trapped in loop
    while (outofgame[whosturn] && loop < 5) {
      loop++;
      whosturn++;
      if (whosturn >= 4) whosturn = 0;
    }
    if (whosturn != 3) {
      waitformsg[whosturn] = true;
      if (!aiPlayer[whosturn]) {
        sendCommand("yourturn:", whosturn);
      } else {
        new AIThread();
      }
    } else {
      canDealerPlay();
    }
  }

  public boolean isValidCard(Card card, String command) {
    boolean multi = false;
    if (ownHand().getCard(0) != null) multi = checkformulti(card);
    else if (ownHand().isFaceUp()) multi = checkformultiFaceUp(card);

    if (multi) return true;

    if (card.getValue() == 2) { // 2 can be played at anytime
      cardAccepted(card, command);
      return true;
    } else if (card.getValue() == 10
        || pile.makesFourOfAKind(card)) { // pile is burn and its players turn again
      burnPile();
      if (ownHand().isFaceDown() == true || ownHand().length() > 1) {
        for (int n = 0; n < 3; n++)
          if (ownHand().length() > 1) sendCommand(command + "burn:", n);
          else sendCommand(command + card.getNumber() + ":", n);
        sh.addMsg("You have burnt the pile, its your turn again");
      } else {
        for (int n = 0; n < 3; n++)
          if (ownHand().length() > 1) sendCommand(command + "burn:", n);
          else sendCommand(command + card.getNumber() + ":", n);
        sh.setmyTurn(false);
        nextTurn();
      }
      return true;
    } else if (pile.isEmpty()) {
      cardAccepted(card, command);
      return true;
    } else if (nine == true && card.getValue() == 9) {
      cardAccepted(card, command);
      return true;
    }

    int topValue = nine ? pile.topValueExcludingNines() : pile.topValue();
    if (topValue == -1) {
      // Pile is only nines
      cardAccepted(card, command);
      return true;
    }
    if (seven && topValue == 7) {
      if (card.getValue() >= 7) {
        sh.addMsg("You Must Play Less Than a Seven");
        sh.soundManager.playTwang();
        return false;
      } else {
        cardAccepted(card, command);
        return true;
      }
    }
    if (topValue <= card.getValue()) {
      cardAccepted(card, command);
      return true;
    }
    if (ownHand().isFaceUp() || ownHand().length() > 1) { // top
      sh.addMsg("You can't play a " + card.getStringValue() + " please select another card");
    }
    return false;
  }

  private void cardAccepted(Card card, String command) {
    // sending move to other players
    for (int n = 0; n < 3; n++) sendCommand(command + card.getNumber() + ":", n);
    // adding card to pile
    pile.add(card);
    sh.setmyTurn(false);
    nextTurn();
  }

  public void processTurn(String otherplayermsg, int commandlength) {
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
    String variable = otherplayermsg.substring(commandlength + 1, varlength);
    if (variable.equals("pickup")) { // if other player picks up deck
      pile.moveToHand(hands[whosturn]);
      // sending move to other players
      for (int n = 0; n < 3; n++)
        if (n != whosturn) sendCommand("othersturn:" + otherNames[whosturn] + ":pickup:", n);
      sh.addMsg(otherNames[whosturn] + " picked up the pile");
    } else if (variable.equals("faceup")) { // if player plays one of there face up cards
      int varlength2 = 0;
      for (int n = varlength + 1; n < otherplayermsg.length(); n++) {
        char extract = otherplayermsg.charAt(n);
        if (extract == (':')) {
          varlength2 = n;
          break;
        }
      }
      String cardno = otherplayermsg.substring(varlength + 1, varlength2);
      if (cardno.equals("multi")) {
        burn = faceupMulti(otherplayermsg, varlength2);
      } else {
        int cardNo = 0;
        try {
          cardNo = Integer.parseInt(cardno);
        } catch (NumberFormatException b) {
          sh.addMsg("processTurn - variable to Int error: " + b);
        }
        // removing card from players hand and adding to pile
        for (int n = 0; n < 3; n++) {
          if (hands[whosturn].getFaceUp(n) != null)
            if (hands[whosturn].getFaceUp(n).getNumber() == cardNo) {
              if (hands[whosturn].getFaceUp(n).getValue() == 10
                  || pile.makesFourOfAKind(hands[whosturn].getFaceUp(n))) { // if card is 10 burning the deck
                burnPile();
                burn = true;
                sh.addMsg(otherNames[whosturn] + " burnt the pile");
              } else { // else adding card to pile
                pile.add(hands[whosturn].getFaceUp(n));
              }
              hands[whosturn].removeFaceUp(n);
              break;
            }
        }
        // sending move to other players
        for (int n = 0; n < 3; n++)
          if (n != whosturn)
            sendCommand("othersturn:" + otherNames[whosturn] + ":faceup:" + cardno + ":", n);
      }

    } else if (variable.equals("facedown")) { // if player plays a facedown card
      int varlength2 = 0;
      for (int n = varlength + 1; n < otherplayermsg.length(); n++) {
        char extract = otherplayermsg.charAt(n);
        if (extract == (':')) {
          varlength2 = n;
          break;
        }
      }
      String cardno = otherplayermsg.substring(varlength + 1, varlength2);
      if (cardno.equals("pickup")) { // card that was played facedown made player pick up the deck
        int varlength3 = 0; // getting card that player played
        for (int n = varlength2 + 1; n < otherplayermsg.length(); n++) {
          char extract = otherplayermsg.charAt(n);
          if (extract == (':')) {
            varlength3 = n;
            break;
          }
        }
        String cardno2 = otherplayermsg.substring(varlength2 + 1, varlength3);
        int cardNo = 0;
        try {
          cardNo = Integer.parseInt(cardno2);
        } catch (NumberFormatException b) {
          sh.addMsg("processTurn facedown pickup- variable to Int error: " + b);
        }
        for (int n = 0; n < 3; n++)
          if (hands[whosturn].getFaceDown(n) != null)
            if (hands[whosturn].getFaceDown(n).getNumber() == cardNo) {
              hands[whosturn].addCard(hands[whosturn].getFaceDown(n));
              pile.moveToHand(hands[whosturn]);
              for (int i = 0; i < 3; i++)
                if (i != whosturn)
                  sendCommand(
                      "othersturn:" + otherNames[whosturn] + ":facedown:pickup:" + cardNo + ":", i);
              sh.addMsg(
                  otherNames[whosturn]
                      + " played a "
                      + Card.getCardStringValue(cardNo)
                      + " and had to pick up the deck. AWRGH");
              hands[whosturn].removeFaceDown(n);
              break;
            }
      } else { // facedown card didnt make player pick up the deck
        int cardNo = 0;
        try {
          cardNo = Integer.parseInt(cardno);
        } catch (NumberFormatException b) {
          sh.addMsg("processTurn - variable to Int error: " + b);
        }
        // removing card from players hand and adding to pile
        for (int n = 0; n < 3; n++) {
          if (hands[whosturn].getFaceDown(n) != null)
            if (hands[whosturn].getFaceDown(n).getNumber() == cardNo) {
              if (hands[whosturn].getFaceDown(n).getValue() == 10
                  || pile.makesFourOfAKind(hands[whosturn].getFaceDown(n))
                      == true) { // if card is 10 burning the deck
                burnPile();
                if (hands[whosturn].isFaceDown() == true || hands[whosturn].length() > 1)
                  burn = true;
                sh.addMsg(otherNames[whosturn] + " burnt the pile");
              } else { // else adding card to pile
                pile.add(hands[whosturn].getFaceDown(n));
              }
              hands[whosturn].removeFaceDown(n);
              break;
            }
        }
        // sending move to other players
        for (int n = 0; n < 3; n++)
          if (n != whosturn)
            sendCommand("othersturn:" + otherNames[whosturn] + ":facedown:" + cardno + ":", n);
        // Check for out of game
        if (hands[whosturn].getFaceDown(0) == null
            && hands[whosturn].getFaceDown(1) == null
            && hands[whosturn].getFaceDown(2) == null
            && hands[whosturn].getCard(0) == null) outofgame(whosturn);
      }

    } else if (variable.equals("multi")) { // if player plays a muliple cards
      // determining how many card where played
      int varlength2 = 0;
      for (int n = varlength + 1; n < otherplayermsg.length(); n++) {
        char extract = otherplayermsg.charAt(n);
        if (extract == (':')) {
          varlength2 = n;
          break;
        }
      }
      String numPlayedString = otherplayermsg.substring(varlength + 1, varlength2);
      // converting string to int for processing
      int numPlayed = 0;
      try {
        numPlayed = Integer.parseInt(numPlayedString);
      } catch (NumberFormatException b) {
        sh.addMsg("processTurn - multi - variable to Int error: " + b);
      }
      String command = "othersturn:" + otherNames[whosturn] + ":multi:" + numPlayed + ":";
      for (int n = 0; n < numPlayed; n++) {
        varlength = varlength2;
        // determining how many card where played
        varlength2 = 0;
        for (int i = varlength + 1; i < otherplayermsg.length(); i++) {
          char extract = otherplayermsg.charAt(i);
          if (extract == (':')) {
            varlength2 = i;
            break;
          }
        }
        String cardnoString = otherplayermsg.substring(varlength + 1, varlength2);
        // converting string to int for processing
        int cardno = 0;
        try {
          cardno = Integer.parseInt(cardnoString);
        } catch (NumberFormatException b) {
          sh.addMsg("processTurn - multi - variable to Int error: " + b);
        }
        int location = 0;
        for (int i = 0; i < hands[whosturn].length() - 1; i++)
          if (hands[whosturn].getCard(i).getNumber() == cardno) {
            location = i;
            break;
          }
        pile.add(hands[whosturn].getCard(location));
        command = command.concat(hands[whosturn].getCard(location).getNumber() + ":");
        hands[whosturn].removeCard(location);
      }
      // if players hand is less than 3 card ands card still in deck give another card to player
      while (deck[0] != null && hands[whosturn].length() <= 3) {
        hands[whosturn].addCard(deck[0]);
        sendCommand("getcard:" + deck[0].getNumber() + ":", whosturn);
        // removing card from deck
        deck[51] = null;
        for (int w = 0; w < 51; w++) deck[w] = deck[w + 1];
      }
      // sending turn to other players
      for (int n = 0; n < 3; n++) if (n != whosturn) sendCommand(command, n);
      // Check for out of game
      if (hands[whosturn].getFaceDown(0) == null
          && hands[whosturn].getFaceDown(1) == null
          && hands[whosturn].getFaceDown(2) == null
          && hands[whosturn].getCard(0) == null) outofgame(whosturn);
      // checking for 4 of a kind else next players turn
      if (pile.isFourOfAKind() || pile.topValue() == 10) {
        if (!outofgame[whosturn]) {
          burnPile();
          sh.addMsg(otherNames[whosturn] + " burn the pile");
          burn = true;
        }
      }

    } else { // regular game play
      // converting string to int for processing
      int cardno = 0;
      try {
        cardno = Integer.parseInt(variable);
      } catch (NumberFormatException b) {
        sh.addMsg("processTurn - variable to Int error: " + b);
      }

      // removing card from players hand and adding to pile
      for (int n = 0; n < hands[whosturn].length() - 1; n++) {
        if (hands[whosturn].getCard(n) == null) break;
        if (hands[whosturn].getCard(n).getNumber() == cardno) {
          if (hands[whosturn].getCard(n).getValue() == 10
              || pile.makesFourOfAKind(hands[whosturn].getCard(n))) { // if card is 10 burning the deck
            burnPile();
            burn = true;
            sh.addMsg(otherNames[whosturn] + " burnt the pile");
          } else { // else adding card to pile
            pile.add(hands[whosturn].getCard(n));
          }
          hands[whosturn].removeCard(n);
          break;
        }
      }

      // if players hand is less than 3 card ands card still in deck give another card to player
      if (deck[0] != null && hands[whosturn].length() <= 3) {
        hands[whosturn].addCard(deck[0]);
        sendCommand("getcard:" + deck[0].getNumber() + ":", whosturn);
        // removing card from deck
        deck[51] = null;
        for (int w = 0; w < 51; w++) deck[w] = deck[w + 1];
      }

      if (burn) {
        // sending move to other players
        for (int n = 0; n < 3; n++)
          if (n != whosturn) sendCommand("othersturn:" + otherNames[whosturn] + ":burn:", n);
      } else {
        // sending move to other players
        for (int n = 0; n < 3; n++)
          if (n != whosturn)
            sendCommand("othersturn:" + otherNames[whosturn] + ":" + cardno + ":", n);
      }

      // Check for out of game
      if (hands[whosturn].getFaceDown(0) == null
          && hands[whosturn].getFaceDown(1) == null
          && hands[whosturn].getFaceDown(2) == null
          && hands[whosturn].getCard(0) == null) outofgame(whosturn);
    }
    if (!burn && position != 4) {
      if (whosturn != 3) waitformsg[whosturn] = false;
      nextTurn();
    }
    displayTable();
  }

  // returns true if pile burnt, false if not burnt
  private boolean faceupMulti(String otherplayermsg, int varlength) {
    // determining how many card where played
    int varlength2 = 0;
    for (int n = varlength + 1; n < otherplayermsg.length(); n++) {
      char extract = otherplayermsg.charAt(n);
      if (extract == (':')) {
        varlength2 = n;
        break;
      }
    }
    String numPlayedString = otherplayermsg.substring(varlength + 1, varlength2);
    // converting string to int for processing
    int numPlayed = 0;
    try {
      numPlayed = Integer.parseInt(numPlayedString);
    } catch (NumberFormatException b) {
      sh.addMsg("processTurn - multi - variable to Int error: " + b);
    }
    String command = "othersturn:" + otherNames[whosturn] + ":faceup:multi:" + numPlayed + ":";
    for (int n = 0; n < numPlayed; n++) {
      varlength = varlength2;
      // determining how many card where played
      varlength2 = 0;
      for (int i = varlength + 1; i < otherplayermsg.length(); i++) {
        char extract = otherplayermsg.charAt(i);
        if (extract == (':')) {
          varlength2 = i;
          break;
        }
      }
      String cardnoString = otherplayermsg.substring(varlength + 1, varlength2);
      // converting string to int for processing
      int cardno = 0;
      try {
        cardno = Integer.parseInt(cardnoString);
      } catch (NumberFormatException b) {
        sh.addMsg("processTurn - multi - variable to Int error: " + b);
      }
      int location = 0;
      for (int i = 0; i < 3; i++)
        if (hands[whosturn].getFaceUp(i) != null)
          if (hands[whosturn].getFaceUp(i).getNumber() == cardno) {
            location = i;
            break;
          }
      pile.add(hands[whosturn].getFaceUp(location));
      command = command.concat(hands[whosturn].getFaceUp(location).getNumber() + ":");
      hands[whosturn].removeFaceUp(location);
    }
    // sending turn to other players
    for (int n = 0; n < 3; n++) if (n != whosturn) sendCommand(command, n);
    // checking for 4 of a kind else next players turn
    if (pile.isFourOfAKind() || pile.topValue() == 10) {
      burnPile();
      sh.addMsg(otherNames[whosturn] + " burn the pile");
      return true;
    }
    return false;
  }

  private void canDealerPlay() {
    // testing is player has a card they can play
    if (pile.notEmpty()) {
      int top = 0;
      boolean canplay = false;
      if (ownHand().getCard(0) == null) { // if player only has card on the table
        if (ownHand().isFaceUp()) // if player has faceup card on the table
        {
          for (int n = 0; n < 3; n++) {
            if (ownHand().getFaceUp(n) != null) {
              if (nine == true && pile.topValue() == 9) {
                top = 0;
                for (int i = 0; i < 52; i++) {
                  if (pile.get(i) == null) {
                    canplay = true;
                    break;
                  }
                  if (pile.get(i).getValue() == 9) top++;
                  else break;
                }
              }
              if (canplay) break;
              if (seven == true
                  && pile.get(top).getValue() == 7
                  && ownHand().getFaceUp(n).getValue() < 7) {
                canplay = true;
                break;
              } else if (ownHand().getFaceUp(n).getValue() == 2
                  || ownHand().getFaceUp(n).getValue() == 10) {
                canplay = true;
                break;
              } else if (nine == true && ownHand().getFaceUp(n).getValue() == 9) {
                canplay = true;
                break;
              } else if (seven != true || pile.get(top).getValue() != 7) {
                if (pile.get(top).getValue() <= ownHand().getFaceUp(n).getValue()) {
                  canplay = true;
                  break;
                }
              }
            }
          }
        } else // if player only has facedown cards
        canplay = true;
      } else {
        for (int n = 0; n < ownHand().length() - 1; n++) {
          if (ownHand().getCard(n) == null) break;
          if (nine && pile.topValue() == 9) {
            top = 0;
            for (int i = 0; i < 52; i++) {
              if (pile.get(i) == null) {
                canplay = true;
                break;
              }
              if (pile.get(i).getValue() == 9) top++;
              else break;
            }
          }
          if (canplay) break;
          if (ownHand().getCard(n).getValue() == 2 || ownHand().getCard(n).getValue() == 10) {
            canplay = true;
            break;
          }
          if (nine == true && ownHand().getCard(n).getValue() == 9) {
            canplay = true;
            break;
          }
          if (seven == true && pile.get(top).getValue() == 7 && ownHand().getCard(n).getValue() < 7) {
            canplay = true;
            break;
          } else if (seven != true || pile.get(top).getValue() != 7) {
            if (pile.get(top).getValue() <= ownHand().getCard(n).getValue()) {
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
            "The card played was a " + pile.get(top).getStringValue() + " you had to pick up the pile. BLAOW");
        pile.moveToHand(ownHand());
        // sending move to other players
        for (int n = 0; n < 3; n++) sendCommand("othersturn:" + playersName + ":pickup:", n);
        nextTurn();
        displayTable();
      }
    } else {
      // sh.addMsg("Its Your Turn");
      sh.setmyTurn(true);
    }
  }

  private void processSwap(String otherplayermsg, int commandlength, int playerNumber) {
    if (!otherplayermsg.equals("error")) {
      int varlength;
      Card inhand[] = new Card[3];
      Card ontable[] = new Card[3];
      for (int r = 0; r < 2; r++)
        for (int n = 0; n < 3; n++) {
          varlength = commandlength;
          // determining how many card where played
          commandlength = 0;
          for (int i = varlength + 1; i < otherplayermsg.length(); i++) {
            char extract = otherplayermsg.charAt(i);
            if (extract == (':')) {
              commandlength = i;
              break;
            }
          }
          String cardnoString = otherplayermsg.substring(varlength + 1, commandlength);
          int cardno = 0;
          try {
            cardno = Integer.parseInt(cardnoString);
          } catch (NumberFormatException b) {
            sh.addMsg("processSwap - error - variable to Int error: " + b);
          }
          Card card = new Card(cardno, sh, g);
          if (r == 0) inhand[n] = card;
          else ontable[n] = card;
        }
      hands[playerNumber].swap(inhand, ontable);
    }
    waitformsg[playerNumber] = false;
    swapcount++;
    if (swapcount == 4) {
      sendDetails();
      nextTurn();
      displayTable();
    }
  }

  class WaitforMsg implements Runnable {

    Thread wt; // Wait Thread
    int socketNumber; // With of the 3 sockets is this socket listening to ?
    boolean socketOK = true;
    boolean haveswapped = false;

    WaitforMsg(int socketNumber) {
      this.socketNumber = socketNumber;

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
          if (otherplayermsg == null) {
            disconnect();
          } else if (otherplayermsg.equals("end")) {
            disconnect();
          } else {
            if (waitformsg[socketNumber]) {
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

              if (command.equals("turn")) processTurn(otherplayermsg, commandlength);

              if (command.equals("swap")) {
                processSwap(otherplayermsg, commandlength, socketNumber);
                haveswapped = true;
              }
            }
          }
        }

      } while (listen == true && socketOK == true);
      sh.addMsg(
          "Player "
              + otherNames[socketNumber]
              + " Game Socket Has Disconnected \nReplacing player with GameAI");
      sendMsg(
          "Player "
              + otherNames[socketNumber]
              + " Game Socket Has Disconnected Replacing player with GameAI");
    }

    private void disconnect() {
      try {
        gameSocket[socketNumber].close();
      } catch (IOException e) {
        sh.addMsg("Error Closing Listen " + e);
      }
      gameSocket[socketNumber] = null;
      in[socketNumber] = null;
      out[socketNumber] = null;
      aiPlayer[socketNumber] = true;
      if (!haveswapped) swapcount++;
      if (whosturn == socketNumber) new AIThread();
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
        listenSocket = new ServerSocket(4445);
      } catch (IOException e) {
        sh.addMsg("Could not listen " + e);
        return;
      }

      boolean endlook = false;
      // Waiting for connection
      do {
        gameSocket[socketCount] = null;
        try {
          gameSocket[socketCount] = listenSocket.accept();
        } catch (IOException e2) {
          // sh.addMsg("Error Accept " + e2);//removing error notice as apears when game is started
          // early
          endlook = true;
        }

        if (!endlook) {
          try {
            out[socketCount] = new PrintWriter(gameSocket[socketCount].getOutputStream(), true);
            in[socketCount] =
                new BufferedReader(new InputStreamReader(gameSocket[socketCount].getInputStream()));
          } catch (IOException e) {
            sh.addMsg("Error Out / In problem." + e);
          }
          try {
            otherNames[socketCount] = in[socketCount].readLine();
            score.addName(otherNames[socketCount]);
          } catch (IOException e3) {
            sh.addMsg("Getting Otherplayers Name Error " + e3);
          }
          // checking if players name matches name of any of the other players
          for (int n = 0; n < socketCount; n++)
            if (otherNames[socketCount].equals(otherNames[n]))
              otherNames[socketCount] = otherNames[socketCount].concat("-2");

          if (otherNames[socketCount].equals(playersName))
            otherNames[socketCount] = otherNames[socketCount].concat("-2");

          out[socketCount].println(playersName);
          out[socketCount].println(fastgame);
          out[socketCount].println(seven);
          out[socketCount].println(nine);
          out[socketCount].println(swap);
          new WaitforMsg(socketCount);
          socketCount++;
        }
      } while (listen == true && socketCount < 3 && endlook != true);

      sh.addMsg("No longer listening for Game Connections");
      if (listenSocket != null)
        try {
          listenSocket.close();
        } catch (IOException e) {
          sh.addMsg("Error Closing Listen " + e);
        }
      deal();
    }
  }

  class AIThread implements Runnable {

    Thread ait; // AI Thread
    int implaying;
    int currentID;

    AIThread() {
      ait = new Thread(this, "AI");
      ait.start(); // Starting thread
      implaying = (int) whosturn;
      currentID = (int) gameID;
    }

    public void run() {

      try { // pause before move played
        Thread.sleep(aipause);
      } catch (Exception e) {
        sh.addMsg("AI thead sleep error " + e);
      }

      // Using gameAI to process move
      String command = ai.basicMove(hands[whosturn], pile, pile.size());

      // if gameAI generates valid move, process move
      if (!command.equals("error") && implaying == whosturn && currentID == gameID)
        processTurn(command, 4);

      if (implaying == whosturn && currentID == gameID) run();
    }
  }

  // shows scoreboard
  class ScoreThread implements Runnable {

    Thread st; // Score Thread

    ScoreThread() {
      st = new Thread(this, "Score");
      st.start(); // Starting thread
    }

    public void run() {
      score.display();
    }
  }
}
