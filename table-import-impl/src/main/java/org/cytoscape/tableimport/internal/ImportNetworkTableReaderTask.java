package org.cytoscape.tableimport.internal;


import java.io.InputStream;
import javax.swing.JPanel;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.tableimport.internal.ui.ImportAttributeTableTask;
import org.cytoscape.tableimport.internal.ui.ImportTablePanel;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesGUI;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.tableimport.internal.util.CytoscapeServices;


public class ImportNetworkTableReaderTask extends AbstractTask implements CyNetworkReader {
	private ImportTablePanel importTablePanel;
	private final InputStream is;
	private final String fileType;
	private CyNetwork[] networks;
	private final String inputName;
	private final CyTableManager tableManager;

	public ImportNetworkTableReaderTask(final InputStream is, final String fileType,
					    final String inputName, final CyTableManager tableManager)
	{
		this.is           = is;
		this.fileType     = fileType;
		this.inputName    = inputName;
		this.tableManager = tableManager;

		this.importTablePanel = null;
	}

	@ProvidesGUI
	public JPanel getGUI() {
		if (importTablePanel == null) {
			try {
				importTablePanel =
					new ImportTablePanel(ImportTablePanel.NETWORK_IMPORT, is,
					                     fileType, inputName,null, null, null,
					                     null, null, null, tableManager);
			} catch (Exception e) {
				throw new IllegalStateException("Could not initialize ImportTablePanel.", e);
			}
		}

		return importTablePanel;
	}

	@Override
	public void run(TaskMonitor monitor) throws Exception {

		monitor.setTitle("Loading network from table");
		monitor.setProgress(0.0);
		monitor.setStatusMessage("Loading network...");

		importTablePanel.importTable();

		this.insertTasksAfterCurrentTask(importTablePanel.getLoadTask());

		monitor.setProgress(1.0);
	}

	@Override
	public CyNetworkView buildCyNetworkView(CyNetwork arg0) {
		final CyNetworkView view = CytoscapeServices.cyNetworkViewFactory.createNetworkView(arg0);
		return view;
	}

	@Override
	public CyNetwork[] getCyNetworks() {
		return networks;
	}
}
