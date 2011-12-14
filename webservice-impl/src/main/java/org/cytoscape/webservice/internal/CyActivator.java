package org.cytoscape.webservice.internal;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.work.swing.DialogTaskManager;

import org.cytoscape.webservice.internal.ui.UnifiedNetworkImportDialog;
import org.cytoscape.webservice.internal.task.ShowNetworkImportDialogAction;

import org.cytoscape.application.swing.CyAction;

import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;

import org.cytoscape.io.webservice.NetworkImportWebServiceClient;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		CySwingApplication cySwingApplicationServiceRef = getService(bc, CySwingApplication.class);
		DialogTaskManager taskManagerServiceRef = getService(bc, DialogTaskManager.class);

		UnifiedNetworkImportDialog unifiedNetworkImportDialog = new UnifiedNetworkImportDialog(taskManagerServiceRef);
		ShowNetworkImportDialogAction showNetworkImportDialogAction = new ShowNetworkImportDialogAction(
				cySwingApplicationServiceRef, unifiedNetworkImportDialog);

		registerService(bc, showNetworkImportDialogAction, CyAction.class, new Properties());
		registerServiceListener(bc, unifiedNetworkImportDialog, "addNetworkImportClient", "removeNetworkImportClient",
				NetworkImportWebServiceClient.class);
	}
}
