import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;

/**
 * Class that holds code common to both the Dealer (game host) and Player (other
 * player) classes.
 * 
 * This is intended to be a first step towards refactoring
 */
abstract public class PlayerBase {
  // used for testing makes deck empty at start of game if true
  boolean fastgame = false;

  // you must play less than a seven if true
  boolean seven = false;

  // if nine is invisible
  boolean nine = false;

  // if can swap cards at start of game
  boolean swap = false;

  // controls display of 'Burnt' graphic
  boolean burnt = false;

  // our name
  String playersName;

  Graphics g;
  SHinterface sh;

  // cards in play pile: up to 52, terminated by 'null'
  Card pile[] = new Card[52];
  // deck, hands. etc are managed by the dealer only

  // scoreboard
  Score score;
  // position in scoreboard
  int position = 1;

  // point where pile cards are displayed
  Point centre1;

  // Details of other players
  String otherNames[] = new String[3];
  boolean outofgame[] = { false, false, false, false };
  int whosturn;

  // For drawing the screen
  Point tableposition[][] = new Point[3][3];
  Point pointerpoints[] = new Point[4];

  // Is thread listening for message
  boolean listen = true;

  PlayerBase(SHinterface sh, Graphics g, Score score) {
    this.sh = sh;
    this.g = g;
    this.score = score;

    initTablePositions();
  }

  // Represents the player's hand. Player only cares about their own hand,
  // while Dealers keep track of all of them
  abstract protected Hand ownHand();

  abstract protected int deckLength();

  abstract protected int handLength(int playerNo);

  // Get the face up card for the given player
  abstract protected Card getFaceUpCard(int playerNo, int index);

  // Returns true if the given face-down card still remains
  abstract protected boolean hasFaceDownCard(int playerNo, int index);

  protected ImageManager getImageManager() {
    return sh.getImageManager();
  }

  // Utilities
  protected boolean fourOfAKind(Card card) {
    if (pile[0] == null || pile[1] == null || pile[2] == null || card == null)
      return false;
    int top = pile[0].getValue();
    if (pile[1].getValue() == top && pile[2].getValue() == top && card.getValue() == top)
      return true;
    return false;
  }

  protected int pileSize() {
    int cardCount = 0;
    for (int n = 0; n < 52; n++) {
      if (pile[n] == null) break;
      cardCount++;
    }
    return cardCount;
  }

  protected boolean pileIsEmpty() {
    return pile[0] == null;
  }

  protected void burnPile() {
    System.out.println("Burning the pile");
    for (int n = 0; n < 52; n++) {
      pile[n] = null;
    }
    burnt = true;
  }

  // Adds to the end of the pile
  protected void addToPile(Card card) {
    System.out.println("Adding " + card.toString() + " to the pile");
    for (int i = 51; i > 0; i--) pile[i] = pile[i - 1];
    pile[0] = card;
  }

  private void initTablePositions() {
    // Initialise table positions (unscaled co-ords)
    tableposition[0][0] = sh.point(0, 103);
    tableposition[0][1] = sh.point(0, 188);
    tableposition[0][2] = sh.point(0, 276);

    tableposition[1][0] = sh.point(103, 0);
    tableposition[1][1] = sh.point(188, 0);
    tableposition[1][2] = sh.point(276, 0);

    tableposition[2][0] = sh.point(354, 103);
    tableposition[2][1] = sh.point(354, 188);
    tableposition[2][2] = sh.point(354, 276);

    centre1 = sh.point(188, 175);

    pointerpoints[0] = sh.point(115, 220);
    pointerpoints[1] = sh.point(220, 110);
    pointerpoints[2] = sh.point(330, 220);
    pointerpoints[3] = sh.point(220, 330);
  }

  public void displayTable() {
    drawPlayAreaBackground();
    drawCornerBoxes();
    ownHand().showHand();

    drawOtherPlayerCards();

    String pileStr = "";
    for (int i = 0; i < 52; i++) {
      if (pile[i] == null) break;
      pileStr += Card.getCardStringValue(pile[i].cardNumber);
    }
    System.out.println("Pile: " + pileStr);

    if (!pileIsEmpty()) {
      drawPile();
    } else if (burnt) {
      drawBurnt();
      burnt = false;
    }

    drawTurnMarker();

    sh.repaint();
  }

