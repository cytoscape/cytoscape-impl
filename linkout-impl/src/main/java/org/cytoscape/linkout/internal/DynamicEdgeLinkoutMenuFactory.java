package org.cytoscape.linkout.internal;


import javax.swing.JMenuItem;

import org.cytoscape.application.swing.CyEdgeViewContextMenuFactory;
import org.cytoscape.application.swing.CyMenuItem;
import org.cytoscape.model.CyEdge;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.SynchronousTaskManager;


public class DynamicEdgeLinkoutMenuFactory implements CyEdgeViewContextMenuFactory {

	private OpenBrowser browser;
	final SynchronousTaskManager synTaskManager;

	public DynamicEdgeLinkoutMenuFactory(OpenBrowser browser, final SynchronousTaskManager synTaskManager) {
		this.browser = browser;
		this.synTaskManager = synTaskManager;
	}

	@Override
	public CyMenuItem createMenuItem(CyNetworkView netView,
			View<CyEdge> edgeView) {
			DynamicSupport support = new DynamicSupport(browser, synTaskManager);
			CyMenuItem menuItem = new CyMenuItem(new JMenuItem("LiknOut Dynamic"), 0);
			support.createSubMenus(menuItem, netView.getModel(),edgeView.getModel());
			return menuItem;
	}
}
