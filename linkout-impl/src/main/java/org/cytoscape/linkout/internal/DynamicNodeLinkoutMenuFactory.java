package org.cytoscape.linkout.internal;


import javax.swing.JMenu;

import org.cytoscape.model.CyNode;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.application.swing.CyMenuItem;
import org.cytoscape.application.swing.CyNodeViewContextMenuFactory;


public class DynamicNodeLinkoutMenuFactory implements CyNodeViewContextMenuFactory  {

	private OpenBrowser browser;
	final SynchronousTaskManager synTaskManager;
 
	public DynamicNodeLinkoutMenuFactory(OpenBrowser browser, final SynchronousTaskManager synTaskManager) {
		this.browser = browser;
		this.synTaskManager = synTaskManager;
	}

	@Override
	public CyMenuItem createMenuItem(CyNetworkView netView,
			View<CyNode> nodeView) {
		DynamicSupport support = new DynamicSupport(browser, synTaskManager);
		CyMenuItem menuItem = new CyMenuItem(new JMenu("LiknOut Dynamic"), 0);
		support.createSubMenus(menuItem, netView.getModel(),nodeView.getModel());
		return menuItem;
	}
}
