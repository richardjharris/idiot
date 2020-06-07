import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import javax.imageio.ImageIO;

/** Holds all images. */
public class ImageManager {
  private static final String cardsPath = "cards2/";
  private static final String cardBackFilenameNormal =  "back2.png";
  private static final String cardBackFilenameAlt =  "back2d.png";
  private static final String titleFilename = "SHtitle.jpg";
  private static final String pointerFilename = "pointer.gif";
  private static final String burntFilename = "burnt.jpg";

  // Percent chance of Dan card back
  private static final double DAN_CHANCE = 0.05;

  // Caches images loaded from the jar. Populated on demand.
  private HashMap<String, BufferedImage> cache = new HashMap<String, BufferedImage>();
 
  // Preloaded four directions of pointer image
  private BufferedImage pointer[] = new BufferedImage[4];

  private static final int baseCardWidth = 73;
  private static final int baseCardHeight = 99;

  private int cardWidth, cardHeight;
  private double scaleFactor;
  private String cardBackFilename;

  // Public methods

  ImageManager(SHinterface sh) throws IOException {
    cardWidth = sh.scale(baseCardWidth);
    cardHeight = sh.scale(baseCardHeight);
    scaleFactor = sh.scaleFactor;

    // Preload the rotated pointer images
    String prefix = "";
    for (int i = 0; i <= 3; i++) {
      pointer[i] = get(prefix + pointerFilename);
      prefix += "rotate$";
    }

    // Pick a random background image for this session
    cardBackFilename = pickCardBackFilename();
  }

  public BufferedImage getTitle() {
    double scale = 1 + (scaleFactor - 1) * .5;
    return get("scale" + scale + "$" + titleFilename);
  }

  public BufferedImage getBurnt() {
    return get("scale" + scaleFactor + "$" + burntFilename);
  }

  public BufferedImage getCardBack() {
    return get("resize" + cardWidth + "x" + cardHeight + "$" + cardBackFilename);
  }

  public BufferedImage getCardBackSideways() {
    return get("rotate$resize" + cardWidth + "x" + cardHeight + "$" + cardBackFilename);
  }

  BufferedImage getCardFront(int suit, int rank) {
    return get("resize" + cardWidth + "x" + cardHeight + "$" + getCardFilename(suit, rank));
  }

  BufferedImage getCardFrontSideways(int suit, int rank) {
    return get("rotate$resize" + cardWidth + "x" + cardHeight + "$" + getCardFilename(suit, rank));
  }

  /** Returns the pointer image, rotated for the specified player (0-3) */
  BufferedImage getPointer(int playerNo) {
    return pointer[playerNo];
  }

  // Private methods

  /**
   * Returns the image for the give cache key. The key is a filename within the
   * application JAR, but may be prefixed with transformations separated by $,
   * e.g. 'scale0.5$rotate$path/to/image.png'
   * 
   * Allowed transformations are:
   *  scale0.5 (scale factor)
   *  resize100x200 (width/height)
   *  rotate (clockwise 90)
   */
  private BufferedImage get(String key) {
    // Check cache first
    if (cache.containsKey(key)) {
      return cache.get(key);
    }

    BufferedImage dest = null;
    int dollar = key.indexOf('$');
    if (dollar != -1) {
      // Pop the transformation off
      String transform = key.substring(0, dollar);
      BufferedImage src = get(key.substring(dollar + 1));
      if (transform.startsWith("scale")) {
        double scaleBy = new Double(transform.substring(5));
        dest = scale(src, scaleBy);
      } else if (transform.startsWith("resize")) {
        String[] wh = transform.substring(6).split("x");
        dest = resize(src, new Integer(wh[0]), new Integer(wh[1]));
      } else if (transform.equals("rotate")) {
        dest = rotateClockwise90(src);
      }
    } else {
      // Raw filename
      URL url = this.getClass().getResource(key);
      if (url == null) {
        throw new Error("File '" + key + "' is missing or unreadable!");
      }
      try {
        dest = ImageIO.read(url);
      } catch(IOException e) {
        throw new Error("Failed to open file '" + url + "'");
      }
    }

    // Store in cache
    cache.put(key, dest);
    return dest;
  }

  private static String getCardFilename(int suit, int rank) {
    // SH code starts at 2 and ends at 14 (Ace)
    String rankString = Card.ranks[rank - 2];
    // SH code starts at 1 (Hearts), 2 (Spades), 3 (Diamonds), 4 (Clubs)
    String suitString = Card.suits[suit - 1];

    return cardsPath + "500px-" + rankString + suitString + ".svg.png";
  }

  // Returns a 'random' card back filename.
  private static String pickCardBackFilename() {
    return Math.random() >= (1.0 - DAN_CHANCE) ?
      cardBackFilenameAlt : cardBackFilenameNormal;
  }

  // Return a rotated copy of the image (clockwise 90 degrees)
  static BufferedImage rotateClockwise90(BufferedImage src) {
    int width = src.getWidth();
    int height = src.getHeight();
    // Color card images get given type = 0, which would cause an exception
    int type = src.getType() != 0 ? src.getType() : 6;

    BufferedImage dest = new BufferedImage(height, width, type);

    Graphics2D graphics2D = dest.createGraphics();
    graphics2D.translate((height - width) / 2.0, (height - width) / 2.0);
    graphics2D.rotate(Math.PI / 2.0, height / 2.0, width / 2.0);
    graphics2D.drawRenderedImage(src, null);
    graphics2D.dispose();

    return dest;
  }

  // Scale an image by the specified scale factor.
  static BufferedImage scale(BufferedImage src, double scaleFactor) {
    int newWidth = (int)(src.getWidth() * scaleFactor);
    int newHeight = (int)(src.getHeight() * scaleFactor);
    return resize(src, newWidth, newHeight);
  }

  // Resize an image to the specified width/height. Uses a special algorithm
  // optimised for down-scaling.
  static BufferedImage resize(BufferedImage src, int newWidth, int newHeight) {
    int type = src.getType() != 0 ? src.getType() : 6;
    BufferedImage dest = new BufferedImage(newWidth, newHeight, type);
    progressiveResize(src, dest);
    return dest;
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
  
  static private Graphics2D createGraphics(BufferedImage img) {
    Graphics2D g = img.createGraphics();
    g.setRenderingHint(
      RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		return g;  
  }

}
