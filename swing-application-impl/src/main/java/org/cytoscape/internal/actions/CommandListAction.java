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

import java.awt.event.ActionEvent;

import javax.swing.SwingUtilities;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.command.AvailableCommands;

/**
 * Display a list of all available commands
 */
public final class CommandListAction extends AbstractCyAction {

	private static final long serialVersionUID = 8750641831904687541L;
	
	private final CySwingApplication swingApp;
	private final AvailableCommands availableCommands;

	public CommandListAction(CySwingApplication swingApp, AvailableCommands availableCommands) {
		super("List Available Commands...");
		setPreferredMenu("Help");
		setMenuGravity(100000f);
		this.swingApp = swingApp;
		this.availableCommands = availableCommands;
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		SwingUtilities.invokeLater(() -> {
            final CommandListUI ui= new CommandListUI(availableCommands);
            ui.setLocationRelativeTo(swingApp.getJFrame());
            ui.setVisible(true);
        });
	}
}
