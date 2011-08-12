package org.cytoscape.editor.internal;

import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.view.model.CyNetworkView;

public class CurrentNetworkViewListener implements  SetCurrentNetworkViewListener {

	private final CySwingApplication app;
	private final CytoPanelComponent comp;

	public CurrentNetworkViewListener(CySwingApplication app, CytoPanelComponent comp) {
		this.app = app;
		this.comp = comp;
	}

	public void handleEvent(SetCurrentNetworkViewEvent e) {
		CyNetworkView view = e.getNetworkView();
		if ( view != null && view.getModel().getNodeCount() == 0 ) {
			CytoPanel west = app.getCytoPanel(CytoPanelName.WEST);
			int compIndex = west.indexOfComponent(comp.getComponent());
			west.setSelectedIndex( compIndex );
		}
	}
}
