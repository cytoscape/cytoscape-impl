package org.cytoscape.work.internal.tunables.utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;


public class CollapsablePanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private JButton myExpandButton = null;
	private boolean expandPaneVisible;
	private String expandName = null;
	private String collapseName = null;
	private JPanel rightPanel = new JPanel();
	private JPanel leftPanel = new JPanel();	
	private List<Component> listInPane;
	
	
	public CollapsablePanel(final String name, final boolean initialState) {
		expandPaneVisible = initialState;
		
		listInPane = new ArrayList<Component>();
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());
		Border refBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		setBorder(refBorder);
		rightPanel.add(createButton(expandPaneVisible, name), BorderLayout.WEST);
		super.add(rightPanel);
				
		leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));
		setCollapsed(expandPaneVisible);
		super.add(leftPanel);

	}

	
	public Component add(Component c) {
		listInPane.add(c);
		return c;
	}


	public void add(Component c, Object o) {
		listInPane.add(c);
	}
	
	
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == myExpandButton) {
			if (expandPaneVisible) {
				setCollapsed(false);
				expandPaneVisible = false;
			}
			else {
				setCollapsed(true);
				expandPaneVisible = true;
			}
		}
	}

	private JPanel createButton(boolean state, String name) {
		JPanel collapsePanel = new JPanel(new BorderLayout(0, 2));
		String label = null;
		myExpandButton = null;
		collapseName = "<html><b><i>&nbsp;&nbsp;Show "+name+"</i></b></html>";
		expandName = "<html><b><i>&nbsp;&nbsp;Hide "+name+"</i></b></html>";

		if (state) {
			myExpandButton = new JButton(collapseName, UIManager.getIcon("Tree.collapsedIcon"));
		} else {
			myExpandButton = new JButton(expandName, UIManager.getIcon("Tree.expandedIcon"));
		}
		myExpandButton.addActionListener(this);
		collapsePanel.add(myExpandButton, BorderLayout.LINE_START);
		return collapsePanel;
	}
	
	
	public void setCollapsed(boolean visible) {
		if (visible) {
			expandPanel();
			myExpandButton.setIcon(UIManager.getIcon("Tree.expandedIcon"));
			myExpandButton.setText(expandName);
		}
		else {
			collapsePanel();
			myExpandButton.setIcon(UIManager.getIcon("Tree.collapsedIcon"));
			myExpandButton.setText(collapseName);
		}
	}
	

	public void setButtonChanges(boolean value) {
		if (value) {
			myExpandButton.setIcon(UIManager.getIcon("Tree.collapsedIcon"));
			myExpandButton.setText(collapseName);
		} else {
			myExpandButton.setIcon(UIManager.getIcon("Tree.expandedIcon"));
			myExpandButton.setText(expandName);
		}
		
	}
	
	
	public boolean isCollapsed() {
		return expandPaneVisible;
	}
	

	private void collapsePanel() {
		leftPanel.removeAll();
		repackEnclosingDialog();
	}
		
		
	private void expandPanel() {
		for (Component c : listInPane)
			leftPanel.add(c);

		repackEnclosingDialog();
	}


	/**
	 * Attempts to locate the instance of the enclosing JDialog.  If successful we will call the pack() method on it.
	 */
	private void repackEnclosingDialog() {
		Container container = getParent();
		while (container != null && !(container instanceof JDialog))
			container = container.getParent();
		if (container != null)
			((JDialog)container).pack();
	}
}
