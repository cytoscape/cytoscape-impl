



package org.cytoscape.browser.internal;

import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.work.swing.GUITaskManager;

import org.cytoscape.browser.internal.TableBrowser;
import org.cytoscape.browser.internal.PopupMenuHelper;


import org.cytoscape.task.TableCellTaskFactory;
import org.cytoscape.task.TableColumnTaskFactory;

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
		TableTaskFactory deleteTableTaskFactoryService = getService(bc,TableTaskFactory.class);
		GUITaskManager guiTaskManagerServiceRef = getService(bc,GUITaskManager.class);
		CyApplicationManager cyApplicationManagerServiceRef = getService(bc,CyApplicationManager.class);
		CyNetworkTableManager cyNetworkTableManagerServiceRef = getService(bc,CyNetworkTableManager.class);

		PopupMenuHelper popupMenuHelper = new PopupMenuHelper(guiTaskManagerServiceRef);
		TableBrowser tableBrowser = new TableBrowser(cyTableManagerServiceRef,cyNetworkTableManagerServiceRef,cyServiceRegistrarServiceRef,compilerServiceRef,openBrowserServiceRef,cyNetworkManagerServiceRef,deleteTableTaskFactoryService,guiTaskManagerServiceRef,popupMenuHelper,cyApplicationManagerServiceRef);
		
		registerAllServices(bc,tableBrowser, new Properties());

		registerServiceListener(bc,popupMenuHelper,"addTableColumnTaskFactory","removeTableColumnTaskFactory",TableColumnTaskFactory.class);
		registerServiceListener(bc,popupMenuHelper,"addTableCellTaskFactory","removeTableCellTaskFactory",TableCellTaskFactory.class);


	}
}

