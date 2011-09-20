package org.cytoscape.webservice.ncbi.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JPanel;

import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.webservice.ncbi.NCBITableImportClient;
import org.cytoscape.work.TaskManager;

public class NCBIGeneDialog extends JDialog implements PropertyChangeListener {

	private static final long serialVersionUID = -2609215983943863094L;

	private final CyNetworkManager netManager;

	public NCBIGeneDialog(final NCBITableImportClient client, final TaskManager taskManager, final CyNetworkManager netManager) {
		this.netManager = netManager;

		setTitle("NCBI Entrez Gene");

		final JPanel panel = new NCBIGenePanel(client, taskManager, netManager, "NCBI table import");
		panel.addPropertyChangeListener(this);
		add(panel);
		pack();
		setLocationRelativeTo(null);

	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("CLOSE")) {
			dispose();
		}
	}

}
