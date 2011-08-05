package org.cytoscape.filter.internal;

import org.cytoscape.application.swing.events.CytoPanelComponentSelectedEvent;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedListener;
import org.cytoscape.filter.internal.filters.view.FilterMainPanel;

public class FilterPanelSelectedListener implements CytoPanelComponentSelectedListener {
	private FilterMainPanel filterPanel;

	public FilterPanelSelectedListener(FilterMainPanel filterPanel) {
		this.filterPanel = filterPanel;
	}
	
	@Override
	public void handleEvent(CytoPanelComponentSelectedEvent e) {
		if (e.getCytoPanel().getSelectedComponent() == filterPanel) {
			filterPanel.handlePanelSelected();
		}
	}
}
