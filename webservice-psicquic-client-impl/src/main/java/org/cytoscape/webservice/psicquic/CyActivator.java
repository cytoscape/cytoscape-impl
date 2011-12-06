package org.cytoscape.webservice.psicquic;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		CyNetworkNaming namingService = getService(bc, CyNetworkNaming.class);
		DialogTaskManager tm = getService(bc, DialogTaskManager.class);
		CyApplicationManager cyApplicationManagerRef = getService(bc, CyApplicationManager.class);
		CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc, CyNetworkFactory.class);
		CyNetworkViewFactory cyNetworkViewFactoryServiceRef = getService(bc, CyNetworkViewFactory.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc, CyNetworkManager.class);
		CyNetworkViewManager cyNetworkViewManagerServiceRef = getService(bc, CyNetworkViewManager.class);

		NetworkTaskFactory createViewTaskFactoryServiceRef = getService(bc, NetworkTaskFactory.class,
				"(id=createNetworkViewTaskFactory)");

		DialogTaskManager taskManagerServiceRef = getService(bc, DialogTaskManager.class);
		CyTableManager cyTableManagerServiceRef = getService(bc, CyTableManager.class);
		CyTableFactory cyDataTableFactoryServiceRef = getService(bc, CyTableFactory.class);
		CyApplicationManager cyApplicationManagerServiceRef = getService(bc, CyApplicationManager.class);
		CyRootNetworkManager cyRootNetworkFactoryServiceRef = getService(bc, CyRootNetworkManager.class);

		PSICQUICWebServiceClient psicquicClient = new PSICQUICWebServiceClient(
				"http://www.ebi.ac.uk/Tools/webservices/psicquic/registry/registry", "PSICQUIC Universal Client",
				"REST version of PSICQUIC web service client.", cyNetworkFactoryServiceRef,
				cyNetworkViewFactoryServiceRef, cyNetworkViewManagerServiceRef, cyNetworkManagerServiceRef, tm,
				createViewTaskFactoryServiceRef);

		registerAllServices(bc, psicquicClient, new Properties());
	}
}
