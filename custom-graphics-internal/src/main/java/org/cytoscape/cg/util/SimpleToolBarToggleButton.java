package org.cytoscape.cg.util;

import javax.swing.Icon;
import javax.swing.JToggleButton;

import org.cytoscape.cg.internal.util.ViewUtil;

@SuppressWarnings("serial")
public class SimpleToolBarToggleButton extends JToggleButton {

	public SimpleToolBarToggleButton(String text) {
		this(text, false);
	}
	
	public SimpleToolBarToggleButton(Icon icon) {
		this(icon, false);
	}

	public SimpleToolBarToggleButton(String text, Boolean selected) {
		super(text, selected);
		addActionListener(evt -> update());
	}
	
	public SimpleToolBarToggleButton(Icon icon, Boolean selected) {
		super(icon, selected);
		addActionListener(evt -> update());
	}

	@Override
	public void setSelected(boolean b) {
		if (b != isSelected()) {
			super.setSelected(b);
			update();
		}
	}
	
	protected void update() {
		ViewUtil.updateToolBarStyle(this);
	}
}
