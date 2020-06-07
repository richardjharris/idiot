import java.awt.*;
import java.awt.Color;
import java.awt.event.*;
import java.awt.image.*;
import java.io.IOException;
import javax.swing.*;

// TODO(release) Dialogs (e.g. MultiCardD) should appear in the center
// TODO Set scaling based on resolution
// TODO graphical way of indicating hand sizes
// TODO random card backs
// TODO fix rightmost hand position
// TODO right-click (or middle) to send all cards

/*-------------------------------
 This is a Shithead(card game) Program

 @Author Sam Cavenagh

 @Date 1/11/02

Overview:
I develop this program to extend my java skills and to implement something that to the
best of my knowledge had not been done before.  I spend a good few months of my life
working on this project and have implemented most of the functionality that I had foreseen
in my initial plans.  However, Shithead is a very adaptable game and many different versions
of it exist.  I have received several emails requesting source code or changes to the
program.  I myself feel I have spent enough time on this project and its time to move onto
other work (such as uni).  Thus I am making the source code available to all who are brave
enough to attempt to improve this program!!

Important Note:
I will be the first to admit that the code is not in the best form and large aspect of the
program need to be re-architectured, however I do not have time to do this myself but if
someone else wants to do it great.  I only have a few requests listed below.

1.	Any new versions of the program I would like to remain open source
2.	Improved versions are emailed to me so that can be added to my site for Shithead player
    that don't have the time or skill to implement changes themselves
3.	I'm still given some form of credit for the program's creation (even if only as a footnote)

Website: http://home.pacific.net.au/~cavenagh/SH/

Email: cavenaghweb@hotmail.com

 -------------------------------*/

