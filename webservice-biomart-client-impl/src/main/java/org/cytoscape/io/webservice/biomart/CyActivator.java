
package org.cytoscape.io.webservice.biomart;

import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.work.swing.DialogTaskManager;

import org.cytoscape.io.webservice.biomart.task.ShowBiomartGUIAction;
import org.cytoscape.io.webservice.biomart.BiomartClient;
import org.cytoscape.io.webservice.biomart.rest.BiomartRestClient;
import org.cytoscape.io.webservice.biomart.ui.BiomartAttrMappingPanel;

import org.cytoscape.application.swing.CyAction;


import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;


public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		CySwingApplication cySwingApplicationServiceRef = getService(bc,CySwingApplication.class);
		DialogTaskManager taskManagerServiceRef = getService(bc,DialogTaskManager.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc,CyNetworkManager.class);
		CyTableManager cyTableManagerServiceRef = getService(bc,CyTableManager.class);
		CyApplicationManager cyApplicationManagerServiceRef = getService(bc,CyApplicationManager.class);
		CyTableFactory cyTableFactoryServiceRef = getService(bc,CyTableFactory.class);
		CyRootNetworkManager cyRootNetworkFactoryServiceRef = getService(bc,CyRootNetworkManager.class);
		
		BiomartRestClient biomartRestClient = new BiomartRestClient("http://www.biomart.org/biomart/martservice");
		BiomartClient biomartClient = new BiomartClient("BioMart Client","REST version of BioMart Web Service Client.",biomartRestClient,cyTableFactoryServiceRef,cyNetworkManagerServiceRef,cyApplicationManagerServiceRef,cySwingApplicationServiceRef,cyTableManagerServiceRef,cyRootNetworkFactoryServiceRef);
		BiomartAttrMappingPanel biomartAttrMappingPanel = new BiomartAttrMappingPanel(biomartClient,taskManagerServiceRef,cyApplicationManagerServiceRef,cyTableManagerServiceRef,cyNetworkManagerServiceRef);
		ShowBiomartGUIAction showBiomartGUIAction = new ShowBiomartGUIAction(biomartAttrMappingPanel,biomartClient,taskManagerServiceRef,cyApplicationManagerServiceRef,cySwingApplicationServiceRef);
		
		registerService(bc,showBiomartGUIAction,CyAction.class, new Properties());
		registerAllServices(bc,biomartAttrMappingPanel, new Properties());
	}
}

