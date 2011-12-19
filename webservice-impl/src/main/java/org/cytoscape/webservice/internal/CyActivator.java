package org.cytoscape.webservice.internal;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.work.swing.DialogTaskManager;

import org.cytoscape.webservice.internal.ui.UnifiedNetworkImportDialog;
import org.cytoscape.webservice.internal.task.ShowImportNetworkFromWebServiceDialogAction;

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
		ShowImportNetworkFromWebServiceDialogAction showImportNetworkFromWebServiceDialogAction = new ShowImportNetworkFromWebServiceDialogAction(
				cySwingApplicationServiceRef, unifiedNetworkImportDialog);

		Properties showImportNetworkFromWebServiceDialogActionProps = new Properties();
		showImportNetworkFromWebServiceDialogActionProps.setProperty("id", "showImportNetworkFromWebServiceDialogAction");
		registerService(bc, showImportNetworkFromWebServiceDialogAction, CyAction.class,
				showImportNetworkFromWebServiceDialogActionProps);

		registerServiceListener(bc, unifiedNetworkImportDialog, "addNetworkImportClient", "removeNetworkImportClient",
				NetworkImportWebServiceClient.class);
	}
}
