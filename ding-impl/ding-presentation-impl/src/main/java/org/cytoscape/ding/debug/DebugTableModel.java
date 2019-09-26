package org.cytoscape.ding.debug;

import java.util.LinkedList;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class DebugTableModel extends AbstractTableModel {
	
	private static final int NODE_COL = 0;
	private static final int EDGE_COL = 1;
	private static final int TIME_COL = 2;

	private final int maxSize;
	private LinkedList<DebugEntry> list = new LinkedList<>();
	
	
	public DebugTableModel(int maxSize) {
		this.maxSize = maxSize;
	}
	
	public void add(DebugEntry entry) {
		if(list.size() == maxSize) {
			list.removeFirst();
			fireTableRowsDeleted(0, 0);
		}
		list.addLast(entry);
		int last = list.size() - 1;
		fireTableRowsInserted(last, last);
	}
	
	public void clear() {
		if(list.isEmpty())
			return;
		int last = list.size() - 1;
		list.clear();
		fireTableRowsDeleted(0, last);
	}
	
	@Override
	public int getColumnCount() {
		return 3;
	}
	
	@Override
	public int getRowCount() {
		return list.size();
	}
	
	@Override
	public String getColumnName(int col) {
		switch(col) {
			case TIME_COL: return "Time (MS)";
			case NODE_COL: return "Nodes";
			case EDGE_COL: return "Edges (est)";
		}
		return null;
	}

	@Override
	public Object getValueAt(int row, int col) {
		DebugEntry entry = list.get(row);
		switch(col) {
			case TIME_COL: return entry.getTimeMessage();
			case NODE_COL: return entry.getNodeCount();
			case EDGE_COL: return entry.getEdgeCountEstimate();
		}
		return null;
	}

}
