package org.cytoscape.filter.internal.gui;

import java.awt.Component;
import java.util.Vector;

import javax.swing.Icon;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.filter.internal.filters.CompositeFilter;
import org.cytoscape.filter.internal.filters.view.FilterMainPanel;

public class FilterCytoPanelComponent implements CytoPanelComponent {

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
		return "Filters";
	}

	@Override
	public Icon getIcon() {
		return null;
	}

}
