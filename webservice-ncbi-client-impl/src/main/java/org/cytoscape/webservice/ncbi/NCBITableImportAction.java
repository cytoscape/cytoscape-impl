package org.cytoscape.webservice.ncbi;

import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.webservice.ncbi.ui.NCBIGeneDialog;
import org.cytoscape.work.TaskManager;

public class NCBITableImportAction extends AbstractCyAction {

	private static final long serialVersionUID = 3101400401346193602L;

	final CyNetworkManager netManager;
	private final NCBITableImportClient client;
	private final TaskManager taskManager;

	public NCBITableImportAction(final NCBITableImportClient client, final TaskManager taskManager,
			final CyNetworkManager netManager) {
		super("Import Data Table from NCBI...");
		setPreferredMenu("File.Import.Table.WebService");
		this.netManager = netManager;
		this.client = client;
		this.taskManager = taskManager;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		final NCBIGeneDialog dialog = new NCBIGeneDialog(client, taskManager, netManager);
		dialog.setVisible(true);
	}

}
