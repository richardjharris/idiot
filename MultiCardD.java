import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * For when player wants to play more than one card of the same suit
 *
 * @author Sam Cavenagh
 * @version 21/11/02
 *     <p>Website: http://home.pacific.net.au/~cavenagh/SH/ Email: cavenaghweb@hotmail.com
 */
public class MultiCardD extends JDialog implements ActionListener {

  private static final long serialVersionUID = 1L;

  JLabel question = new JLabel("How many cards would you like to play ?");

  JButton buttons[] = new JButton[4];

  JPanel panel;

  int choice = 1;

  public MultiCardD(JFrame parent, int cardcount) {

    super(parent, "MultiCard Selection", true);
    setSize(280, 80);

    Point p = parent.getLocation();
    setLocation((int) p.getX() + 60, (int) p.getY() + 400);

    panel = new JPanel();
    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    panel.setLayout(gridbag);
    // c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    getContentPane().add(panel);

    c.gridy = 1;
    c.gridwidth = cardcount;
    panel.add(question, c);

    c.weightx = 1;
    c.gridy = 2;
    c.gridwidth = 1;

    buttons[0] = new JButton("Play 1");
    buttons[1] = new JButton("Play 2");
    buttons[2] = new JButton("Play 3");
    buttons[3] = new JButton("Play 4");

    if (cardcount > 4) cardcount = 4;

    for (int n = 0; n < cardcount; n++) {
      panel.add(buttons[n], c);
      buttons[n].addActionListener(this);
    }
  }

  public void actionPerformed(ActionEvent e) {
    String pressed = e.getActionCommand();
    if (pressed.equals("Play 1")) choice = 1;
    if (pressed.equals("Play 2")) choice = 2;
    if (pressed.equals("Play 3")) choice = 3;
    if (pressed.equals("Play 4")) choice = 4;
    setVisible(false);
  }

  public int getChoice() {
    setVisible(true);
    return choice;
  }
}
