package org.cytoscape.webservice.internal.task;

import java.awt.Window;
import java.awt.event.ActionEvent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.webservice.internal.ui.UnifiedNetworkImportDialog;

/**
 * Network Import GUI.
 * 
 */
public class ShowNetworkImportDialogAction extends AbstractCyAction {

	private static final long serialVersionUID = -36712860667900147L;

	private UnifiedNetworkImportDialog dialog;

	private final Window parent;

	public ShowNetworkImportDialogAction(
			final CyApplicationManager applicationManager,
			final CySwingApplication app,
			final UnifiedNetworkImportDialog dialog) {
		super("Public Databases...", applicationManager);
		setPreferredMenu("File.Import.Network");

		if (dialog == null)
			throw new NullPointerException("Dialog is null.");

		this.parent = app.getJFrame();
		this.dialog = dialog;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
	}

}
