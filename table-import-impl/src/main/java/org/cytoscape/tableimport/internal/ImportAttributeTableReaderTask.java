package org.cytoscape.tableimport.internal;


import java.io.InputStream;

import javax.swing.JPanel;

import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.tableimport.internal.ui.ImportAttributeTableTask;
import org.cytoscape.tableimport.internal.ui.ImportTablePanel;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesGUI;
import org.cytoscape.work.TaskMonitor;


public class ImportAttributeTableReaderTask extends AbstractTask implements CyTableReader {
	private ImportTablePanel importTablePanel;
	private final InputStream is;
	private final String fileType;
	private final CyTableManager tableManager;

	public ImportAttributeTableReaderTask(final InputStream is, final String fileType,
					      final CyTableManager tableManager)
	{
		this.is           = is;
		this.fileType     = fileType;
		this.tableManager = tableManager;
		
		this.importTablePanel = null;
	}

	@ProvidesGUI
	public JPanel getGUI() {
		if (importTablePanel == null) {
			try {
				this.importTablePanel = 
					new ImportTablePanel(ImportTablePanel.SIMPLE_ATTRIBUTE_IMPORT,
					                     is, fileType, null, null, null, null,
					                     null, null, tableManager);
			} catch (Exception e) {
				throw new IllegalStateException("Could not initialize ImportTablePanel.", e);
			}
		}

		return importTablePanel;
	}

	@Override
	public void run(TaskMonitor monitor) throws Exception {
		monitor.setTitle("Loading attribute table data");
		monitor.setProgress(0.0);
		monitor.setStatusMessage("Loading table...");

		importTablePanel.importTable();

		this.insertTasksAfterCurrentTask(importTablePanel.getLoadTask());

		monitor.setProgress(1.0);
	}

	@Override
	public CyTable[] getCyTables() {
		if (this.importTablePanel.getLoadTask() instanceof ImportAttributeTableTask) {
			ImportAttributeTableTask importTask = (ImportAttributeTableTask) this.importTablePanel
					.getLoadTask();
			return importTask.getCyTables();
		}
		
		
		return null;
	}
}
