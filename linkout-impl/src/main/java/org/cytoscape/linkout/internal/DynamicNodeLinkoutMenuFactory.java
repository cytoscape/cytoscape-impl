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



import javax.swing.JMenu;

import org.cytoscape.model.CyNode;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import static org.cytoscape.work.ServiceProperties.*;
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
		CyMenuItem menuItem = new CyMenuItem(new JMenu(NODE_DYNAMIC_LINKOUTS_MENU), 1100); // Gravity from ServiceProperties
		support.createSubMenus(menuItem, netView.getModel(),nodeView.getModel());
		return menuItem;
	}
}
