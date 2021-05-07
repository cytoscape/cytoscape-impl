package org.cytoscape.internal.actions;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.KeyStroke;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.util.swing.LookAndFeelUtil;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class FullScreenAction extends AbstractCyAction {

	private static final long serialVersionUID = 2987814408730103803L;

	private static final String MENU_NAME = "Full Screen Mode";

	protected final CySwingApplication desktop;

	protected boolean inFullScreenMode;

	private Rectangle lastBounds;

	public FullScreenAction(final CySwingApplication desktop) {
		this(desktop, MENU_NAME);
	}

	public FullScreenAction(final CySwingApplication desktop, final String menuName) {
		super(menuName);
		setPreferredMenu("View");
		setMenuGravity(5.1f);
		useCheckBoxMenuItem = true;
		this.desktop = desktop;
		
		final KeyStroke ks;
		
		if (LookAndFeelUtil.isMac())
			ks = KeyStroke.getKeyStroke(KeyEvent.VK_F,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + InputEvent.CTRL_DOWN_MASK);
		else // Windows and Linux
			ks = KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0);
			
		setAcceleratorKeyStroke(ks);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		toggle();
		inFullScreenMode = !inFullScreenMode;
	}

	protected void toggle() {
		JFrame frame = desktop.getJFrame();
		
		if (!inFullScreenMode)
			lastBounds = frame.getBounds();

		if (!LookAndFeelUtil.isMac()) {
			// Always dispose the frame first to prevent this error: 
			// "java.awt.IllegalComponentStateException: The frame is displayable."
			frame.dispose();
			
			if (inFullScreenMode) {
				// Return to normal mode...
				frame.setUndecorated(false);
				frame.setBounds(lastBounds);
				frame.setExtendedState(JFrame.NORMAL);
				frame.setVisible(true);
			} else {
				// Simulate full screen mode...
				frame.setUndecorated(true);
				frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
				frame.setVisible(true);
			}
		}
	}
}
