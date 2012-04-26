package org.cytoscape.filter.internal.gui;

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
		return "Filters";
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
