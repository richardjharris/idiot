import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import javax.swing.*;

/** Dialog for displaying game rules */
public class RulesD extends JDialog implements ActionListener {

  private static final long serialVersionUID = 1L;

  JPanel panel;

  public RulesD(JFrame parent) {
    super(parent, "Rules", true);
    setLocation((int) parent.getX() + 80, (int) parent.getY() + 100);

    String rules = loadRules();

    JTextArea ruleBox = new JTextArea(rules, 20, 50);
    ruleBox.setLineWrap(true);
    ruleBox.setEditable(false);
    ruleBox.setWrapStyleWord(true);
    ruleBox.setAutoscrolls(true); // ??

    JScrollPane scrollPane = new JScrollPane(ruleBox);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setViewportView(ruleBox);
    scrollPane.setAutoscrolls(true);

    JButton closeButton = new JButton("Close");
    closeButton.addActionListener(this);
    closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.add(scrollPane);
    panel.add(closeButton);
    getContentPane().add(panel);
    pack();
  }

  public void actionPerformed(ActionEvent e) {
    dispose();
  }

  private String loadRules() {
    String rules;
    try (InputStream rulesInput = getClass().getResourceAsStream("rules.txt")) {
      // Java 11
      //rules = new String(rulesInput.readAllBytes(), StandardCharsets.UTF_8);
      // Java 8
      rules = new BufferedReader(new InputStreamReader(rulesInput, StandardCharsets.UTF_8))
      .lines()
      .collect(Collectors.joining("\n"));
    } catch (IOException e) {
      rules = "Encountered an error opening the rules file.";
    }
    return rules;
  }
}
