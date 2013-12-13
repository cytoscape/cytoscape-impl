package org.cytoscape.filter.internal.composite;

import javax.swing.JPanel;

import org.cytoscape.filter.internal.view.ViewUtil;

@SuppressWarnings("serial")
public class CompositeSeparator extends JPanel {
	public CompositeSeparator() {
		setBackground(ViewUtil.UNSELECTED_BACKGROUND_COLOR);
	}
}
