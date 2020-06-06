import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Dialog for displaying game information
 *
 * @author Sam Cavenagh
 * @version 12/12/02
 *     <p>Website: http://home.pacific.net.au/~cavenagh/SH/ Email: cavenaghweb@hotmail.com
 */
public class InfoD extends JDialog {

  JLabel title = new JLabel("SHITHEAD", JLabel.CENTER);

  JLabel version = new JLabel("Version 1.12", JLabel.CENTER);

  JLabel creator = new JLabel("Created by Sam Cavenagh", JLabel.CENTER);

  JLabel sent1 = new JLabel("For more information on this game", JLabel.CENTER);

  JLabel sent2 = new JLabel("Please Visit my WebSite", JLabel.CENTER);

  JLabel website = new JLabel("http://home.pacific.net.au/~cavenagh", JLabel.CENTER);

  JLabel email = new JLabel("or Email me at cavenaghweb@hotmail.com", JLabel.CENTER);

  JLabel date = new JLabel("27 July 2003", JLabel.CENTER);

  JPanel panel;

  public InfoD(JFrame parent) {

    super(parent, "About", true);
    setSize(280, 180);
    Point p = parent.getLocation();
    setLocation((int) p.getX() + 80, (int) p.getY() + 100);

    title.setForeground(Color.black);
    creator.setForeground(Color.black);
    website.setForeground(Color.black);
    // version.setFont(new Font("Arial", Font.PLAIN, 12));

    panel = new JPanel();
    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    panel.setLayout(gridbag);
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.BOTH;
    c.ipadx = 2;
    getContentPane().add(panel);

    c.gridy = 1;
    panel.add(title, c);

    c.gridy = 2;
    panel.add(version, c);

    c.gridy = 3;
    panel.add(creator, c);

    c.gridy = 4;
    panel.add(sent1, c);

    c.gridy = 5;
    panel.add(sent2, c);

    c.gridy = 6;
    panel.add(website, c);

    c.gridy = 7;
    panel.add(email, c);

    c.gridy = 8;
    panel.add(date, c);
  }
}
