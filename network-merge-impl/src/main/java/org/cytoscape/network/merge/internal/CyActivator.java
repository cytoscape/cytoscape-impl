package org.cytoscape.network.merge.internal;

import java.util.Properties;

import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.create.CreateNetworkViewTaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		CyNetworkNaming cyNetworkNamingServiceRef = getService(bc, CyNetworkNaming.class);
		CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc, CyNetworkFactory.class);
		CreateNetworkViewTaskFactory netViewCreator = getService(bc, CreateNetworkViewTaskFactory.class);
		DialogTaskManager taskManagerServiceRef = getService(bc, DialogTaskManager.class);
		CySwingApplication cySwingApplicationServiceRef = getService(bc, CySwingApplication.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc, CyNetworkManager.class);

		NetworkMergeAction networkMergeAction = new NetworkMergeAction(cySwingApplicationServiceRef,
				cyNetworkManagerServiceRef, cyNetworkFactoryServiceRef, cyNetworkNamingServiceRef,
				taskManagerServiceRef, netViewCreator);

		registerService(bc, networkMergeAction, CyAction.class, new Properties());
	}
}
