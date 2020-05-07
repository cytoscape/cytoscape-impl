package org.cytoscape.view.vizmap.gui.internal.view.table;

import javax.swing.SwingUtilities;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyColumn;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class TableStyleDialogTask extends AbstractTask {

	private final CyServiceRegistrar registrar;
	private final CyColumn column;
	
	public TableStyleDialogTask(CyColumn column, CyServiceRegistrar registrar) {
		this.registrar = registrar;
		this.column = column;
	}

	@Override
	public void run(TaskMonitor tm) {
		CySwingApplication swingApplication = registrar.getService(CySwingApplication.class);
		SwingUtilities.invokeLater(() -> {
			ColumnStyleDialog dialog = new ColumnStyleDialog(column, registrar);
			dialog.setLocationRelativeTo(swingApplication.getJFrame());
			dialog.setVisible(true);
		});
	}

}
