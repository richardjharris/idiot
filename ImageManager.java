import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.imageio.ImageIO;

/** Holds all images. */
public class ImageManager {
  private static final String cardsPath = "cards/";
  private static final String cardBackFilename = cardsPath + "blue_back.png";
  private static final String titleFilename = "SHtitle.jpg";
  private static final String pointerFilename = "pointer.gif";
  private static final String burntFilename = "burnt.jpg";

  private HashMap<String, BufferedImage> images;
  private BufferedImage pointer[] = new BufferedImage[4];
  private BufferedImage cardBackSideways;

  // These are in the same numbered order as the other SH code.
  // suits: 1-4
  private String[] suits = {"H", "S", "D", "C"};
  // ranks: 2-10, 11(J), 12(Q), 13(K), 14(A)
  private String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};

  ImageManager(Component parent) throws InterruptedException, IOException {
    MediaTracker tracker = new MediaTracker(parent);

    // List of files to load
    ArrayList<String> files = new ArrayList<String>();
    files.add(cardBackFilename);
    files.add(titleFilename);
    files.add(pointerFilename);
    files.add(burntFilename);

    for (String suit : suits) {
      for (String rank : ranks) {
        files.add(cardsPath + rank + suit + ".png");
      }
    }

    for (String file : files) {
      BufferedImage image = ImageIO.read(this.getClass().getResource(file));
      images.put(file, image);
      tracker.addImage(image, 1);
    }

    tracker.waitForAll();

    // Produce rotated pointer images
    pointer[0] = getImage(pointerFilename);
    pointer[3] = rotateClockwise90(pointer[0]);
    pointer[2] = rotateClockwise90(pointer[3]);
    pointer[1] = rotateClockwise90(pointer[2]);

    cardBackSideways = rotateClockwise90(getCardBack());
  }

  BufferedImage getTitle() {
    return getImage(titleFilename);
  }

  BufferedImage getBurnt() {
    return getImage(burntFilename);
  }

  BufferedImage getCardBack() {
    return getImage(cardBackFilename);
  }

  BufferedImage getCardBackSideways() {
    return cardBackSideways;
  }

  BufferedImage getCardFront(int suit, int rank) {
    // SH code starts at 2 and ends at 14 (Ace)
    String rankString = ranks[rank - 2];
    // SH code starts at 1 (Hearts), 2 (Spades), 3 (Diamonds), 4 (Clubs)
    String suitString = suits[suit - 1];

    return getImage(cardsPath + rankString + suitString + ".png");
  }

  BufferedImage getCardFrontSideways(int suit, int rank) {
    return rotateClockwise90(getCardFront(suit, rank));
  }

  /** Returns the pointer image, rotated for the specified player (0-3) */
  BufferedImage getPointer(int playerNo) {
    return pointer[playerNo];
  }

  private BufferedImage getImage(String filename) {
    return images.get(filename);
  }

  // Return a rotated copy of the image (clockwise 90 degrees)
  private BufferedImage rotateClockwise90(BufferedImage src) {
    int width = src.getWidth();
    int height = src.getHeight();

    BufferedImage dest = new BufferedImage(height, width, src.getType());

    Graphics2D graphics2D = dest.createGraphics();
    graphics2D.translate((height - width) / 2, (height - width) / 2);
    graphics2D.rotate(Math.PI / 2, height / 2, width / 2);
    graphics2D.drawRenderedImage(src, null);

    return dest;
  }
}
