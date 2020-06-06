import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

/**
 * Card Class
 *
 * @author Sam Cavenagh
 * @version 5/11/02
 *     <p>Website: http://home.pacific.net.au/~cavenagh/SH/ Email: cavenaghweb@hotmail.com
 */
public class Card {
  // Reference at bottom of class
  int cardSuit;
  int cardValue;
  int cardNumber;

  BufferedImage cardPic;
  Image cardSideWays;

  int picX;
  int picY;

  // for drawing card
  SHinterface sh;
  Graphics g;

  public Card(int cardNumber, Image cardspic, SHinterface sh, Graphics g) {
    this.cardNumber = cardNumber;
    this.sh = sh;
    this.g = g;

    cardPic = new BufferedImage(71, 96, Transparency.BITMASK);
    Graphics tempg = cardPic.getGraphics();

    cardSideWays = new BufferedImage(96, 71, Transparency.BITMASK);

    getCardDetails(cardspic, tempg);
  }

  public void drawCard(Point p) {
    g.drawImage(cardPic, (int) p.getX(), (int) p.getY(), sh);
  }

  public void drawCard2(int x, int y) {
    g.drawImage(cardPic, x, y, sh);
  }

  public void drawSideWays(Point p) {
    g.drawImage(cardSideWays, (int) p.getX(), (int) p.getY(), sh);
  }

  public void drawSideWays2(int x, int y) {
    g.drawImage(cardSideWays, x, y, sh);
  }

  public int getSuit() {
    return cardSuit;
  }

  public int getValue() {
    return cardValue;
  }

  public int getNumber() {
    return cardNumber;
  }

  /*--------------------------------
   *   Card Code Index
   *
   *   SUIT:
   *   1 = hearts
   *   2 = spades
   *   3 = diamonds
   *   4 = clubs
   *
   *   VALUE:
   *   2 - 10 = as numbers
   *   11 = jack
   *   12 = queen
   *   13 = king
   *   14 = ace
   *
   *-------------------------------*/

  private void getCardDetails(Image cardpic, Graphics tempg) {
    int cardWidth = 73;
    int cardHeight = 99;

    // Finding Suit
    if (cardNumber < 14) cardSuit = 1;
    else if (cardNumber >= 14 && cardNumber < 27) cardSuit = 2;
    else if (cardNumber >= 27 && cardNumber < 40) cardSuit = 3;
    else if (cardNumber >= 40 && cardNumber < 53) cardSuit = 4;

    // Finding Value
    if (cardSuit == 1) cardValue = cardNumber + 1;
    else if (cardSuit == 2) cardValue = cardNumber - 12;
    else if (cardSuit == 3) cardValue = cardNumber - 25;
    else if (cardSuit == 4) cardValue = cardNumber - 38;

    // seperating card image from other card images
    tempg.drawImage(cardpic, -(cardValue - 2) * cardWidth, -(cardSuit - 1) * cardHeight, sh);

    // rotating card image to create sideways card image
    Graphics tempg2 = cardSideWays.getGraphics();
    Graphics2D g2d = (Graphics2D) tempg2;
    AffineTransform origXform = g2d.getTransform();
    AffineTransform newXform = (AffineTransform) (origXform.clone());
    newXform.rotate(Math.toRadians(-90), 50, 50);
    g2d.setTransform(newXform);
    // draw image
    g2d.drawImage(cardPic, 29, 0, sh);
    g2d.setTransform(origXform);
  }

  public String getStringValue() {
    switch (cardValue) {
      case 2:
        return "Two";
      case 3:
        return "Three";
      case 4:
        return "Four";
      case 5:
        return "Five";
      case 6:
        return "Six";
      case 7:
        return "Seven";
      case 8:
        return "Eight";
      case 9:
        return "Nine";
      case 10:
        return "Ten";
      case 11:
        return "Jack";
      case 12:
        return "Queen";
      case 13:
        return "King";
      case 14:
        return "Ace";
      default:
        return "Unknown";
    }
  }

  public static String getCardStringValue(int cardNumber) {
    int cardSuit = 0;
    int cardValue = 0;

    // Finding Suit
    if (cardNumber < 14) cardSuit = 1;
    else if (cardNumber >= 14 && cardNumber < 27) cardSuit = 2;
    else if (cardNumber >= 27 && cardNumber < 40) cardSuit = 3;
    else if (cardNumber >= 40 && cardNumber < 53) cardSuit = 4;

    // Finding Value
    if (cardSuit == 1) cardValue = cardNumber + 1;
    else if (cardSuit == 2) cardValue = cardNumber - 12;
    else if (cardSuit == 3) cardValue = cardNumber - 25;
    else if (cardSuit == 4) cardValue = cardNumber - 38;

    switch (cardValue) {
      case 2:
        return "Two";
      case 3:
        return "Three";
      case 4:
        return "Four";
      case 5:
        return "Five";
      case 6:
        return "Six";
      case 7:
        return "Seven";
      case 8:
        return "Eight";
      case 9:
        return "Nine";
      case 10:
        return "Ten";
      case 11:
        return "Jack";
      case 12:
        return "Queen";
      case 13:
        return "King";
      case 14:
        return "Ace";
      default:
        return "Unknown";
    }
  }
}
