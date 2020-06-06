import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Dialog for displaying score information
 *
 * @author Sam Cavenagh
 * @version 27/2/03
 *     <p>Website: http://home.pacific.net.au/~cavenagh/SH/ Email: cavenaghweb@hotmail.com
 */
public class Score extends JDialog implements ActionListener {

  private static final long serialVersionUID = 1L;

  JLabel position[] = new JLabel[5];
  JLabel nameL = new JLabel("Name", JLabel.CENTER);
  JLabel firstL = new JLabel("First ");
  JLabel secondL = new JLabel("Second");
  JLabel thirdL = new JLabel("Third ");
  JLabel fourthL = new JLabel("Shithead");

  JTextField names[] = new JTextField[4];
  JTextField first[] = new JTextField[4];
  JTextField second[] = new JTextField[4];
  JTextField third[] = new JTextField[4];
  JTextField fourth[] = new JTextField[4];

  JButton ok = new JButton("Ok");

  JPanel panel;

  String name[] = new String[4];
  int firsts[] = new int[4];
  int seconds[] = new int[4];
  int thirds[] = new int[4];
  int fourths[] = new int[4];
  int total[] = new int[4];

  int namecount = 0;

  JFrame parent;

  // redeal button for dealer only
  Dealer dealer = null;
  JButton redeal = new JButton("Redeal");
  GridBagConstraints c;

  public Score(JFrame parent) {

    super(parent, "Scoreboard", true);
    setSize(300, 200);
    Point p = parent.getLocation();
    setLocation((int) p.getX() + 80, (int) p.getY() + 100);

    this.parent = parent;

    position[0] = new JLabel(" "); // Position
    position[1] = new JLabel("First ", JLabel.CENTER);
    position[2] = new JLabel("Second", JLabel.CENTER);
    position[3] = new JLabel("Third ", JLabel.CENTER);
    position[4] = new JLabel("Shithead", JLabel.CENTER);

    for (int n = 0; n < 4; n++) {

      firsts[n] = 0;
      seconds[n] = 0;
      thirds[n] = 0;
      fourths[n] = 0;
      total[n] = 0;

      names[n] = new JTextField("unknown");
      first[n] = new JTextField(" " + firsts[n] + " ");
      second[n] = new JTextField(" " + seconds[n] + " ");
      third[n] = new JTextField(" " + thirds[n] + " ");
      fourth[n] = new JTextField(" " + fourths[n] + " ");

      names[n].setEditable(false);
      first[n].setEditable(false);
      second[n].setEditable(false);
      third[n].setEditable(false);
      fourth[n].setEditable(false);

      names[n].setHorizontalAlignment(JTextField.CENTER);
      first[n].setHorizontalAlignment(JTextField.CENTER);
      second[n].setHorizontalAlignment(JTextField.CENTER);
      third[n].setHorizontalAlignment(JTextField.CENTER);
      fourth[n].setHorizontalAlignment(JTextField.CENTER);
    }

    panel = new JPanel();
    GridBagLayout gridbag = new GridBagLayout();
    c = new GridBagConstraints();
    panel.setLayout(gridbag);
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.BOTH;
    c.ipadx = 5; // 2
    c.ipady = 5; // not here
    getContentPane().add(panel);

    c.gridy = 1;
    panel.add(position[0], c);
    panel.add(nameL, c);
    panel.add(firstL, c);
    panel.add(secondL, c);
    panel.add(thirdL, c);
    panel.add(fourthL, c);

    for (int n = 0; n < 4; n++) {
      c.gridy++;
      panel.add(position[n + 1], c);
      panel.add(names[n], c);
      panel.add(first[n], c);
      panel.add(second[n], c);
      panel.add(third[n], c);
      panel.add(fourth[n], c);
    }

    c.gridy++;

    panel.add(ok, c);
    // JLabel space = new JLabel("       ");
    // panel.add(space, c);

    ok.addActionListener(this);
  }

  public void actionPerformed(ActionEvent e) {
    String pressed = e.getActionCommand();
    setVisible(false);
    if ((pressed.equals("Redeal"))) {
      dealer.redeal();
    }
  }

  public void addName(String newname) {
    if (namecount < 4) {
      names[namecount].setText(newname);
      name[namecount] = newname;
      namecount++;
    }
  }

  public void addScore(String playerName, int position) {
    // adding info
    for (int n = 0; n < 4; n++)
      if (playerName.equals(name[n])) {
        switch (position) {
          case 1:
            firsts[n]++;
            break;
          case 2:
            seconds[n]++;
            break;
          case 3:
            thirds[n]++;
            break;
          case 4:
            fourths[n]++;
            break;
        }
        total[n] = (firsts[n] + 2 * seconds[n] + 3 * thirds[n] + 4 * fourths[n]);

        // updating display
        int highertotal = 0;
        int lessertotal = 0;
        for (int i = 0; i < 4; i++)
          if (lessertotal < total[i]) {
            names[3].setText(name[i]);
            first[3].setText(" " + firsts[i] + " ");
            second[3].setText(" " + seconds[i] + " ");
            third[3].setText(" " + thirds[i] + " ");
            fourth[3].setText(" " + fourths[i] + " ");

            lessertotal = total[i];
          }

        for (int s = 2; s >= 0; s--) {
          highertotal = lessertotal;
          lessertotal = 0;
          for (int i = 0; i < 4; i++)
            if ((highertotal > total[i] && lessertotal < total[i])
                || (total[i] == highertotal
                    && !(name[i].equals(
                        names[s + 1].getText())))) { // second clause for when score are equal
              // if more than 2 players have the same score, like at start of game
              boolean pass = true;
              if (s < 2) if (name[i].equals(names[s + 2].getText())) pass = false;
              if (s < 1) if (name[i].equals(names[s + 3].getText())) pass = false;

              if (pass) {
                names[s].setText(name[i]);
                first[s].setText(" " + firsts[i] + " ");
                second[s].setText(" " + seconds[i] + " ");
                third[s].setText(" " + thirds[i] + " ");
                fourth[s].setText(" " + fourths[i] + " ");

                lessertotal = total[i];
              }
            }
        }

        break;
      }
  }

  public void imDealer(Dealer dealer) {
    this.dealer = dealer;
    c.gridwidth = 2;
    panel.add(redeal, c);
    redeal.addActionListener(this);
  }

  public void display() {
    Point p = parent.getLocation();
    setLocation((int) p.getX() + 80, (int) p.getY() + 100);
    setVisible(true);
  }
}
