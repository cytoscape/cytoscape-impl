package org.cytoscape.browser.internal;

import java.awt.event.ActionListener;
import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.TableAboutToBeDeletedListener;
import org.cytoscape.model.events.TableAddedListener;
import org.cytoscape.model.events.TablePrivacyChangedListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.task.TableCellTaskFactory;
import org.cytoscape.task.TableColumnTaskFactory;
import org.cytoscape.task.destroy.DeleteTableTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;

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
		
		CyEventHelper cyEventHelperServiceRef = getService(bc,CyEventHelper.class);

		PopupMenuHelper popupMenuHelper = new PopupMenuHelper(guiTaskManagerServiceRef, openBrowserServiceRef);
		
		AbstractTableBrowser nodeTableBrowser = new DefaultTableBrowser("Node Table", CyNode.class, cyTableManagerServiceRef,cyNetworkTableManagerServiceRef,cyServiceRegistrarServiceRef,compilerServiceRef,cyNetworkManagerServiceRef,deleteTableTaskFactoryService,guiTaskManagerServiceRef,popupMenuHelper,cyApplicationManagerServiceRef, cyEventHelperServiceRef);//, mapGlobalTableTaskFactoryServiceRef);
		AbstractTableBrowser edgeTableBrowser = new DefaultTableBrowser("Edge Table", CyEdge.class, cyTableManagerServiceRef,cyNetworkTableManagerServiceRef,cyServiceRegistrarServiceRef,compilerServiceRef,cyNetworkManagerServiceRef,deleteTableTaskFactoryService,guiTaskManagerServiceRef,popupMenuHelper,cyApplicationManagerServiceRef, cyEventHelperServiceRef);//, mapGlobalTableTaskFactoryServiceRef);
		AbstractTableBrowser networkTableBrowser = new DefaultTableBrowser("Network Table", CyNetwork.class, cyTableManagerServiceRef,cyNetworkTableManagerServiceRef,cyServiceRegistrarServiceRef,compilerServiceRef,cyNetworkManagerServiceRef,deleteTableTaskFactoryService,guiTaskManagerServiceRef,popupMenuHelper,cyApplicationManagerServiceRef, cyEventHelperServiceRef);//, mapGlobalTableTaskFactoryServiceRef);
		
		AbstractTableBrowser globalTableBrowser = new GlobalTableBrowser("Unassigned Tables", cyTableManagerServiceRef,cyNetworkTableManagerServiceRef,cyServiceRegistrarServiceRef,compilerServiceRef,cyNetworkManagerServiceRef,deleteTableTaskFactoryService,guiTaskManagerServiceRef,popupMenuHelper,cyApplicationManagerServiceRef, cyEventHelperServiceRef);
		
		registerAllServices(bc,nodeTableBrowser, new Properties());
		registerAllServices(bc,edgeTableBrowser, new Properties());
		registerAllServices(bc,networkTableBrowser, new Properties());

		Properties globalTableProp = new Properties();
		registerService(bc, globalTableBrowser, ActionListener.class, globalTableProp);
		registerService(bc, globalTableBrowser, SessionLoadedListener.class, globalTableProp);
		registerService(bc, globalTableBrowser, SessionAboutToBeSavedListener.class, globalTableProp);
		registerService(bc, globalTableBrowser, TableAboutToBeDeletedListener.class, globalTableProp);
		registerService(bc, globalTableBrowser, TableAddedListener.class, globalTableProp);
		registerService(bc, globalTableBrowser, TablePrivacyChangedListener.class, globalTableProp);

		registerServiceListener(bc,popupMenuHelper,"addTableColumnTaskFactory","removeTableColumnTaskFactory",TableColumnTaskFactory.class);
		registerServiceListener(bc,popupMenuHelper,"addTableCellTaskFactory","removeTableCellTaskFactory",TableCellTaskFactory.class);


	}
}

