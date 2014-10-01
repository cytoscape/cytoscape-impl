package org.cytoscape.group.view.internal;

/*
 * #%L
 * Cytoscape Group View Impl (group-view-impl)
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

import org.osgi.framework.BundleContext;

import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.data.internal.CyGroupSettingsImpl;
import org.cytoscape.group.events.GroupAboutToCollapseListener;
import org.cytoscape.group.events.GroupCollapsedListener;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;

import static org.cytoscape.work.ServiceProperties.TITLE;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;

import static org.cytoscape.work.ServiceProperties.*;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		CyGroupManager cyGroupManager = getService(bc,CyGroupManager.class);
		CyNetworkViewManager cyNetworkViewManager = getService(bc,CyNetworkViewManager.class);
		CyNetworkViewFactory cyNetworkViewFactory = getService(bc,CyNetworkViewFactory.class);
		CyNetworkManager cyNetworkManager = getService(bc,CyNetworkManager.class);
		CyGroupSettingsImpl groupSettings = getService(bc,CyGroupSettingsImpl.class);
		VisualMappingManager styleManager = getService(bc, VisualMappingManager.class);

		GroupViewCollapseHandler gvcHandler = 
			new GroupViewCollapseHandler(cyGroupManager, groupSettings, cyNetworkManager,
			                             cyNetworkViewManager, cyNetworkViewFactory,
			                             styleManager);

		registerService(bc,gvcHandler,GroupAboutToCollapseListener.class, new Properties());
		registerService(bc,gvcHandler,GroupCollapsedListener.class, new Properties());

		// Listen for double-click
		GroupViewDoubleClickListener gvsListener =
			new GroupViewDoubleClickListener(cyGroupManager, groupSettings, cyNetworkViewManager, styleManager);

		Properties doubleClickProperties = new Properties();
		doubleClickProperties.setProperty(PREFERRED_ACTION, "OPEN");
		doubleClickProperties.setProperty(TITLE, "Expand/Collapse Group");
		registerService(bc,gvsListener,NodeViewTaskFactory.class, doubleClickProperties);
	}
}

