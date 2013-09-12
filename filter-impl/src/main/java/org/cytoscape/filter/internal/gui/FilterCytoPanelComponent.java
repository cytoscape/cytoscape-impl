package org.cytoscape.filter.internal.gui;

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

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.SwingUtilities;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedEvent;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedListener;
import org.cytoscape.filter.internal.filters.view.FilterMainPanel;

public class FilterCytoPanelComponent implements CytoPanelComponent, CytoPanelComponentSelectedListener {

	FilterMainPanel panel;
	
	public FilterCytoPanelComponent(FilterMainPanel panel) {
		this.panel = panel; 
	}
	
	@Override
	public Component getComponent() {
		return panel;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.WEST;
	}

	@Override
	public String getTitle() {
		return "Filter";
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public void handleEvent(final CytoPanelComponentSelectedEvent e) {
		SwingUtilities.invokeLater( new Runnable() {
			
		@Override
		public void run(){
		if (e.getCytoPanel().getSelectedComponent() == panel) {
			panel.handlePanelSelected();
		}
		}});
	}
}
