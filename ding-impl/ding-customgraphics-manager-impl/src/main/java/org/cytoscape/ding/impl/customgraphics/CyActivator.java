



package org.cytoscape.ding.impl.customgraphics;

import org.cytoscape.property.CyProperty;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyApplicationConfiguration;

import org.cytoscape.ding.impl.customgraphics.CustomGraphicsManagerImpl;
import org.cytoscape.ding.impl.customgraphics.action.CustomGraphicsManagerAction;

import org.cytoscape.application.swing.CyAction;


import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		DialogTaskManager dialogTaskManagerServiceRef = getService(bc,DialogTaskManager.class);
		CyProperty coreCyPropertyServiceRef = getService(bc,CyProperty.class,"(cyPropertyName=cytoscape3.props)");
		CyApplicationManager cyApplicationManagerServiceRef = getService(bc,CyApplicationManager.class);
		CyApplicationConfiguration cyApplicationConfigurationServiceRef = getService(bc,CyApplicationConfiguration.class);
		
		CustomGraphicsManagerImpl customGraphicsManager = new CustomGraphicsManagerImpl(coreCyPropertyServiceRef,dialogTaskManagerServiceRef,cyApplicationConfigurationServiceRef);
		CustomGraphicsManagerAction customGraphicsManagerAction = new CustomGraphicsManagerAction(customGraphicsManager,cyApplicationManagerServiceRef);
		
		registerAllServices(bc,customGraphicsManager, new Properties());
		registerService(bc,customGraphicsManagerAction,CyAction.class, new Properties());
	}
}

