package org.cytoscape.internal.view;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2013 The Cytoscape Consortium
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

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.application.swing.events.CytoPanelStateChangedEvent;
import org.cytoscape.application.swing.events.CytoPanelStateChangedListener;

/**
 * This class handles the embedding of the Tools CytoPanel within the Control
 * CytoPanel. For all other cytopanels this is handled by CytoPanelAction, but
 * because tools panel is within another cytopanel, we have to handle things
 * separately.
 */
public class ToolCytoPanelListener implements CytoPanelStateChangedListener {

	private final BiModalJSplitPane split;
	private final CytoPanel southWest;
	private final CytoPanelImp west;

	public ToolCytoPanelListener(BiModalJSplitPane split, CytoPanelImp west, CytoPanel southWest) {
		this.split = split;
		this.west = west;
		this.southWest = southWest;
	}

	public void handleEvent(CytoPanelStateChangedEvent e) {
		if (e.getCytoPanel() != southWest)
			return;

		final CytoPanelState newState = e.getNewState();
		
		SwingUtilities.invokeLater(() -> {
            if (newState == CytoPanelState.DOCK)
                west.addComponentToSouth(split);
            else
                west.addComponentToSouth(new JLabel());

            west.validate();
        });
	}
}
