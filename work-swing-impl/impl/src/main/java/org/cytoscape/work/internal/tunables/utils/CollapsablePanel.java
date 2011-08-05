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
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;


public class CollapsablePanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private JToggleButton myExpandButton = null;
	private boolean expandPaneVisible;
	private static String ExpandName = "+";
	private static String CollapseName = "-";
	private JPanel rightPanel = new JPanel();
	private JPanel leftPanel = new JPanel();	
	private List<Component> listInPane;
	
	
	public CollapsablePanel(final String name, final boolean initialState) {
		expandPaneVisible = initialState;
		
		listInPane = new ArrayList<Component>();
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());
		TitledBorder titleborder = BorderFactory.createTitledBorder(name);
		titleborder.setTitleColor(Color.RED);
		setBorder(titleborder);
		//setBorder(BorderFactory.createTitledBorder(name));
		rightPanel.add(myExpandButton = createButton(expandPaneVisible), BorderLayout.WEST);
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
				collapsePanel();
				myExpandButton.setText(ExpandName);
				expandPaneVisible = false;
			}
			else {
				expandPanel();
				myExpandButton.setText(CollapseName);
				expandPaneVisible = true;
			}
		}
	}

	
	private JToggleButton createButton(boolean state) {
		JToggleButton button = new JToggleButton();
		if (state)
			button.setText(CollapseName);
		else
			button.setText(ExpandName);
		button.setPreferredSize(new Dimension(90, 20));
		button.setMargin(new Insets(2, 2, 2, 2));
		button.addActionListener(this);
		return button;
	}
	
	
	public void setCollapsed(boolean visible) {
		if (visible) {
			myExpandButton.setSelected(true);
			expandPanel();
			myExpandButton.setText(CollapseName);
		}
		else {
			myExpandButton.setSelected(false);
			collapsePanel();
			myExpandButton.setText(ExpandName);
		}
	}
	

	public void setButtonChanges(boolean value) {
		myExpandButton.setSelected(value);
		if (value)
			myExpandButton.setText(CollapseName);
		else
			myExpandButton.setText(ExpandName);
		
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