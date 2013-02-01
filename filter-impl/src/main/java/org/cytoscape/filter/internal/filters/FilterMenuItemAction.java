package org.cytoscape.filter.internal.filters;

/*
 * #%L
 * Cytoscape Filters Impl (filter-impl)
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
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.filter.internal.filters.view.FilterMainPanel;


/**
 *
 */
@SuppressWarnings("serial")
public class FilterMenuItemAction extends AbstractCyAction {
	
	private final CytoPanel cytoPanelWest;
	private final FilterMainPanel filterPanel;

	public FilterMenuItemAction(CySwingApplication application, FilterMainPanel filterPanel) {
		super("Use Filters");
		setPreferredMenu("Select");
		setToolbarGravity(9.9f);
		putValue(SHORT_DESCRIPTION, "Use Filters"); //tooltip

		ImageIcon icon = new ImageIcon(getClass().getResource("/images/filter.png"));
		ImageIcon smallIcon = new ImageIcon(getClass().getResource("/images/filter-small.png"));
		
		putValue(LARGE_ICON_KEY, icon);
		putValue(SMALL_ICON, smallIcon);
		setAcceleratorKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
		cytoPanelWest = application.getCytoPanel(CytoPanelName.WEST);
		this.filterPanel = filterPanel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// If the state of the cytoPanelEast is HIDE, show it
		if (cytoPanelWest.getState() == CytoPanelState.HIDE) {
			cytoPanelWest.setState(CytoPanelState.DOCK);
		}	

		// Select the filter panel
		int index = cytoPanelWest.indexOfComponent(filterPanel);
		if (index == -1) {
			return;
		}
		
		cytoPanelWest.setSelectedIndex(index);
	}

	public boolean isInToolBar() {
		return false;
	}

	public boolean isInMenuBar() {
		return true;
	}
}
