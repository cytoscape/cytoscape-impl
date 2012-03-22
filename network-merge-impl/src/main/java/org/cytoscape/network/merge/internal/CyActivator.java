
package org.cytoscape.network.merge.internal;

import org.cytoscape.work.TaskManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetworkFactory;

import org.cytoscape.network.merge.internal.NetworkMergeAction;

import org.cytoscape.application.swing.CyAction;
import org.cytoscape.task.creation.NetworkViewCreator;


import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		CyNetworkNaming cyNetworkNamingServiceRef = getService(bc,CyNetworkNaming.class);
		CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc,CyNetworkFactory.class);
		NetworkViewCreator netViewCreator = getService(bc,NetworkViewCreator.class);
		TaskManager taskManagerServiceRef = getService(bc,TaskManager.class);
		CySwingApplication cySwingApplicationServiceRef = getService(bc,CySwingApplication.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc,CyNetworkManager.class);
		
		NetworkMergeAction networkMergeAction = new NetworkMergeAction(cySwingApplicationServiceRef,cyNetworkManagerServiceRef,cyNetworkFactoryServiceRef,cyNetworkNamingServiceRef,taskManagerServiceRef,netViewCreator);
		
		registerService(bc,networkMergeAction,CyAction.class, new Properties());
	}
}