class SHinterface extends JFrame
    implements ActionListener, MouseMotionListener, MouseListener, WindowListener {

  private static final long serialVersionUID = 1L;

  JLabel image;
  JTextArea msg;
  JPanel panel;
  JTextField input;
  JScrollPane scrollPane;

  int mouseX, mouseY;

  JMenuBar menuBar;

  // Graphics buffer stuff
  Graphics g;
  BufferedImage offscreen;
  ImageIcon imageI;

  // Scale factor for all drawing and input routines
  double scaleFactor = 1.5;

  boolean player1 = false; // is 1 player game running
  boolean player2 = false; // is 2 player game running

  boolean myTurn = false; // is it my turn

  boolean imServer = false;

  ServerMessage servermsg = null;
  Message message = null;
  Dealer dealer = null;
  Player player = null;
  Score score;

  String playersName = "unknown";

  Hand hand;

  // Points where to place cards
  Point pointplayer1[] = new Point[3];
  Point pointplayer2[] = new Point[3];
  Point pointplayer3[] = new Point[3];
  Point pointplayer4[] = new Point[3];

  Point centre1;

  ImageManager imageManager;
  SoundManager soundManager;

  int scale(int number) {
    return (int) (number * scaleFactor);
  }

  Point point(int x, int y) {
    return new Point(scale(x), scale(y));
  }

  Rectangle rect(int arg0, int arg1, int arg2, int arg3) {
    return new Rectangle(scale(arg0), scale(arg1), scale(arg2), scale(arg3));
  }

  Point getCoordsForCentredImage(BufferedImage img) {
    // Play area is a 450x450 square, with Hand area below it.
    int canvasSize = scale(450);

    int x = (int)(((double)canvasSize / 2) - ((double)img.getWidth() / 2));
    int y = (int)(((double)canvasSize / 2) - ((double)img.getHeight() / 2));

    return new Point(x,y);
  }

  void drawTitle() {
    g.setColor(Color.black);
    g.fillRect(0, 0, scale(450), scale(550));

    BufferedImage title = imageManager.getTitle();

    Point xy = getCoordsForCentredImage(title);
    g.drawImage(title, xy.x, xy.y, this);
  }

  SHinterface() {
    try {
      imageManager = new ImageManager(this);
    } catch (IOException e) {
      msg.setText("Load Error " + e);
    }

    offscreen = new BufferedImage(scale(450), scale(550), BufferedImage.TYPE_3BYTE_BGR);
    g = offscreen.getGraphics();
    drawTitle();
    g.setColor(Color.white);
    g.drawLine(0, scale(450), scale(450), scale(450));

    hand = new Hand(this, imageManager.getCardBack(), g);

    imageI = new ImageIcon(offscreen);

    image = new JLabel(imageI);

    addMouseMotionListener(this);
    addMouseListener(this);
    requestFocus();

    // Construction Menu
    menuBar = buildMenuBar();

    addWindowListener(this);

    msg = new JTextArea("Welcome to Shithead the card game\n", 4, 20);
    msg.setLineWrap(true);
    msg.setEditable(false);
    msg.setDisabledTextColor(Color.black);
    msg.setWrapStyleWord(true);

    input = new JTextField();
    input.addActionListener(this);

    scrollPane =
        new JScrollPane(
            msg,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    // Testing if images loaded correctly -  doesnt need to do this with jar file
    // File f = new File("cards.gif");
    // if (!f.exists() || !f.isFile() || !f.canRead())
    // addMsg("Unable to load file " +f.getAbsolutePath());

    panel = new JPanel();
    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    panel.setLayout(gridbag);
    panel.setBackground(Color.white);
    getContentPane().add(panel);

    // Menu bar spans entire length
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 2;
    panel.add(menuBar, c);

    // Game occupies middle area
    c.fill = GridBagConstraints.BOTH;
    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 1;
    c.gridheight = 2;
    panel.add(image, c);

    // Chat goes to the right of game
    c.fill = GridBagConstraints.VERTICAL;
    c.gridx = 1;
    c.gridheight = 1;
    c.weighty = 1.0;
    panel.add(scrollPane, c);

    c.fill = GridBagConstraints.BOTH;
    c.gridy = 2;
    c.weighty = 0.0;
    panel.add(input, c);

    score = new Score(this);

    soundManager = new SoundManager();
  }

  public void actionPerformed(ActionEvent event) {
    String label = event.getActionCommand();

    if (label.equals("Quit")) {

      // Closing Sockets on Quiting
      if (servermsg != null) servermsg.endConnection();
      if (message != null) message.endConnection();
      if (dealer != null) dealer.endConnection();
      if (player != null) player.endConnection();

      System.exit(0); // terminates the program
    } else if (label.equals("Close Connections")) {
      closeConnection();
    } else if (label.equals("1 Player")) {
      addMsg("One Play Option");
      if (dealer != null || player != null) {
        addMsg(
            "You already have sockets open.  You must select  \"Close Connections\"  before you"
                + " can create a new connection");
      } else {
        DealerD dealerD = new DealerD(this);
        dealerD.setVisible(true);
        playersName = dealerD.getName();
        if (playersName != "cancel#*#") {
          if (dealerD.fastgame()) addMsg("Fast Game Selected");
          if (dealerD.seven()) addMsg("Must play under Seven Selected");
          if (dealerD.nine()) addMsg("Nine is Invisible Selected");
          if (dealerD.swap()) addMsg("Card Swap at start of game Selected");
          dealer =
              new Dealer(
                  this,
                  g,
                  hand,
                  dealerD.fastgame(),
                  dealerD.seven(),
                  dealerD.nine(),
                  dealerD.swap(),
                  score);
          dealer.onePlayer(playersName);
          imServer = true;
        }
      }
    } else if (label.equals("Redeal")) {
      if (dealer != null) dealer.redeal();
    } else if (label.equals("Start Game")) {
      if (dealer != null) dealer.start();
    } else if (label.equals("Scoreboard")) {
      score.display();
    } else if (label.equals("About")) {
      InfoD info = new InfoD(this);
      info.setVisible(true);
    } else if (label.equals("Rules")) {
      RulesD rules = new RulesD(this);
      rules.setVisible(true);
    } else if (label.equals("AI Speed")) {
      if (dealer != null) {
        Speed speed = new Speed(this, dealer.getAIPause());
        int temp = speed.getSpeed();
        addMsg("AI Speed: " + (double) temp / 1000 + " seconds");
        dealer.setAIPause(temp);
      } else addMsg("You must be the Dealer to change the AI Speed");
    } else if (label.equals("Multi Player Server")) {
      if (dealer != null || player != null) {
        addMsg(
            "You already have sockets open.  You must select  \"Close Connections\"  before you"
                + " can create a new connection");
      } else {
        DealerD dealerD = new DealerD(this);
        dealerD.setVisible(true);
        playersName = dealerD.getName();
        if (!(playersName == "cancel#*#" || playersName.equals(""))) {
          if (dealerD.fastgame()) addMsg("Fast Game Selected");
          if (dealerD.seven()) addMsg("Must play under Seven Selected");
          if (dealerD.nine()) addMsg("Nine is Invisible Selected");
          if (dealerD.swap()) addMsg("Card Swap at start of game Selected");
          servermsg = new ServerMessage(this);
          servermsg.createConnection(playersName);
          dealer =
              new Dealer(
                  this,
                  g,
                  hand,
                  dealerD.fastgame(),
                  dealerD.seven(),
                  dealerD.nine(),
                  dealerD.swap(),
                  score);
          dealer.createConnection(playersName);
          imServer = true;

          try { // outputting host name and ip address
            java.net.InetAddress i = java.net.InetAddress.getLocalHost();
            java.net.InetAddress[] add = java.net.InetAddress.getAllByName(i.getHostName());

            addMsg("Host Name: " + i.getHostName()); // name
            addMsg("IP Address: " + i.getHostAddress()); // local IP address

            // Getting all address on system

            for (int n = 1; n < add.length; n++)
              addMsg("Other IP Address: " + add[n].getHostAddress()); // Global IP address

          } catch (Exception e) {
            addMsg("Error Finding IP : " + e);
            e.printStackTrace();
          }
        }
      }
    } else if (label.equals("Multi Player Client")) {
      if (dealer != null || player != null) {
        addMsg(
            "You already have sockets open.  You must select  \"Close Connections\"  before you"
                + " can create a new connection");
      } else {
        PlayerD playerD = new PlayerD(this);
        playerD.setVisible(true);
        playersName = playerD.getName();
        String servername = playerD.getServer();
        if (!(playersName == "cancel"
            || servername == "cancel"
            || playersName.equals("")
            || servername.equals(""))) {
          message = new Message(this);
          message.createConnection(servername, playersName);
          player = new Player(this, g, hand, score);
          player.createConnection(servername, playersName);
        }
      }
    } else {
      addMsg(": " + input.getText());
      if (servermsg != null) servermsg.sendMsg(input.getText());
      if (message != null) message.sendMsg(input.getText());
      input.setText("");
    }
  }

  public void closeConnection() {

    // Closing Sockets on Quiting
    if (servermsg != null) servermsg.endConnection();
    if (message != null) message.endConnection();
    if (dealer != null) {
      dealer.destroy();
      dealer = null;
    }
    if (player != null) {
      player.endConnection();
      player = null;
    }
    hand.removeAll();

    score = new Score(this);

    // redrawint title
    g.setColor(Color.black);
    g.fillRect(0, 0, 450, 550);
    g.setColor(Color.white);
    g.drawImage(imageManager.getTitle(), -40, 120, this);
    g.setColor(Color.white);
    g.drawLine(0, 450, 450, 450);
    repaint();
  }

  public ImageManager getImageManager() {
    return imageManager;
  }

  public void addMsg(String message) {
    if (message != null) msg.append(message + "\n");
    try { // scroll to end of display
      // msg.scrollRectToVisible(
      // new Rectangle(0,
      //     msg.getLineCount() * , 0, (msg.getLineCount() + 5) * 100));
      // msg.scrollRectToVisible(msg.modelToView(msg.getLineCount()));
      Rectangle current = msg.getVisibleRect();
      int scrollunitinc = msg.getScrollableUnitIncrement(current, SwingConstants.VERTICAL, 1);
      // System.out.println("scrollunitinc " + scrollunitinc + " Y " + current.getY());
      // current.setRect(current.getX(), current.getY()  + scrollunitinc, current.getWidth(),
      // current.getHeight());
      current.setRect(
          current.getX(),
          (msg.getLineCount() + 1) * scrollunitinc,
          current.getWidth(),
          current.getHeight());
      // System.out.println("Y " + current.getY());
      msg.scrollRectToVisible(current);
    } catch (Exception ex) {
      System.out.println("\n Error scrolling to end " + ex);
    }
  }

  public void mousePressed(MouseEvent me) {
    if (myTurn) {
      int selection = hand.mouseClick(mouseX, mouseY);
      if (dealer != null && selection != -1) dealer.cardSelection(selection);
      if (player != null && selection != -1) player.cardSelection(selection);
    }
  }

  public void setmyTurn(boolean myTurn) {
    this.myTurn = myTurn;
  }

  public void repaint() {
    panel.repaint();
  }

  public void mouseMoved(MouseEvent me) {
    // ajusting so mouse points are over image
    mouseX = me.getX() - 5;
    mouseY = me.getY() - 45;
  }

  public void mouseDragged(MouseEvent e) {}

  public void mouseEntered(MouseEvent me) {}

  public void mouseExited(MouseEvent me) {}

  public void mouseClicked(MouseEvent me) {}

  public void mouseReleased(MouseEvent me) {}

  public static void main(String[] args) {

    // setting look and feel
    // try {
    // UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    //   UIManager.setLookAndFeel("javax.swing.plaf.mac.MacLookAndFeel");
    // } catch (Exception e) { System.out.println("Look and Feel Error " + e); }

    SHinterface frame = new SHinterface();
    frame.setTitle("Shithead");
    frame.setResizable(false);

    frame.pack();
    frame.setVisible(true);
  }

  public void windowClosing(WindowEvent e) {
    // Closing Sockets on Quiting
    if (servermsg != null) servermsg.endConnection();
    if (message != null) message.endConnection();
    if (dealer != null) dealer.endConnection();
    if (player != null) player.endConnection();
    System.exit(0);
  }

  public void windowClosed(WindowEvent e) {}

  public void windowOpened(WindowEvent e) {}

  public void windowIconified(WindowEvent e) {}

  public void windowDeiconified(WindowEvent e) {}

  public void windowActivated(WindowEvent e) {}

  public void windowDeactivated(WindowEvent e) {}

  private JMenuBar buildMenuBar() {

    JMenuBar menuBar = new JMenuBar();

    // Build the first menu.
    JMenu menu = new JMenu("File");
    menu.setMnemonic(KeyEvent.VK_F);
    menu.getAccessibleContext().setAccessibleDescription("Game Options Can Be Found Here");
    menuBar.add(menu);

    // File group of JMenuItems
    JMenuItem menuItem = new JMenuItem("1 Player", KeyEvent.VK_P);
    menuItem.getAccessibleContext().setAccessibleDescription("Start Single Player Game");
    menuItem.addActionListener(this);
    menu.add(menuItem);

    menuItem = new JMenuItem("Multi Player Server", KeyEvent.VK_S);
    menuItem
        .getAccessibleContext()
        .setAccessibleDescription("Be the server for a Multi player game");
    menuItem.addActionListener(this);
    menu.add(menuItem);

    menuItem = new JMenuItem("Multi Player Client", KeyEvent.VK_C);
    menuItem
        .getAccessibleContext()
        .setAccessibleDescription("Connect to another Multi player Game");
    menuItem.addActionListener(this);
    menu.add(menuItem);

    menuItem = new JMenuItem("Close Connections", KeyEvent.VK_L);
    menuItem.getAccessibleContext().setAccessibleDescription("Close Game Connection");
    menuItem.addActionListener(this);
    menu.add(menuItem);

    menuItem = new JMenuItem("Scoreboard", KeyEvent.VK_B);
    menuItem.getAccessibleContext().setAccessibleDescription("Veiw Scoreboard");
    menuItem.addActionListener(this);
    menu.add(menuItem);

    menuItem = new JMenuItem("Quit", KeyEvent.VK_Q);
    menuItem.getAccessibleContext().setAccessibleDescription("Quit Game");
    menuItem.addActionListener(this);
    menu.add(menuItem);

    // Build second menu in the menu bar.
    menu = new JMenu("Options");
    menu.getAccessibleContext().setAccessibleDescription("Game Options");
    menuBar.add(menu);

    menuItem = new JMenuItem("Redeal", KeyEvent.VK_R);
    menuItem.getAccessibleContext().setAccessibleDescription("Redeal the deck");
    menuItem.addActionListener(this);
    menu.add(menuItem);

    menuItem = new JMenuItem("Start Game", KeyEvent.VK_S);
    menuItem.getAccessibleContext().setAccessibleDescription("Start game now");
    menuItem.addActionListener(this);
    menu.add(menuItem);

    menuItem = new JMenuItem("AI Speed", KeyEvent.VK_A);
    menuItem
        .getAccessibleContext()
        .setAccessibleDescription("Time it takes for GameAI to make a move");
    menuItem.addActionListener(this);
    menu.add(menuItem);

    // Build 3rd menu in the menu bar.
    menu = new JMenu("About");
    menu.getAccessibleContext().setAccessibleDescription("About the Game");
    menuBar.add(menu);

    menuItem = new JMenuItem("Rules", KeyEvent.VK_R);
    menuItem.getAccessibleContext().setAccessibleDescription("How to play");
    menuItem.addActionListener(this);
    menu.add(menuItem);

    menuItem = new JMenuItem("About", KeyEvent.VK_A);
    menuItem.getAccessibleContext().setAccessibleDescription("About the Game");
    menuItem.addActionListener(this);
    menu.add(menuItem);

    return menuBar;
  }
}
