import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Dialog for getting player information
 *
 * @author Sam Cavenagh
 * @version 21/11/02
 *     <p>Website: http://home.pacific.net.au/~cavenagh/SH/ Email: cavenaghweb@hotmail.com
 */
public class PlayerD extends JDialog implements ActionListener {

  private static final long serialVersionUID = 1L;

  JLabel question1 = new JLabel("Name:");

  JLabel question2 = new JLabel("Server:");

  JTextField nameF = new JTextField("client" + (int) Math.round(Math.random() * 51), 15);

  // JTextField serverF = new JTextField("bwypc420-11", 15);

  JTextField serverF = new JTextField(15);

  JButton ok = new JButton("Ok");

  JButton cancel = new JButton("Cancel");

  JPanel panel;

  String name = "cancel";

  String server = "cancel";

  JFrame parent;

  public PlayerD(JFrame parent) {

    super(parent, "Players Details", true);
    setSize(200, 100);
    Point p = parent.getLocation();
    setLocation((int) p.getX() + 80, (int) p.getY() + 100);

    this.parent = parent;

    panel = new JPanel();
    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    panel.setLayout(gridbag);
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.BOTH;
    c.ipadx = 2;
    getContentPane().add(panel);

    c.gridy = 1;
    panel.add(question1, c);
    c.gridwidth = 2;
    panel.add(nameF, c);
    nameF.addActionListener(this);
    c.gridwidth = 1;

    c.gridy = 2;

    panel.add(question2, c);
    c.gridwidth = 2;
    panel.add(serverF, c);
    serverF.addActionListener(this);
    c.gridwidth = 1;

    c.gridy = 3;

    panel.add(ok, c);
    panel.add(cancel, c);
    JLabel space = new JLabel("       ");
    panel.add(space, c);

    ok.addActionListener(this);
    cancel.addActionListener(this);
  }

  public void actionPerformed(ActionEvent e) {
    boolean pass = true;
    String pressed = e.getActionCommand();
    if (!(pressed.equals("Cancel"))) { // was = to ok
      name = nameF.getText();
      if (name.equals("")) {
        pass = false;
        JOptionPane.showMessageDialog(
            parent, "You must enter a Name.", "Input Error", JOptionPane.WARNING_MESSAGE);
      }
      server = serverF.getText();
      if (server.equals("")) {
        pass = false;
        JOptionPane.showMessageDialog(
            parent, "You must enter a Server Name.", "Input Error", JOptionPane.WARNING_MESSAGE);
      }
    }

    if (pass) setVisible(false);
  }

  public String getName() {
    return name;
  }

  public String getServer() {
    return server;
  }
}
