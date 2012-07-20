package org.cytoscape.welcome.internal.panel;

import java.awt.Window;

import javax.swing.JPanel;

public class AbstractWelcomeScreenChildPanel extends JPanel implements WelcomeScreenChildPanel {
	
	private static final long serialVersionUID = -2263794799072713710L;
	
	private Window window;

	@Override
	public void closeParentWindow() {
		if(window != null)
			window.dispose();
	}

	@Override
	public void setParentWindow(final Window window) {
		this.window = window;
	}

}
