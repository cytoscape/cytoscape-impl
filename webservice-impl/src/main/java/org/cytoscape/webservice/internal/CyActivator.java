package org.cytoscape.webservice.internal;

/*
 * #%L
 * Cytoscape Webservice Impl (webservice-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Properties;

import javax.swing.KeyStroke;

import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.webservice.NetworkImportWebServiceClient;
import org.cytoscape.io.webservice.TableImportWebServiceClient;
import org.cytoscape.io.webservice.WebServiceClient;
import org.cytoscape.io.webservice.swing.WebServiceGUI;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.webservice.internal.task.ShowImportDialogAction;
import org.cytoscape.webservice.internal.ui.WebServiceGUIImpl;
import org.cytoscape.webservice.internal.ui.WebServiceImportDialog;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {

	public CyActivator() {
		super();
	}

	@Override
	public void start(BundleContext bc) {
		CySwingApplication cySwingApplicationServiceRef = getService(bc, CySwingApplication.class);
		DialogTaskManager taskManagerServiceRef = getService(bc, DialogTaskManager.class);
		OpenBrowser openBrowser = getService(bc, OpenBrowser.class);
		
		// UI for Network Import Clients
		WebServiceImportDialog<NetworkImportWebServiceClient> unifiedNetworkImportDialog = new WebServiceImportDialog<NetworkImportWebServiceClient>(
				NetworkImportWebServiceClient.class, "Import Network from Web Service", cySwingApplicationServiceRef, taskManagerServiceRef, openBrowser);
		
		// UI for Table Import Clients
		WebServiceImportDialog<TableImportWebServiceClient> unifiedTableImportDialog = new WebServiceImportDialog<TableImportWebServiceClient>(
				TableImportWebServiceClient.class, "Import Data Table from Web Service", cySwingApplicationServiceRef, taskManagerServiceRef, openBrowser);
		
		WebServiceGUIImpl webServiceGui = new WebServiceGUIImpl();
		webServiceGui.addClient(NetworkImportWebServiceClient.class, unifiedNetworkImportDialog);
		webServiceGui.addClient(TableImportWebServiceClient.class, unifiedTableImportDialog);
		
		// ALT (for Mac, it's Option)
		final KeyStroke networkImportShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.ALT_DOWN_MASK);
		final KeyStroke tableImportShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.ALT_DOWN_MASK);
		
		ShowImportDialogAction showImportNetworkFromWebServiceDialogAction = new ShowImportDialogAction(
				cySwingApplicationServiceRef, unifiedNetworkImportDialog, "File.Import.Network", "Public Databases...", networkImportShortcut);
		ShowImportDialogAction showImportTableFromWebServiceDialogAction = new ShowImportDialogAction(
				cySwingApplicationServiceRef, unifiedTableImportDialog, "File.Import.Table", "Public Databases...", tableImportShortcut);

		Properties showImportNetworkFromWebServiceDialogActionProps = new Properties();
		showImportNetworkFromWebServiceDialogActionProps.setProperty("id",
				"showImportNetworkFromWebServiceDialogAction");
		registerService(bc, showImportNetworkFromWebServiceDialogAction, CyAction.class,
				showImportNetworkFromWebServiceDialogActionProps);
		registerService(bc, showImportTableFromWebServiceDialogAction, CyAction.class,
				new Properties());
		registerService(bc, webServiceGui, WebServiceGUI.class, new Properties());

		registerServiceListener(bc, unifiedNetworkImportDialog, "addClient", "removeClient",
				WebServiceClient.class);
		registerServiceListener(bc, unifiedTableImportDialog, "addClient", "removeClient",
				WebServiceClient.class);
	}
}
