package org.cytoscape.webservice.internal;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Properties;

import javax.swing.KeyStroke;

import org.cytoscape.application.swing.CyAction;
import org.cytoscape.io.webservice.NetworkImportWebServiceClient;
import org.cytoscape.io.webservice.TableImportWebServiceClient;
import org.cytoscape.io.webservice.WebServiceClient;
import org.cytoscape.io.webservice.swing.WebServiceGUI;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.webservice.internal.task.ShowImportDialogAction;
import org.cytoscape.webservice.internal.ui.WebServiceGUIImpl;
import org.cytoscape.webservice.internal.ui.WebServiceImportDialog;
import org.osgi.framework.BundleContext;

/*
 * #%L
 * Cytoscape Webservice Impl (webservice-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

public class CyActivator extends AbstractCyActivator {

	@Override
	public void start(BundleContext bc) {
		CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);
		
		// UI for Network Import Clients
		WebServiceImportDialog<NetworkImportWebServiceClient> unifiedNetworkImportDialog = new WebServiceImportDialog<NetworkImportWebServiceClient>(
				NetworkImportWebServiceClient.class, "Import Network from Public Databases", serviceRegistrar);
		
		// UI for Table Import Clients
		WebServiceImportDialog<TableImportWebServiceClient> unifiedTableImportDialog = new WebServiceImportDialog<TableImportWebServiceClient>(
				TableImportWebServiceClient.class, "Import Table from Public Databases", serviceRegistrar);
		
		WebServiceGUIImpl webServiceGui = new WebServiceGUIImpl();
		webServiceGui.addClient(NetworkImportWebServiceClient.class, unifiedNetworkImportDialog);
		webServiceGui.addClient(TableImportWebServiceClient.class, unifiedTableImportDialog);
		
		// ALT (for Mac, it's Option)
		final KeyStroke networkImportShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.ALT_DOWN_MASK);
		final KeyStroke tableImportShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.ALT_DOWN_MASK);
		
		ShowImportDialogAction showImportNetworkFromWebServiceDialogAction = new ShowImportDialogAction(
				unifiedNetworkImportDialog,
				"File.Import.Network",
				3.0f,
				"Public Databases...",
				networkImportShortcut,
				2.1f,
				getClass().getResource("/images/icons/import-net-db-32.png"),
				"Import Network From Database...",
				serviceRegistrar
		);
		ShowImportDialogAction showImportTableFromWebServiceDialogAction = new ShowImportDialogAction(
				unifiedTableImportDialog,
				"File.Import.Table",
				3.0f,
				"Public Databases...",
				tableImportShortcut,
				serviceRegistrar
		);
		
		{
			Properties props = new Properties();
			props.setProperty("id", "showImportNetworkFromWebServiceDialogAction");
			registerService(bc, showImportNetworkFromWebServiceDialogAction, CyAction.class, props);
		}
		
		registerService(bc, showImportTableFromWebServiceDialogAction, CyAction.class, new Properties());
		registerService(bc, webServiceGui, WebServiceGUI.class, new Properties());

		registerServiceListener(bc, unifiedNetworkImportDialog, "addClient", "removeClient", WebServiceClient.class);
		registerServiceListener(bc, unifiedTableImportDialog, "addClient", "removeClient", WebServiceClient.class);
	}
}
