/**
 * Represents the ordered pile of zero or more cards that appears in the middle
 * of the play area.
 * 
 * The pile may be added to, burnt (cleared) and inspected.
 * 
 * The pile has a maximum size of 52, as there are 52 cards. It currently uses
 * an array, but this implementation may change.
 */
public class CardPile {
  private static final int MAX = 52;

  // Hold the cards in the pile. The last card in the pile is followed by
  // a null (if <52) or the end of the array (if == 52).
  private Card pile[] = new Card[MAX];

  CardPile() {
    clear();
  }

  // Adds to the top of the pile
  public void add(Card card) {
    System.out.println("Adding " + card.toString() + " to the pile");
    for (int i = MAX - 1; i > 0; i--) pile[i] = pile[i - 1];
    pile[0] = card;
    System.out.println("Pile is now: " + toString());
  }

  // Returns the topmost item. Pile must not be empty.
  public Card top() {
    if (isEmpty()) throw new Error("top() called on empty pile");
    return pile[0];
  }

  // Convenience function. Returns the value of the topmost card
  public int topValue() {
    return top().getValue();
  }

  // Returns the card at offset N from the top (0 = top)
  public Card get(int offset) {
    return pile[offset];
  }

  // Exposes the raw card array. Be sure to check for null
  public Card[] rawData() {
    return pile;
  }

  // Clear the pile
  public void clear() {
    System.out.println("Clearing the pile");
    for (int n = 0; n < MAX; n++) {
      pile[n] = null;
    }
  }

  // Move the contents of the pile to the hand, then empty it.
  public void moveToHand(Hand hand) {
    System.out.println("Moving the pile to a hand");
    for (int n = 0; n < MAX; n++) {
      if (pile[n] == null) break;
      hand.addCard(pile[n]);
      pile[n] = null;
    }
  }

  public boolean isEmpty() {
    return pile[0] == null;
  }

  public boolean notEmpty() {
    return !isEmpty();
  }

  public int size() {
    int cardCount = 0;
    for (int n = 0; n < MAX; n++) {
      if (pile[n] == null) break;
      cardCount++;
    }
    return cardCount;
  }

  // Returns value of top card excluding 9s.
  // Returns -1 if the pile only contains 9s.
  public int topValueExcludingNines() {
    int count = topNinesCount();
    return pile[count] == null ? -1 : pile[count].getValue();
  }

  // Returns number of 9s on top of the pile, if any
  private int topNinesCount() {
    int count = 0;
    for (int i = 0; i < MAX; i++) {
      if (pile[i] == null) break;
      if (pile[i].getValue() == 9) count++;
      else break;
    }
    return count;
  }

  // Returns true if the pile is topped with a four of a kind
  // This is a common idiom in the SH code.
  public boolean isFourOfAKind() {
    return makesFourOfAKind(pile[3]);
  }

  // Returns true if the pile can make a four of a kind if combined with
  // the given card.
  public boolean makesFourOfAKind(Card card) {
    if (pile[0] == null || pile[1] == null || pile[2] == null || card == null) {
      // Pile is not large enough
      return false;
    }
    int top = top().getValue();
    if (pile[1].getValue() == top && pile[2].getValue() == top && card.getValue() == top) {
      return true;
    }
    return false;
  }

  public String toString() {
    String pileString = "";
    for (int i = 0; i <= MAX; i++) {
      if (pile[i] == null) break;
      pileString += " " + pile[i].toShortString();
    }
    return pileString;
  }
}