package org.cytoscape.cpath2.internal.view.tree;

import org.cytoscape.cpath2.internal.view.CollapsablePanel;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;

/**
 * Tree Demo.  Used to debug JTreeWithCheckNodes.
 *
 * Code was originally obtained from:
 * http://www.javaresearch.org/source/javaresearch/jrlib0.6/org/jr/swing/tree/
 *
 * and, has since been modified.
 */
public class TreeDemo extends JFrame {

  public TreeDemo() {
    super("CheckNode TreeExample");
    String[] strs = {"Filters (optional)",                  // 0
                 "Filter by Data Source",                   // 1
                 "Reactome (4)",                            // 2
                 "Cancer Cell Map (5)",                     // 3
                 "Filter by Interaction Type",              // 4
                 "Protein-Protein Interactions (43)",       // 5
                 "Other (52)"};                             // 6

    CheckNode[] nodes = new CheckNode[strs.length];
    for (int i=0;i<strs.length;i++) {
      nodes[i] = new CheckNode(strs[i]);
    }
    nodes[0].add(nodes[1]);
    nodes[1].add(nodes[2]);
    nodes[1].add(nodes[3]);
    nodes[0].add(nodes[4]);
    nodes[3].setSelected(true);
    nodes[4].add(nodes[5]);
    nodes[4].add(nodes[6]);
    JTreeWithCheckNodes tree = new JTreeWithCheckNodes( nodes[0] );
    JScrollPane sp = new JScrollPane(tree);

    JTextArea textArea = new JTextArea(3,10);
    JScrollPane textPanel = new JScrollPane(textArea);
    JButton button = new JButton("print");
    button.addActionListener(
      new ButtonActionListener(nodes[0], textArea));
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(button, BorderLayout.SOUTH);

    CollapsablePanel filterPanel = new CollapsablePanel("Filters (Optional)");
    filterPanel.getContentPane().add(sp);

    getContentPane().add(filterPanel,    BorderLayout.CENTER);
    getContentPane().add(panel, BorderLayout.EAST);
    getContentPane().add(textPanel, BorderLayout.SOUTH);
  }

  class ButtonActionListener implements ActionListener {
    CheckNode root;
    JTextArea textArea;

    ButtonActionListener(final CheckNode root,
                         final JTextArea textArea) {
      this.root     = root;
      this.textArea = textArea;
    }

    public void actionPerformed(ActionEvent e) {
      Enumeration enum1 = root.breadthFirstEnumeration();
      while (enum1.hasMoreElements()) {
        CheckNode node = (CheckNode)enum1.nextElement();
        if (node.isSelected()) {
          TreeNode[] nodes = node.getPath();
          textArea.append("\n" + nodes[0].toString());
          for (int i=1;i<nodes.length;i++) {
            textArea.append("/" + nodes[i].toString());
          }
        }
      }
    }
  }

  public static void main(String args[]) {
    TreeDemo frame = new TreeDemo();
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });
    frame.setSize(300, 200);
    frame.setVisible(true);
  }
}