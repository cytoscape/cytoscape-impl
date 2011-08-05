package org.cytoscape.task.internal.quickstart;

import java.util.List;

import javax.swing.JOptionPane;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class MergeDataTask extends AbstractTask {
	
	private final QuickStartState state;
	private final ImportTaskUtil util;
	
	MergeDataTask(final QuickStartState state, final ImportTaskUtil util) {
		this.state = state;
		this.util = util;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// Step 1: Create new column for the network.
		final IDType type = state.getIDType();
		if(type == null)
			throw new IllegalStateException("ID type is unknown.");
		
		final String columnName = type.getDisplayName();
		final CyTable table = state.getImportedTable();
		final CyNetwork net = util.getTargetNetwork();
		final CyTable nodeTable = net.getDefaultNodeTable();
		
		// "Copy" Name column to ID type name.
		final CyColumn pKey = table.getPrimaryKey();
		table.addVirtualColumn(columnName, pKey.getName(), table, pKey.getName(), pKey.getName(), false);
		nodeTable.addVirtualColumn(columnName, pKey.getName(), table, pKey.getName(), pKey.getName(), false);
				
		taskMonitor.setStatusMessage("Finished!");
		taskMonitor.setProgress(1.0);
		
		JOptionPane.showMessageDialog(null, generateReport(table, columnName), "Network and Attributes Loaded", JOptionPane.INFORMATION_MESSAGE);
	}
	
	private String generateReport(final CyTable table, final String columnName) {

		final StringBuilder builder = new StringBuilder();
		
		builder.append("Data sets loaded:\n  Network: " + util.getTargetNetwork().getCyRow().get(CyTableEntry.NAME, String.class));
		builder.append("\n  Data Table: " + table.getTitle());
		builder.append("\n\n  ID Type: " + columnName);
		builder.append("\n  Matched entries: " + checkMatching(columnName, table.getColumn(columnName).getType()));
		
		// TODO: Should be done in mapping. 
		return builder.toString();
	}
	
	private int checkMatching(final String columnName, final Class<?> type) {
		final CyNetwork net = util.getTargetNetwork();
		final CyTable nodeTable = net.getDefaultNodeTable();
		
		final List<CyRow> rows = nodeTable.getAllRows();
		int matched = 0;
		
		for(CyRow row: rows) {
			Object val = row.get(columnName, type);
			if(val != null)
				matched++;
		}
		
		return matched;
	}

}