  // Clear play area
  // Always call setColor before any drawing routines. If you want to change
  // anything else in Graphics, make a clone.
  protected void drawPlayAreaBackground() {
    g.setColor(Color.black);
    fillRect(0, 0, 450, 550);
    g.setColor(Color.white);
    drawLine(0, 450, 450, 450);
  }

  // Draw deck count, player name and cards
  protected void drawCornerBoxes() {
    ImageManager im = getImageManager();

    g.setColor(Color.red);
    drawRoundRect(355, 5, 90, 40, 15, 15);
    g.setColor(Color.white);
    drawString("Deck: " + deckLength(), 365, 20);
    drawString("Pile: " + pileSize(), 365, 40);

    drawRoundRect(5, 360, 90, 40, 15, 15);
    drawString("Name: " + otherNames[0], 10, 375);
    drawString("Cards: " + handLength(0), 10, 395);
    drawImage(im.getPointer(1), 68, 380);

    drawRoundRect(5, 5, 90, 40, 15, 15);
    drawString("Name: " + otherNames[1], 10, 20);
    drawString("Cards: " + handLength(1), 10, 40);
    drawImage(im.getPointer(2), 70, 25);

    drawRoundRect(355, 360, 90, 40, 15, 15);
    drawString("Name: " + otherNames[2], 360, 375);
    drawString("Cards: " + handLength(2), 360, 395);
    drawImage(im.getPointer(1), 423, 380);
  }

  // Draw cards for other players
  protected void drawOtherPlayerCards() {
    BufferedImage back = getImageManager().getCardBack();
    BufferedImage backSW = getImageManager().getCardBackSideways();

    // Draw cards in central pool
    for (int player = 0; player <= 2; player++) {
      // Top player (1) is normal, others are sideways
      boolean sideways = player != 1;
      for (int card = 0; card <= 2; card++) {
        Card faceUpCard = getFaceUpCard(player, card);
        if (faceUpCard != null) {
          faceUpCard.drawCard(tableposition[player][card], sideways);
        } else if (hasFaceDownCard(player, card)) {
          g.drawImage(sideways ? backSW : back, (int) tableposition[player][card].getX(), (int) tableposition[player][card].getY(), sh);
        }
      }
    }
  }

  protected void drawTurnMarker() {
    g.drawImage(
        sh.getImageManager().getPointer(whosturn),
        (int) pointerpoints[whosturn].getX(),
        (int) pointerpoints[whosturn].getY(),
        sh);
  }

  protected void drawBurnt() {
    BufferedImage burnBang = sh.getImageManager().getBurnt();
    // Draw in centre of play area
    Point xy = sh.getCoordsForCentredImage(burnBang);
    g.drawImage(burnBang, xy.x, xy.y, sh);
  }

  protected void drawPile() {
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
  }

  // Scaling routines
  protected final Point scalePoint(Point p) {
    return new Point(sh.scale(p.x), sh.scale(p.y));
  }

  // Scaled drawing routines
  protected final void drawRoundRect(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
    g.drawRoundRect(
        sh.scale(arg0),
        sh.scale(arg1),
        sh.scale(arg2),
        sh.scale(arg3),
        sh.scale(arg4),
        sh.scale(arg5));
  }

  protected final void drawLine(int arg0, int arg1, int arg2, int arg3) {
    g.drawLine(sh.scale(arg0), sh.scale(arg1), sh.scale(arg2), sh.scale(arg3));
  }

  protected final void fillRect(int arg0, int arg1, int arg2, int arg3) {
    g.fillRect(sh.scale(arg0), sh.scale(arg1), sh.scale(arg2), sh.scale(arg3));
  }

  protected final void drawString(String s, int x, int y) {
    g.drawString(s, sh.scale(x), sh.scale(y));
  }

  protected final void drawImage(Image i, int x, int y) {
    g.drawImage(i, sh.scale(x), sh.scale(y), sh);
  }
}