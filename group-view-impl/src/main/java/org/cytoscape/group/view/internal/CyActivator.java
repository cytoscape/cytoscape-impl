
package org.cytoscape.group.view.internal;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.service.util.CyServiceRegistrar;

import org.osgi.framework.BundleContext;

import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.events.GroupAboutToCollapseListener;
import org.cytoscape.group.events.GroupCollapsedListener;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;


import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;


public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		CyEventHelper cyEventHelperServiceRef = getService(bc,CyEventHelper.class);
		CyServiceRegistrar cyServiceRegistrarServiceRef = getService(bc,CyServiceRegistrar.class);
		CyGroupManager cyGroupManager = getService(bc,CyGroupManager.class);
		CyNetworkViewManager cyNetworkViewManager = getService(bc,CyNetworkViewManager.class);
		VisualMappingManager styleManager = getService(bc,VisualMappingManager.class);

		GroupViewCollapseHandler gvcHandler = 
			new GroupViewCollapseHandler(cyGroupManager, cyNetworkViewManager, styleManager);

		registerService(bc,gvcHandler,GroupAboutToCollapseListener.class, new Properties());
		registerService(bc,gvcHandler,GroupCollapsedListener.class, new Properties());
	}
}

