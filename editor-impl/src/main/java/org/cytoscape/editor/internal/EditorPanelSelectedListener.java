package org.cytoscape.editor.internal;

import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedListener;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedEvent;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.task.creation.NewEmptyNetworkViewFactory; 

public class EditorPanelSelectedListener implements  CytoPanelComponentSelectedListener {

	private final CySwingApplication app;
	private final CytoPanelComponent comp;
	private final CyNetworkManager netMgr;
	private final NewEmptyNetworkViewFactory viewFactory; 

	public EditorPanelSelectedListener(CySwingApplication app, CytoPanelComponent comp, CyNetworkManager netMgr, NewEmptyNetworkViewFactory viewFactory) {
		this.app = app;
		this.comp = comp;
		this.netMgr = netMgr;
		this.viewFactory = viewFactory;
	}

	public void handleEvent(CytoPanelComponentSelectedEvent e) {
		CytoPanel west = app.getCytoPanel(CytoPanelName.WEST);
		if ( west != e.getCytoPanel() )
			return;

		int compIndex = west.indexOfComponent(comp.getComponent());
		if ( compIndex == e.getSelectedIndex() ) {
			if ( netMgr.getNetworkSet().size() == 0 ) {
				CyNetworkView view = viewFactory.createNewEmptyNetworkView();
			}
		}
	}
}
