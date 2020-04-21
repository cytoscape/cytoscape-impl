package org.cytoscape.browser.internal.task;

import org.cytoscape.model.CyColumn;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractTableColumnTask;
import org.cytoscape.work.TaskMonitor;

public class SetColumnFormatTask extends AbstractTableColumnTask {
	
	private CyServiceRegistrar serviceRegistrar;
	
	public SetColumnFormatTask(final CyColumn column, final CyServiceRegistrar serviceRegistrar) {
		super(column);
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(final TaskMonitor tm) throws Exception {
//		SwingUtilities.invokeLater(new Runnable() {
//
//			@Override
//			public void run() {
//				final CyTable table = column.getTable();
//				final String name = column.getName();
//				final BrowserTable browserTable = getBrowserTable(table, serviceRegistrar);
//				final JFrame frame = (JFrame)SwingUtilities.getRoot(browserTable);
//				
//				// Set format
//				SetColumnFormatDialog dialog = new SetColumnFormatDialog(browserTable, frame, name, serviceRegistrar);
//
//				dialog.pack();
//				dialog.setLocationRelativeTo(frame);
//				dialog.setVisible(true);
//				
//			}
//			
//		});
	}
	

//	public static BrowserTable getBrowserTable(final CyTable table, final CyServiceRegistrar serviceRegistrar) {
//		final CySwingApplication swingAppManager = serviceRegistrar.getService(CySwingApplication.class);
//		final CytoPanel cytoPanel = swingAppManager.getCytoPanel(CytoPanelName.SOUTH);
//		
//		if (cytoPanel != null) {
//			final int count = cytoPanel.getCytoPanelComponentCount();
//			
//			for (int i = 0; i < count; i++) {
//				final Component c = cytoPanel.getComponentAt(i);
//				
//				if (c instanceof AbstractTableBrowser) {
//					final AbstractTableBrowser tableBrowser = (AbstractTableBrowser) c;
//					final BrowserTable browserTable = tableBrowser.getBrowserTable(table);
//					
//					if (browserTable != null)
//						return browserTable;
//				}
//			}
//		}
//		
//		return null;
//	}
}

