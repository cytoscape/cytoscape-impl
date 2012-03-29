package org.cytoscape.browser.internal;

import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.work.swing.DialogTaskManager;

import org.cytoscape.browser.internal.AbstractTableBrowser;
import org.cytoscape.browser.internal.PopupMenuHelper;


import org.cytoscape.task.TableCellTaskFactory;
import org.cytoscape.task.TableColumnTaskFactory;
import org.cytoscape.task.table.DeleteTableTaskFactory;
import org.cytoscape.task.table.MapGlobalToLocalTableTaskFactory;

import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		CyTableManager cyTableManagerServiceRef = getService(bc,CyTableManager.class);
		CyServiceRegistrar cyServiceRegistrarServiceRef = getService(bc,CyServiceRegistrar.class);
		EquationCompiler compilerServiceRef = getService(bc,EquationCompiler.class);
		OpenBrowser openBrowserServiceRef = getService(bc,OpenBrowser.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc,CyNetworkManager.class);
		DeleteTableTaskFactory deleteTableTaskFactoryService = getService(bc, DeleteTableTaskFactory.class);
		DialogTaskManager guiTaskManagerServiceRef = getService(bc,DialogTaskManager.class);
		CyApplicationManager cyApplicationManagerServiceRef = getService(bc,CyApplicationManager.class);
		CyNetworkTableManager cyNetworkTableManagerServiceRef = getService(bc,CyNetworkTableManager.class);
		MapGlobalToLocalTableTaskFactory mapGlobalTableTaskFactoryServiceRef = getService(bc,MapGlobalToLocalTableTaskFactory.class);
		
		CyEventHelper cyEventHelperServiceRef = getService(bc,CyEventHelper.class);

		PopupMenuHelper popupMenuHelper = new PopupMenuHelper(guiTaskManagerServiceRef);
		
		AbstractTableBrowser nodeTableBrowser = new DefaultTableBrowser("Node Table", CyNode.class, cyTableManagerServiceRef,cyNetworkTableManagerServiceRef,cyServiceRegistrarServiceRef,compilerServiceRef,openBrowserServiceRef,cyNetworkManagerServiceRef,deleteTableTaskFactoryService,guiTaskManagerServiceRef,popupMenuHelper,cyApplicationManagerServiceRef, cyEventHelperServiceRef, mapGlobalTableTaskFactoryServiceRef);
		AbstractTableBrowser edgeTableBrowser = new DefaultTableBrowser("Edge Table", CyEdge.class, cyTableManagerServiceRef,cyNetworkTableManagerServiceRef,cyServiceRegistrarServiceRef,compilerServiceRef,openBrowserServiceRef,cyNetworkManagerServiceRef,deleteTableTaskFactoryService,guiTaskManagerServiceRef,popupMenuHelper,cyApplicationManagerServiceRef, cyEventHelperServiceRef, mapGlobalTableTaskFactoryServiceRef);
		AbstractTableBrowser networkTableBrowser = new DefaultTableBrowser("Network Table", CyNetwork.class, cyTableManagerServiceRef,cyNetworkTableManagerServiceRef,cyServiceRegistrarServiceRef,compilerServiceRef,openBrowserServiceRef,cyNetworkManagerServiceRef,deleteTableTaskFactoryService,guiTaskManagerServiceRef,popupMenuHelper,cyApplicationManagerServiceRef, cyEventHelperServiceRef, mapGlobalTableTaskFactoryServiceRef);
		
		AbstractTableBrowser globalTableBrowser = new GlobalTableBrowser("Global Table", cyTableManagerServiceRef,cyNetworkTableManagerServiceRef,cyServiceRegistrarServiceRef,compilerServiceRef,openBrowserServiceRef,cyNetworkManagerServiceRef,deleteTableTaskFactoryService,guiTaskManagerServiceRef,popupMenuHelper,cyApplicationManagerServiceRef, cyEventHelperServiceRef, mapGlobalTableTaskFactoryServiceRef);

		
		registerAllServices(bc,nodeTableBrowser, new Properties());
		registerAllServices(bc,edgeTableBrowser, new Properties());
		registerAllServices(bc,networkTableBrowser, new Properties());
		registerAllServices(bc,globalTableBrowser, new Properties());

		registerServiceListener(bc,popupMenuHelper,"addTableColumnTaskFactory","removeTableColumnTaskFactory",TableColumnTaskFactory.class);
		registerServiceListener(bc,popupMenuHelper,"addTableCellTaskFactory","removeTableCellTaskFactory",TableCellTaskFactory.class);


	}
}

