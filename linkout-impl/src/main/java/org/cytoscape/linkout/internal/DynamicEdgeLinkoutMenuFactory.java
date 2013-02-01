package org.cytoscape.linkout.internal;

/*
 * #%L
 * Cytoscape Linkout Impl (linkout-impl)
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



import javax.swing.JMenuItem;

import org.cytoscape.application.swing.CyEdgeViewContextMenuFactory;
import org.cytoscape.application.swing.CyMenuItem;
import org.cytoscape.model.CyEdge;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import static org.cytoscape.work.ServiceProperties.*;
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
			CyMenuItem menuItem = new CyMenuItem(new JMenuItem(EDGE_DYNAMIC_LINKOUTS_MENU), 1050); // Gravity comes from ServiceProperties
			support.createSubMenus(menuItem, netView.getModel(),edgeView.getModel());
			return menuItem;
	}
}
