package org.cytoscape.webservice.psicquic;

import java.util.Properties;

import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		DialogTaskManager tm = getService(bc, DialogTaskManager.class);
		CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc, CyNetworkFactory.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc, CyNetworkManager.class);

		NetworkTaskFactory createViewTaskFactoryServiceRef = getService(bc, NetworkTaskFactory.class,
				"(id=createNetworkViewTaskFactory)");

		PSICQUICWebServiceClient psicquicClient = new PSICQUICWebServiceClient(
				"http://www.ebi.ac.uk/Tools/webservices/psicquic/registry/registry", "PSICQUIC Universal Client",
				"REST version of PSICQUIC web service client.", cyNetworkFactoryServiceRef, cyNetworkManagerServiceRef,
				tm, createViewTaskFactoryServiceRef);

		registerAllServices(bc, psicquicClient, new Properties());
	}
}
