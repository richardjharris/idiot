import java.awt.*;
import java.awt.image.*;

/**
 * Card Class
 *
 * @author Sam Cavenagh
 * @version 5/11/02
 *     <p>Website: http://home.pacific.net.au/~cavenagh/SH/ Email: cavenaghweb@hotmail.com
 */
public class Card {
  // Suit numbered 1-4, in the same order as `suits` below.
  int cardSuit;
  // Rank numbered 2-14, in the same order as `ranks` below (11=J, 12=Q, 13=K, 14=A)
  int cardValue;

  // Original card number from 0-51.
  int cardNumber;

  // TODO move routines to SHinterface.
  SHinterface sh;
  Graphics g;

  public static final String[] suits = {"H", "S", "D", "C"};
  public static final String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};

  public static final String[] suitsLong = {"Hearts", "Spades", "Diamonds", "Clubs"};
  public static final String[] ranksLong = {"Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Jack", "Queen", "King", "Ace"};

  public Card(int cardNumber, SHinterface sh, Graphics g) {
    this.cardNumber = cardNumber;
    this.sh = sh;
    this.g = g;

    if (cardNumber < 1 || cardNumber > 52) {
      throw new Error("Invalid card number: " + cardNumber);
    }

    cardSuit = getCardSuit(cardNumber);
    cardValue = getCardValue(cardNumber, cardSuit);
  }

  private BufferedImage cardPic() {
    return sh.getImageManager().getCardFront(cardSuit, cardValue);
  }

  private BufferedImage cardSideways() {
    return sh.getImageManager().getCardFrontSideways(cardSuit, cardValue);
  }

  public void drawCard(Point p, boolean sideways) {
    if (sideways) drawSideways(p);
    else drawCard(p);
  }

  public void drawCard(int x, int y, boolean sideways) {
    if (sideways) drawSideways(x, y);
    else drawCard(x, y);
  }

  public void drawCard(Point p) {
    g.drawImage(cardPic(), (int) p.getX(), (int) p.getY(), sh);
  }

  public void drawCard(int x, int y) {
    g.drawImage(cardPic(), x, y, sh);
  }

  public void drawSideways(Point p) {
    g.drawImage(cardSideways(), (int) p.getX(), (int) p.getY(), sh);
  }

  public void drawSideways(int x, int y) {
    g.drawImage(cardSideways(), x, y, sh);
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

  public String getStringSuit() {
    return suitsLong[cardSuit - 1];
  }

  public String getStringValue() {
    return ranksLong[cardValue - 2];
  }

  public String getShortStringSuit() {
    return suits[cardSuit - 1];
  }

  public String getShortStringValue() {
    return ranks[cardValue - 2];
  }

  // Long name of card, e.g. Two of Hearts
  public String toString() {
    return getStringValue() + " of " + getStringSuit();
  }

  // Short name of card, e.g. AH or 6D
  public String toShortString() {
    return getShortStringValue() + getShortStringSuit();
  }

  public static int getCardSuit(int cardNumber) {
    int cardSuit = 0;

    // Finding Suit
    if (cardNumber < 14) cardSuit = 1;
    else if (cardNumber >= 14 && cardNumber < 27) cardSuit = 2;
    else if (cardNumber >= 27 && cardNumber < 40) cardSuit = 3;
    else if (cardNumber >= 40 && cardNumber < 53) cardSuit = 4;
    return cardSuit;
  }

  public static int getCardValue(int cardNumber, int cardSuit) {
    int cardValue = 0;

    // Finding Value
    if (cardSuit == 1) cardValue = cardNumber + 1;
    else if (cardSuit == 2) cardValue = cardNumber - 12;
    else if (cardSuit == 3) cardValue = cardNumber - 25;
    else if (cardSuit == 4) cardValue = cardNumber - 38;
    return cardValue;
  }

  public static String getCardStringValue(int cardNumber) {
    int cardSuit = getCardSuit(cardNumber);
    int cardValue = getCardValue(cardNumber, cardSuit);

    // TODO duplication
    return ranksLong[cardValue - 2];
  }
}
