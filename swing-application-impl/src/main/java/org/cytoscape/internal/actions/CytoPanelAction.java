package org.cytoscape.internal.actions;

import static org.cytoscape.internal.view.CytoPanelNameInternal.BOTTOM;
import static org.cytoscape.internal.view.CytoPanelStateInternal.DOCK;
import static org.cytoscape.internal.view.CytoPanelStateInternal.HIDE;
import static org.cytoscape.internal.view.CytoPanelStateInternal.UNDOCK;

import java.awt.event.ActionEvent;

import javax.swing.event.MenuEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.internal.view.CytoPanelImpl;
import org.cytoscape.internal.view.CytoPanelNameInternal;
import org.cytoscape.internal.view.CytoPanelStateInternal;
import org.cytoscape.internal.view.CytoscapeDesktop;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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
public class CytoPanelAction extends AbstractCyAction {
	
	private final CytoPanelNameInternal position;
	private final CytoscapeDesktop desktop;

	public CytoPanelAction(
			CytoPanelNameInternal position,
			CytoscapeDesktop desktop,
			float menuGravity
	) {
		super("Show " + position.getTitle());

		this.position = position;
		this.desktop = desktop;
		
		setPreferredMenu("View");
		setMenuGravity(menuGravity);
		useCheckBoxMenuItem = true;
	}

	/**
	 * Toggles the cytopanel state.  
	 */
	@Override
	public void actionPerformed(ActionEvent evt) {
		CytoPanelImpl cytoPanel = (CytoPanelImpl) desktop.getCytoPanel(position);
		CytoPanelStateInternal state = cytoPanel.getStateInternal();
		final CytoPanelStateInternal newState;
		
		if (state == HIDE)
			newState = cytoPanel.getCytoPanelNameInternal() == BOTTOM ? UNDOCK : DOCK;
		else
			newState = HIDE;
		
		cytoPanel.setStateInternal(newState);
	} 

	@Override
	public void menuSelected(MenuEvent evt) {
		updateEnableState();
		CytoPanel cytoPanel = desktop.getCytoPanel(position);
		boolean select = cytoPanel.getCytoPanelComponentCount() > 0
				&& ((CytoPanelImpl) cytoPanel).getStateInternal() != HIDE;

		putValue(SELECTED_KEY, select);
	}
	
	@Override
	public void updateEnableState() {
		CytoPanel cytoPanel = desktop.getCytoPanel(position);
		setEnabled(cytoPanel instanceof CytoPanelImpl && cytoPanel.getThisComponent() != null
				&& cytoPanel.getCytoPanelComponentCount() > 0);
	}
}
