package org.cytoscape.browser.internal.task;

import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.browser.internal.format.SetColumnFormatDialog;
import org.cytoscape.browser.internal.view.AbstractTableBrowser;
import org.cytoscape.browser.internal.view.TableRenderer;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractTableColumnTask;
import org.cytoscape.work.TaskMonitor;

public class SetColumnFormatTask extends AbstractTableColumnTask {
	
	private CyServiceRegistrar serviceRegistrar;
	
	public SetColumnFormatTask(CyColumn column, CyServiceRegistrar serviceRegistrar) {
		super(column);
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor tm) {
		SwingUtilities.invokeLater(() -> {
			CySwingApplication swingAppManager = serviceRegistrar.getService(CySwingApplication.class);
			
			CyTable table = column.getTable();
			String name = column.getName();
			TableRenderer browserTable = getTableRenderer(table, serviceRegistrar);
			JFrame frame = swingAppManager.getJFrame();
			
			SetColumnFormatDialog dialog = new SetColumnFormatDialog(browserTable, frame, name, serviceRegistrar);

			dialog.pack();
			dialog.setLocationRelativeTo(frame);
			dialog.setVisible(true);
		});
	}
	

	public static TableRenderer getTableRenderer(CyTable table, CyServiceRegistrar serviceRegistrar) {
		CySwingApplication swingAppManager = serviceRegistrar.getService(CySwingApplication.class);
		CytoPanel cytoPanel = swingAppManager.getCytoPanel(CytoPanelName.SOUTH);
		
		if (cytoPanel != null) {
			int count = cytoPanel.getCytoPanelComponentCount();
			for (int i = 0; i < count; i++) {
				Component c = cytoPanel.getComponentAt(i);
				if (c instanceof AbstractTableBrowser) {
					AbstractTableBrowser tableBrowser = (AbstractTableBrowser) c;
					TableRenderer browserTable = tableBrowser.getTableRenderer(table);
					if (browserTable != null)
						return browserTable;
				}
			}
		}
		return null;
	}
}

