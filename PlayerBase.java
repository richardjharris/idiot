import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;

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

  // Utilities
  protected boolean fourOfAKind(Card card) {
    if (pile[0] == null || pile[1] == null || pile[2] == null || card == null) return false;
    int top = pile[0].getValue();
    if (pile[1].getValue() == top && pile[2].getValue() == top && card.getValue() == top)
      return true;
    return false;
  }

  protected void addcardtopile(Card card) {
    // adding card to pile
    for (int i = 51; i > 0; i--) pile[i] = pile[i - 1];
    pile[0] = card;
  }

  protected int pilelength() {
    int cardCount = 0;
    for (int n = 0; n < 52; n++) {
      if (pile[n] == null) break;
      cardCount++;
    }
    return cardCount;
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
    ImageManager im = sh.getImageManager();

    g.setColor(Color.red);
    drawRoundRect(355, 5, 90, 40, 15, 15);
    g.setColor(Color.white);
    drawString("Deck: " + deckLength(), 365, 20);
    drawString("Pile: " + pilelength(), 365, 40);

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