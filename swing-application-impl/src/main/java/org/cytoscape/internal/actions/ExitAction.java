package org.cytoscape.internal.actions;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.cytoscape.application.CyShutdown;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.service.util.CyServiceRegistrar;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

@SuppressWarnings("serial")
public class ExitAction extends AbstractCyAction {

	protected int returnVal;
	
	private final CyServiceRegistrar serviceRegistrar;

	public ExitAction(final CyServiceRegistrar serviceRegistrar) {
		super("Quit");
		this.serviceRegistrar = serviceRegistrar;
		setPreferredMenu("File");

		String osName = System.getProperty("os.name").toLowerCase();
		// boolean isMacOs = osName.startsWith("mac os x");

		if (osName.indexOf("mac") != -1) {
			// TODO: need to create special case for Mac to use apple key.
			// do mac-specific things here
			// setAcceleratorKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.META_MASK));
			setAcceleratorKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.VK_CONTROL));
		} else {
			setAcceleratorKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
		}
		
		setMenuGravity(1000.0f);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		serviceRegistrar.getService(CyShutdown.class).exit(returnVal);
	}
}
