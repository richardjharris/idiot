import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
/**
 * Dialog for getting Dealer information
 * 
 * @author Sam Cavenagh
 * @version 21/11/02
 * 
 * Website: http://home.pacific.net.au/~cavenagh/SH/
 * Email: cavenaghweb@hotmail.com
 */
public class DealerD extends JDialog implements ActionListener
{

    JLabel question1 = new JLabel("Name:");
    JLabel title = new JLabel("Game Options:");

    JTextField nameF = new JTextField("server",15);

    JCheckBox fastgame = new JCheckBox("Fast Game");

    JCheckBox seven = new JCheckBox("Must play under 7");

    JCheckBox swap = new JCheckBox("Swap at Start");

    JCheckBox nine = new JCheckBox("Nine is Invisible");

    JButton ok = new JButton("Ok");

    JButton cancel = new JButton("Cancel");

    JPanel panel;

    String name = "cancel#*#";

    boolean fastgameB = false;
    boolean sevenB = false;
    boolean nineB = false;
    boolean swapB = false;

    JFrame parent;

    public DealerD(JFrame parent)
    {

        super(parent, "Players Details", true);
        setSize(250, 150);
        Point p = parent.getLocation();
        setLocation((int)p.getX() + 80,(int)p.getY() + 100);

        this.parent = parent;

        panel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        panel.setLayout(gridbag);
        c.anchor = GridBagConstraints.WEST;
        //c.fill = GridBagConstraints.BOTH; 
        getContentPane().add(panel);

        c.gridy = 1;
        panel.add(question1, c);
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH; 
        panel.add(nameF, c);
        nameF.addActionListener(this);
        c.fill = GridBagConstraints.NONE; 

        c.gridy = 2;
        c.gridwidth = 3;
        panel.add(title, c);
        c.gridwidth = 1;

        c.gridy = 3;
        panel.add(fastgame, c);
        c.gridwidth = 2;
        panel.add(seven, c);
        c.gridwidth = 1;

        c.gridy = 4;
        panel.add(swap, c);
        c.gridwidth = 2;
        panel.add(nine, c);
        c.gridwidth = 1;

        c.gridy = 5;
    
        panel.add(ok, c);
        panel.add(cancel, c);
        JLabel space = new JLabel("       ");
        panel.add(space, c);

        ok.addActionListener(this);
        cancel.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e)
    {
    boolean pass = true;
    String pressed = e.getActionCommand();
    if(!(pressed.equals("Cancel"))){
        name = nameF.getText();
        if(name.equals("")){
            pass = false;
            JOptionPane.showMessageDialog(parent, "You must enter a Name.", "Input Error", JOptionPane.WARNING_MESSAGE);
        }
        if(fastgame.isSelected())
            fastgameB = true;
        if(seven.isSelected())
            sevenB = true;
        if(nine.isSelected())
            nineB = true;
        if(swap.isSelected())
            swapB = true;
    }

    if(pass)
    setVisible(false);
    }

    public boolean fastgame()
    {
    return fastgameB;
    }

    public boolean seven()
    {
    return sevenB;
    }

    public boolean nine()
    {
    return nineB;
    }

    public boolean swap()
    {
    return swapB;
    }

    public String getName()
    {
    return name;
    }

}
