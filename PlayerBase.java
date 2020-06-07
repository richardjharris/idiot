import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Vector;

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

  CardPile pile = new CardPile();
  // Deck + hands are managed by the Dealer only; the Player sees counts.

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

  // Clear pile and schedule BURNT graphic.
  protected void burnPile() {
    pile.clear();
    burnt = true;
  }

  // Indicates if the given card value is allowed to go on top of the pile.
  protected boolean canAddToPile(int value) {
    if (pile.isEmpty()) {
      // If pile is empty, any card is valid
      return true;
    }

    if ((nine && value == 9) || value == 10 || value == 2) {
      // These cards can go onto anything
      return true;
    }

    int topValue = nine ? pile.topValueExcludingNines() : pile.topValue();
    if (topValue == -1) {
      // nine=true and pile only consists of 9s
      return true;
    }

    if (seven && topValue == 7 && value >= 7) return false;
    if (value < topValue) return false;

    return true;
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

    if (pile.notEmpty()) {
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
    drawString("Pile: " + pile.size(), 365, 40);

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
          sh.drawCard(faceUpCard, tableposition[player][card], sideways);
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

  protected void drawPileOld() {
    // HACK as I don't feel like rewriting this yet.
    // TODO scale the offsets?
    Card[] pile = this.pile.rawData();

    // determining how many cards of the same value are ontop of each other
    int top = 0;
    if (nine && pile[0].getValue() == 9) {
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
      if (pile[top] != null) sh.drawCard(pile[top], centre1);
    } else if (samecount == 2) { // 2 of a kind
      sh.drawCard(pile[top + 1], (int) centre1.getX(), (int) centre1.getY() - 10);
      sh.drawCard(pile[top], (int) centre1.getX(), (int) centre1.getY() + 10);
    } else if (samecount >= 3) { // 3 of a kind
      sh.drawCard(pile[top + 2], (int) centre1.getX(), (int) centre1.getY() - 20);
      sh.drawCard(pile[top + 1], (int) centre1.getX(), (int) centre1.getY());
      sh.drawCard(pile[top], (int) centre1.getX(), (int) centre1.getY() + 20);
    }
    if (nine && pile[0].getValue() == 9)
      if (top == 1) // one nine
      sh.drawSideways(pile[0], (int) centre1.getX() - 15, (int) centre1.getY() + 40);
    if (top == 2) { // 2 nines
      sh.drawSideways(pile[1], (int) centre1.getX() - 15, (int) centre1.getY() + 40);
      sh.drawSideways(pile[0], (int) centre1.getX() - 15, (int) centre1.getY() + 50);
    }
    if (top == 3) { // 3 nines
      sh.drawSideways(pile[2], (int) centre1.getX() - 15, (int) centre1.getY() + 40);
      sh.drawSideways(pile[1], (int) centre1.getX() - 15, (int) centre1.getY() + 50);
      sh.drawSideways(pile[0], (int) centre1.getX() - 15, (int) centre1.getY() + 60);
    }
  }

  // New version of drawPile. Draws the ENTIRE pile.
  protected void drawPile() {
    Card[] pile = this.pile.rawData();

    // Convert the pile into stacks. Stacks of 9s will be rendered
    // sideways.
    int stackCount = 0;
    int prev = -1;
    for (int i = 0; i < 52; i++) {
      if (pile[i] == null) break;
      if (prev != pile[i].getValue()) {
        prev = pile[i].getValue();
        stackCount++;
      }
    }

    Point centre = new Point(
      (int)(centre1.x - stackCount * 0.5),
      (int)(centre1.y - stackCount * 0.5)
    );

    double offsetDelta = stackCount >= 10 ? 2.0 : 3.0;

    // Start rendering the pile in reverse.
    Vector<Card> stack = new Vector<Card>();
    for (int i = 51; i >= 0; i--) {
      if (pile[i] == null) continue;
      // Consider 9s part of the same stack if applicable
      if (!stack.isEmpty() && stack.get(0).getValue() != pile[i].getValue()
        && (!nine || pile[i].getValue() != 9)) {
        // Start a new stack
        drawCardStack(stack, centre);
        centre.x += offsetDelta;
        centre.y += offsetDelta;
        stack = new Vector<Card>();
      }
      stack.add(pile[i]);
    }
    if (!stack.isEmpty()) {
      drawCardStack(stack, centre);
    }
  }

  // Helper method for drawPile
  private void drawCardStack(Vector<Card> stack, Point centre) {
    // Fan cards out from centre (maybe subject to scaling)
    // 20 pixels per card
    int spacing = stack.size() == 2 ? 10 : 20;
    int verticalSize = stack.size() * spacing;
    Point c = new Point(centre.x, (int)(centre.y - verticalSize / 2));

    for (Card card: stack) {
      boolean sideways = nine && card.getValue() == 9;
      Point c2 = (Point)c.clone();
      if (sideways) {
        c2.x -= 15;
        c2.y += 30;
      }
      sh.drawCard(card, c2, sideways);
      c.y += spacing;
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