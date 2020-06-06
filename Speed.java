import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * JSilder Dialog for Controlling AI Speed
 *
 * @author Sam Cavenagh
 * @version 21/11/02
 *     <p>Website: http://home.pacific.net.au/~cavenagh/SH/ Email: cavenaghweb@hotmail.com
 */
public class Speed extends JDialog implements ActionListener {

  private static final long serialVersionUID = 1L;

  JLabel label = new JLabel("Select AI Speed", JLabel.CENTER);

  JSlider speed;

  JButton ok = new JButton("Ok");

  JButton cancel = new JButton("Cancel");

  JPanel panel;

  int speedint = 1000;

  public Speed(JFrame parent, int speedint) {

    super(parent, "AI Wait Time", true);
    setSize(180, 110);
    Point p = parent.getLocation();
    setLocation((int) p.getX() + 130, (int) p.getY() + 100);

    this.speedint = speedint;

    panel = new JPanel();
    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    panel.setLayout(gridbag);
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.BOTH;
    c.ipadx = 2;
    getContentPane().add(panel);

    c.gridy = 1;
    c.gridwidth = 2;
    panel.add(label, c);

    c.gridy = 2;
    speed = new JSlider(JSlider.HORIZONTAL, 0, 2000, speedint);

    // speed.setMajorTickSpacing(1000);
    speed.setMinorTickSpacing(100);
    // speed.setLabelTable(speed.createStandardLabels(1000 , 0));
    speed.setPaintTicks(true);
    speed.setPaintLabels(true);
    speed.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
    panel.add(speed, c);

    c.gridwidth = 1;
    c.gridy = 3;

    panel.add(ok, c);
    panel.add(cancel, c);

    ok.addActionListener(this);
    cancel.addActionListener(this);
  }

  public void actionPerformed(ActionEvent e) {
    String pressed = e.getActionCommand();
    if (!(pressed.equals("Cancel"))) {
      speedint = speed.getValue();
    }
    setVisible(false);
  }

  public int getSpeed() {
    setVisible(true);
    return speedint;
  }
}
