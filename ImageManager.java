import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.imageio.ImageIO;

/** Holds all images. */
// TODO cache resized images
public class ImageManager {
  private static final String cardsPath = "cards/";
  private static final String cardBackFilename = cardsPath + "blue_back.png";
  private static final String titleFilename = "SHtitle.jpg";
  private static final String pointerFilename = "pointer.gif";
  private static final String burntFilename = "burnt.jpg";

  private HashMap<String, BufferedImage> images;
  private BufferedImage pointer[] = new BufferedImage[4];
  private BufferedImage cardBackSideways;

  private static final int baseCardWidth = 73;
  private static final int baseCardHeight = 99;

  private int cardWidth, cardHeight;

  // These are in the same numbered order as the other SH code.
  // suits: 1-4
  private String[] suits = {"H", "S", "D", "C"};
  // ranks: 2-10, 11(J), 12(Q), 13(K), 14(A)
  private String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};

  ImageManager(SHinterface sh) throws InterruptedException, IOException {
    MediaTracker tracker = new MediaTracker(sh);

    cardWidth = sh.scale(baseCardWidth);
    cardHeight = sh.scale(baseCardHeight);

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

    images = new HashMap<String, BufferedImage>();
    for (String file : files) {
      BufferedImage image = ImageIO.read(this.getClass().getResource(file));
      images.put(file, image);
      tracker.addImage(image, 1);
    }

    tracker.waitForAll();

    // Produce rotated pointer images
    pointer[0] = getImage(pointerFilename);
    for (int i = 1; i <= 3; i++) {
      pointer[i] = rotateClockwise90(pointer[i - 1]);
    }

    cardBackSideways = rotateClockwise90(getCardBack());
  }

  BufferedImage getTitle() {
    return getImage(titleFilename);
  }

  BufferedImage getBurnt() {
    return getImage(burntFilename);
  }

  BufferedImage getCardBack() {
    return resize(getImage(cardBackFilename), cardWidth, cardHeight);
  }

  BufferedImage getCardBackSideways() {
    return resize(cardBackSideways, cardHeight, cardWidth);
  }

  private BufferedImage getRawCardFront(int suit, int rank) {
    // SH code starts at 2 and ends at 14 (Ace)
    String rankString = ranks[rank - 2];
    // SH code starts at 1 (Hearts), 2 (Spades), 3 (Diamonds), 4 (Clubs)
    String suitString = suits[suit - 1];

    return getImage(cardsPath + rankString + suitString + ".png");
  }

  BufferedImage getCardFront(int suit, int rank) {
    BufferedImage image = getRawCardFront(suit, rank);
    return resize(image, cardWidth, cardHeight);
  }

  BufferedImage getCardFrontSideways(int suit, int rank) {
    return resize(rotateClockwise90(getRawCardFront(suit, rank)), cardHeight, cardWidth);
  }

  /** Returns the pointer image, rotated for the specified player (0-3) */
  BufferedImage getPointer(int playerNo) {
    return pointer[playerNo];
  }

  private BufferedImage getImage(String filename) {
    return images.get(filename);
  }

  // Return a rotated copy of the image (clockwise 90 degrees)
  static BufferedImage rotateClockwise90(BufferedImage src) {
    int width = src.getWidth();
    int height = src.getHeight();

    BufferedImage dest = new BufferedImage(height, width, src.getType());

    Graphics2D graphics2D = dest.createGraphics();
    graphics2D.translate((height - width) / 2, (height - width) / 2);
    graphics2D.rotate(Math.PI / 2, height / 2, width / 2);
    graphics2D.drawRenderedImage(src, null);
    graphics2D.dispose();

    return dest;
  }

  static BufferedImage resize(BufferedImage src, int newWidth, int newHeight) {
    BufferedImage dest = new BufferedImage(newWidth, newHeight, src.getType());
    progressiveResize(src, dest);
    return dest;
  }

  static private Graphics2D createGraphics(BufferedImage img) {
    Graphics2D g = img.createGraphics();
    g.setRenderingHint(
      RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		return g;  
  }

	// Progressive resizer, stolen from Thumbnailator library.
	static public void progressiveResize(BufferedImage srcImage, BufferedImage destImage)
			throws NullPointerException
	{		
		int currentWidth = srcImage.getWidth();
		int currentHeight = srcImage.getHeight();
		
		final int targetWidth = destImage.getWidth();
		final int targetHeight = destImage.getHeight();
		
		// If multi-step downscaling is not required, perform one-step.
		if ((targetWidth * 2 >= currentWidth) && (targetHeight * 2 >= currentHeight))
		{
			Graphics2D g = createGraphics(destImage);
			g.drawImage(srcImage, 0, 0, targetWidth, targetHeight, null);
			g.dispose();
			return;
		}
		
		// Temporary image used for in-place resizing of image.
		BufferedImage tempImage = new BufferedImage(
				currentWidth,
				currentHeight,
				destImage.getType()
		);
		
		Graphics2D g = createGraphics(tempImage);
		g.setComposite(AlphaComposite.Src);
		
		/*
		 * Determine the size of the first resize step should be.
		 * 1) Beginning from the target size
		 * 2) Increase each dimension by 2
		 * 3) Until reaching the original size
		 */
		int startWidth = targetWidth;
		int startHeight = targetHeight;
		
		while (startWidth < currentWidth && startHeight < currentHeight)
		{
			startWidth *= 2;
			startHeight *= 2;
		}
		
		currentWidth = startWidth / 2;
		currentHeight = startHeight / 2;

		// Perform first resize step.
		g.drawImage(srcImage, 0, 0, currentWidth, currentHeight, null);
		
		// Perform an in-place progressive bilinear resize.
		while (	(currentWidth >= targetWidth * 2) && (currentHeight >= targetHeight * 2) )
		{
			currentWidth /= 2;
			currentHeight /= 2;
			
			if (currentWidth < targetWidth)
			{
				currentWidth = targetWidth;
			}
			if (currentHeight < targetHeight)
			{
				currentHeight = targetHeight;
			}
			
			g.drawImage(
					tempImage,
					0, 0, currentWidth, currentHeight,
					0, 0, currentWidth * 2, currentHeight * 2,
					null
			);
		}
		
		g.dispose();
		
		// Draw the resized image onto the destination image.
		Graphics2D destg = createGraphics(destImage);
		destg.drawImage(tempImage, 0, 0, targetWidth, targetHeight, 0, 0, currentWidth, currentHeight, null);
		destg.dispose();
	}
}
