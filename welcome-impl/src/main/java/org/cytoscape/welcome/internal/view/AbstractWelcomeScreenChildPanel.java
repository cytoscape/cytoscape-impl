package org.cytoscape.welcome.internal.view;

/*
 * #%L
 * Cytoscape Welcome Screen Impl (welcome-impl)
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

import java.awt.Window;

import javax.swing.JPanel;

import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class AbstractWelcomeScreenChildPanel extends JPanel implements WelcomeScreenChildPanel {
	
	protected Window window;

	protected AbstractWelcomeScreenChildPanel(final String title) {
		setBorder(LookAndFeelUtil.createTitledBorder(title));
	}
	
	@Override
	public void closeParentWindow() {
		if (window != null)
			window.dispose();
	}

	@Override
	public void setParentWindow(final Window window) {
		this.window = window;
	}
}
