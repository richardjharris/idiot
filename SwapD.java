import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
/**
 * Swap Dialog
 * 
 * @author Sam Cavenagh
 * @version 02/12/02
 * 
 * Website: http://home.pacific.net.au/~cavenagh/SH/
 * Email: cavenaghweb@hotmail.com
 */
public class SwapD extends JDialog implements ActionListener
{

    JLabel handLabel = new JLabel("Cards in Hand", JLabel.CENTER);
    JLabel tableLabel = new JLabel("Cards on Table", JLabel.CENTER);

    JList hand;
    JList table;

    JScrollPane handScroll;
    JScrollPane tableScroll;

    private DefaultListModel handModel;
    private DefaultListModel tableModel;

    JButton left = new JButton("<<");
    JButton right = new JButton(">>");

    JButton swap = new JButton("  Swap Cards   ");
    JButton noswap = new JButton("Dont Swap Cards");

    JPanel panel;

    Hand inhand;
    Hand ontable;
    int inontable = 3;

    boolean swapB = false;

    JFrame parent;

    public SwapD(JFrame parent, Card[] dealthand, Card[] dealttable)
    {

        super(parent, "Card Swap", true);
        setSize(260, 200);

        Point p = parent.getLocation();
        setLocation((int)p.getX() + 90,(int)p.getY() + 150);

        this.parent = parent;

        inhand = new Hand();
        ontable = new Hand();

        for(int n = 0; n < 3; n++){
            inhand.addCard(dealthand[n]);
            ontable.addCard(dealttable[n]);
        }

        panel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        panel.setLayout(gridbag);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH; 
        getContentPane().add(panel);
        c.weightx = 1;
        //c.weighty = 1;

        c.gridy = 1;
        panel.add(handLabel, c);
        panel.add(tableLabel, c);

        //JList ----------------

        c.gridy = 2;

        handModel = new DefaultListModel();
        tableModel = new DefaultListModel();

        //adding card string value to JList
        for(int n = 0; n < 3; n++)
        {
            handModel.addElement(inhand.getCard(n).getStringValue());
            tableModel.addElement(ontable.getCard(n).getStringValue());
        }

        hand = new JList(handModel);
        table = new JList(tableModel);
            

        hand.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        handScroll = new JScrollPane(hand);
        tableScroll = new JScrollPane(table);

        c.weighty = 1;
        panel.add(handScroll, c);
        panel.add(tableScroll, c);
        c.weighty = 0;

        c.gridy = 3;

        panel.add(right, c);
        right.addActionListener(this);

        panel.add(left, c);
        left.addActionListener(this);

        c.gridy = 4;

        panel.add(swap, c);
        swap.addActionListener(this);

        panel.add(noswap, c);
        noswap.addActionListener(this);


    }

    public void actionPerformed(ActionEvent e)
    {
    String pressed = e.getActionCommand();
    String selection = "none";
    if(pressed.equals(">>")){
        try{
        selection = handModel.getElementAt(hand.getSelectedIndex()).toString();
        }catch(Exception w){ selection = "none";}
        if(!selection.equals("none")){
            handModel.removeElementAt(hand.getSelectedIndex());
            for(int n = 0; n < 6; n++){
                if(inhand.getCard(n).getStringValue().equals(selection)){
                    ontable.addCard(inhand.getCard(n));
                    tableModel.addElement(inhand.getCard(n).getStringValue());
                    inhand.removeCard(n);
                    break;
                }
            }
        }
    }
    else if(pressed.equals("<<")){
        try{
        selection = tableModel.getElementAt(table.getSelectedIndex()).toString();
        }catch(Exception w){ selection = "none";}
        if(!selection.equals("none")){
            tableModel.removeElementAt(table.getSelectedIndex());
            for(int n = 0; n < 6; n++){
                if(ontable.getCard(n).getStringValue().equals(selection)){
                    inhand.addCard(ontable.getCard(n));
                    handModel.addElement(ontable.getCard(n).getStringValue());
                    ontable.removeCard(n);
                    break;
                }
            }
        }
    }
    else if(pressed.equals("  Swap Cards   ")){
        if(inhand.length() == 4 && ontable.length() == 4){
            swapB = true;
            setVisible(false);
        }else
            JOptionPane.showMessageDialog(parent, "You must have 3 in each group.", "Swap Error", JOptionPane.WARNING_MESSAGE);
    }else
        setVisible(false);
    }

    public boolean display()
    {
    show();
    return swapB;
    }

    public Card[] getInHand()
    {
    Card cards[] = new Card[3];
    for(int n = 0; n < 3; n++)
        cards[n] = inhand.getCard(n);
    return cards;
    }

    public Card[] getOnTable()
    {
    Card cards[] = new Card[3];
    for(int n = 0; n < 3; n++)
        cards[n] = ontable.getCard(n);
    return cards;
    }

}
