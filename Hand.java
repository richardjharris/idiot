import java.awt.*;

/**
 * Contains cards in players hand
 *
 * @author Sam Cavenagh
 * @version 12/11/02
 *     <p>Website: http://home.pacific.net.au/~cavenagh/SH/ Email: cavenaghweb@hotmail.com
 */
public class Hand {
  Card hand[] = new Card[52];
  Card facedown[] = new Card[3];
  Card faceup[] = new Card[3];

  Point ontable[] = new Point[3];

  // click detection box for cards in hand
  Rectangle cardBox[] = new Rectangle[52];

  // click detection box for cards on table
  Rectangle tableBox[] = new Rectangle[3];

  SHinterface sh;
  Image back;
  Graphics g;

  public Hand(SHinterface sh, Image back, Graphics g) {
    this.sh = sh;
    this.back = back;
    this.g = g;

    ontable[0] = sh.point(103, 350);
    ontable[1] = sh.point(188, 350);
    ontable[2] = sh.point(276, 350);

    tableBox[0] = sh.rect(103, 350, 71, 96);
    tableBox[1] = sh.rect(188, 350, 71, 96);
    tableBox[2] = sh.rect(276, 350, 71, 96);
 }

  public Hand() {}

  // is there any cards facing up ?
  public boolean isFaceUp() {
    if (faceup[0] != null || faceup[1] != null || faceup[2] != null) return true;
    return false;
  }

  // is there any cards facing down ? or only 1 card facing down
  public boolean isFaceDown() {
    if (facedown[0] == null && facedown[1] == null && facedown[2] == null) return false;
    if (facedown[1] == null && facedown[2] == null) return false;
    if (facedown[0] == null && facedown[2] == null) return false;
    if (facedown[0] == null && facedown[1] == null) return false;
    return true;
  }

  public void swap(Card[] inhand, Card[] ontable) {
    for (int n = 0; n < 3; n++) {
      hand[n] = inhand[n];
      faceup[n] = ontable[n];
    }
  }

  public int mouseClick(int mouseX, int mouseY) {
    // counting number of cards in hand
    int cardCount = length();
    if (cardCount == 1) {
      for (int n = 0; n < 3; n++)
        if (tableBox[n].contains(mouseX, mouseY)) if (selectionValid(n)) return n;
    }
    // Checking for click inside card
    for (int n = 0; n < cardCount - 1; n++)
      if (cardBox[n].contains(mouseX, mouseY)) {
        return n;
      }
    return -1;
  }

  public boolean selectionValid(int selection) {
    if (isFaceUp()) {
      if (faceup[selection] == null) return false;
    } else {
      if (facedown[selection] == null) return false;
    }
    return true;
  }

  // Draw Cards in hand on screen
  public void showHand() {
    // counting number of cards in hand
    int cardCount = length();

    int xfactor;
    if (cardCount <= 20) xfactor = 450 / cardCount;
    else xfactor = 450 / 21;
    int shiftfactor = 38;
    if (xfactor - shiftfactor < 0) shiftfactor = xfactor;

    // Drawing the cards
    for (int n = 0; n < 52; n++) {
      if (hand[n] == null) break;

      int x = (int) (xfactor * (n + 1) - shiftfactor);
      int y = 453;

      if (n > 20) {
        x = (int) (xfactor * (n - 20) - shiftfactor);
        y = 485;
      }

      if (n > 41) {
        x = (int) (xfactor * (n - 41) - shiftfactor);
        y = 515;
      }

      hand[n].drawCard2(sh.scale(x), sh.scale(y));

      // creating cardBox

      // setting card height
      int height = 96; // default height
      if (cardCount > 22) { // if more than 22 card there must be an over lay
        // cardCount = total number of cards
        // n = where this card is in the hand, ie 0 is first
        if (n < 21 && cardCount - n - 20 > 0) height = 32;
        if (cardCount > 43 && n < 42) // if more than 43 card must over lay 3 times
        height = 30;
      }

      int n2 = n;

      if (n2 > 21) n2 = 22;
      int width = (xfactor * (n2 + 2) - shiftfactor) - x;
      if (n > 20) width = (int) (xfactor * (n - 19) - shiftfactor) - x;
      if (n > 41) width = (int) (xfactor * (n - 40) - shiftfactor) - x;

      if (width > 71) width = 71;
      if (n + 2 == cardCount || n == 21 || n == 42) width = 71;

      cardBox[n] = new Rectangle(sh.scale(x), sh.scale(y), sh.scale(width), sh.scale(height));
    }

    for (int n = 0; n < 3; n++)
      if (faceup[n] != null) faceup[n].drawCard(ontable[n]);
      else if (facedown[n] != null)
        g.drawImage(back, (int) ontable[n].getX(), (int) ontable[n].getY(), sh);
  }

  // Add cards during dealing, this adds cards to facedown then faceup then hand
  public void deal(Card card) {
    boolean added = false;
    for (int n = 0; n < 3; n++)
      if (facedown[n] == null) {
        facedown[n] = card;
        added = true;
        break;
      }
    if (!added)
      for (int n = 0; n < 3; n++)
        if (faceup[n] == null) {
          faceup[n] = card;
          added = true;
          break;
        }
    if (!added) addCard(card);
  }

  // Adds card to hand, card is added to correct position
  public void addCard(Card card) {
    for (int n = 0; n < hand.length; n++) {
      if (hand[n] == null) {
        hand[n] = card;
        break;
      } else if (hand[n].getValue() >= card.getValue()) {
        Card temp = hand[n];
        hand[n] = card;
        for (int s = length(); s >= n + 1; s--) if (s + 1 < 52) hand[s + 1] = hand[s];
        hand[n + 1] = temp;
        break;
      }
    }
  }

  // Removing card from hand then reordering hand
  public void removeCard(int cardno) {
    for (int n = cardno; n < length(); n++) {
      if (hand[n] == null) break;
      if (n + 1 < 52) hand[n] = hand[n + 1];
    }
    hand[length() - 1] = null;
  }

  public void removeAll() {
    for (int n = 0; n < 3; n++) {
      faceup[n] = null;
      facedown[n] = null;
    }
    for (int n = 0; n < 52; n++) hand[n] = null;
  }

  public Card getCard(int cardno) {
    return hand[cardno];
  }

  public Card getFaceUp(int cardno) {
    return faceup[cardno];
  }

  public void removeFaceUp(int cardno) {
    faceup[cardno] = null;
  }

  public void removeFaceDown(int cardno) {
    facedown[cardno] = null;
  }

  public Card getFaceDown(int cardno) {
    return facedown[cardno];
  }

  public int length() {
    int cardCount = 1;
    for (int n = 0; n < 52; n++) {
      if (hand[n] == null) break;
      cardCount++;
    }
    return cardCount;
  }
}
