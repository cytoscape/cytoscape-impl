package org.cytoscape.filter.internal;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Icon;

import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.filter.internal.view.FilterPanel;
import org.cytoscape.filter.internal.view.FilterPanelController;
import org.cytoscape.filter.internal.view.SelectPanel;
import org.cytoscape.filter.internal.view.TransformerPanel;
import org.cytoscape.filter.internal.view.TransformerViewManager;
import org.cytoscape.filter.view.InteractivityChangedListener;

/*
 * #%L
 * Cytoscape Filters 2 Impl (filter2-impl)
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

public class FilterCytoPanelComponent implements CytoPanelComponent2 {

	private static final String ID = "org.cytoscape.Filter";
	
	SelectPanel panel;

	public FilterCytoPanelComponent(TransformerViewManager transformerViewManager, ModelMonitor modelMonitor,
			final FilterPanel filterPanel, TransformerPanel transformerPanel) {
		filterPanel.setPreferredSize(new Dimension(450, 300));
		
		modelMonitor.addInteractivityChangedListener(new InteractivityChangedListener() {
			@Override
			public void handleInteractivityChanged(boolean isInteractive) {
				FilterPanelController filterPanelController = filterPanel.getController();
				filterPanelController.setInteractive(isInteractive, filterPanel);
			}
		});
		
		
		panel = new SelectPanel(filterPanel, transformerPanel);
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
		return "Select";
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public String getIdentifier() {
		return ID;
	}
}
