package org.cytoscape.internal.actions;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
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

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.KeyStroke;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;

public class FullScreenAction extends AbstractCyAction {

	private static final long serialVersionUID = 2987814408730103803L;

	private static final String MENU_NAME = "Maximize Inner Desktop";
	protected static final boolean IS_MAC = System.getProperty("os.name").startsWith("Mac OS X");

	protected final CySwingApplication desktop;

	protected boolean inFullScreenMode = false;

	private final Set<CytoPanel> panels;
	private final Map<CytoPanel, CytoPanelState> states;
	private Rectangle lastBounds;

	public FullScreenAction(final CySwingApplication desktop) {
		this(desktop, MENU_NAME);
	}

	public FullScreenAction(final CySwingApplication desktop, final String menuName) {
		super(menuName);
		setPreferredMenu("View");
		setMenuGravity(5.1f);
		this.useCheckBoxMenuItem = true;
		setAcceleratorKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit()
				.getMenuShortcutKeyMask()));
		this.desktop = desktop;

		panels = new HashSet<>();
		states = new HashMap<>();
		panels.add(desktop.getCytoPanel(CytoPanelName.WEST));
		panels.add(desktop.getCytoPanel(CytoPanelName.EAST));
		panels.add(desktop.getCytoPanel(CytoPanelName.SOUTH));
		panels.add(desktop.getCytoPanel(CytoPanelName.SOUTH_WEST));
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		toggle();
		inFullScreenMode = !inFullScreenMode;
	}

	protected void toggle() {
		if (inFullScreenMode) {
			desktop.getJToolBar().setVisible(true);
			desktop.getStatusToolBar().setVisible(true);
		} else {
			lastBounds = desktop.getJFrame().getBounds();
			desktop.getJToolBar().setVisible(false);
			desktop.getStatusToolBar().setVisible(false);
		}

		for (CytoPanel panel : panels) {
			final CytoPanelState curState = panel.getState();

			if (!inFullScreenMode) {
				// Save current State
				states.put(panel, curState);
				if (curState != CytoPanelState.HIDE)
					panel.setState(CytoPanelState.HIDE);
			} else {
				final CytoPanelState lastState = states.get(panel);
				panel.setState(lastState);
			}
		}

		if (!IS_MAC) {
			if (inFullScreenMode) {
				desktop.getJFrame().setBounds(lastBounds);
				desktop.getJFrame().setExtendedState(JFrame.NORMAL);
			} else
				desktop.getJFrame().setExtendedState(JFrame.MAXIMIZED_BOTH);
		}
	}
}
