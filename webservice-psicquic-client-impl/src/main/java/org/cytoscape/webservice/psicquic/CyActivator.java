package org.cytoscape.webservice.psicquic;

import java.util.Properties;

import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.create.CreateNetworkViewTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		OpenBrowser openBrowser = getService(bc, OpenBrowser.class);
		
		DialogTaskManager tm = getService(bc, DialogTaskManager.class);
		CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc, CyNetworkFactory.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc, CyNetworkManager.class);

		CreateNetworkViewTaskFactory createViewTaskFactoryServiceRef = getService(bc, CreateNetworkViewTaskFactory.class);

		PSICQUICWebServiceClient psicquicClient = new PSICQUICWebServiceClient(
				"http://www.ebi.ac.uk/Tools/webservices/psicquic/registry/registry", "PSICQUIC Universal Client",
				"REST version of PSICQUIC web service client.", cyNetworkFactoryServiceRef, cyNetworkManagerServiceRef,
				tm, createViewTaskFactoryServiceRef, openBrowser);

		registerAllServices(bc, psicquicClient, new Properties());
	}
}
