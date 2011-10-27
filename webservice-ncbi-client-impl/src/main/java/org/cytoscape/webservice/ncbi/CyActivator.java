



package org.cytoscape.webservice.ncbi;

import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.work.swing.DialogTaskManager;

import org.cytoscape.webservice.ncbi.NCBIWebServiceClient;
import org.cytoscape.webservice.ncbi.NCBITableImportAction;
import org.cytoscape.webservice.ncbi.NCBITableImportClient;

import org.cytoscape.application.swing.CyAction;


import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		CyApplicationManager cyApplicationManagerRef = getService(bc,CyApplicationManager.class);
		CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc,CyNetworkFactory.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc,CyNetworkManager.class);
		DialogTaskManager taskManagerServiceRef = getService(bc,DialogTaskManager.class);
		CyTableManager cyTableManagerServiceRef = getService(bc,CyTableManager.class);
		CyTableFactory cyDataTableFactoryServiceRef = getService(bc,CyTableFactory.class);
		CyApplicationManager cyApplicationManagerServiceRef = getService(bc,CyApplicationManager.class);
		
		NCBIWebServiceClient ncbiClient = new NCBIWebServiceClient("http://www.ncbi.nlm.nih.gov/entrez/eutils/soap/v2.0/eutils.wsdl","NCBI Network Import Client","REST version of NCBI Web Service Client.",cyNetworkFactoryServiceRef,cyDataTableFactoryServiceRef,cyNetworkManagerServiceRef,cyTableManagerServiceRef);
		NCBITableImportClient ncbiTableImportClient = new NCBITableImportClient("http://www.ncbi.nlm.nih.gov/entrez/eutils/soap/v2.0/eutils.wsdl","NCBI Table Import Client","REST version of NCBI Web Service Client for importing tables.",cyDataTableFactoryServiceRef,cyNetworkManagerServiceRef,cyApplicationManagerServiceRef,cyTableManagerServiceRef);
		NCBITableImportAction ncbiTableImportAction = new NCBITableImportAction(ncbiTableImportClient,taskManagerServiceRef,cyNetworkManagerServiceRef,cyApplicationManagerRef);
		
		registerAllServices(bc,ncbiClient, new Properties());
		registerService(bc,ncbiTableImportAction,CyAction.class, new Properties());
	}
}

