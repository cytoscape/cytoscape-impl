package org.cytoscape.cpath2.internal.view.tree;

/*
 * #%L
 * Cytoscape CPath2 Impl (cpath2-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import org.cytoscape.cpath2.internal.view.GradientHeader;

import javax.swing.tree.TreeCellRenderer;
import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * Node with CheckBox Renderer.
 *
 * Code was originally obtained from:
 * http://www.javaresearch.org/source/javaresearch/jrlib0.6/org/jr/swing/tree/
 *
 * and, has since been modified.
*/
public class CheckNodeRenderer implements TreeCellRenderer {

    /**
     * Gets the Tree Cell Renderer.
     * @param tree          JTree Object.
     * @param value         Object value.
     * @param isSelected    Node is selected.
     * @param expanded      Node is expanded.
     * @param leaf          Node is a leaf.
     * @param row           Row number.
     * @param hasFocus      Node has focus.
     * @return Custom Component.
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean isSelected, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {
        String stringValue = tree.convertValueToText(value, isSelected,
                expanded, leaf, row, hasFocus);
        CustomNodePanel customNodePanel = new CustomNodePanel(tree, value,
                expanded, leaf, stringValue);
        customNodePanel.setEnabled(tree.isEnabled());
        return customNodePanel;
    }
}

/**
 * Custom Node Panel.
 *
 * Code was originally obtained from:
 * http://www.javaresearch.org/source/javaresearch/jrlib0.6/org/jr/swing/tree/
 *
 */
class CustomNodePanel extends JPanel {
    private JCheckBox check;
    private JLabel label;
    private static URL url = GradientHeader.class
    		.getResource("/org/cytoscape/cpath2/internal/view/stock_autofilter.png");
    private static ImageIcon filterIcon = new ImageIcon(url);

    /**
     * Constructor.
     * @param tree      JTree Object.
     * @param value     Object value.
     * @param expanded  Node is expanded.
     * @param leaf      Node is a leaf.
     * @param stringValue String value.
     */
    public CustomNodePanel(JTree tree, Object value,
            boolean expanded, boolean leaf, String stringValue) {
        setLayout(new BorderLayout());
        if (leaf) {
            add(check = new JCheckBox(), BorderLayout.WEST);
            check.setBackground(UIManager.getColor("Tree.textBackground"));
            check.setSelected(((CheckNode) value).isSelected());
            check.setOpaque(false);
        }
        add(label = new JLabel(), BorderLayout.EAST);
        label.setOpaque(false);
        label.setFont(tree.getFont());
        label.setText(stringValue);
        if (leaf) {
            //label.setIcon(UIManager.getIcon("Tree.leafIcon"));
        } else if (expanded) {
            label.setIcon(filterIcon);
            //label.setIcon(UIManager.getIcon("Tree.openIcon"));
        } else {
            label.setIcon(filterIcon);
            //label.setIcon(UIManager.getIcon("Tree.closedIcon"));
        }
        setOpaque(false);
    }
}
