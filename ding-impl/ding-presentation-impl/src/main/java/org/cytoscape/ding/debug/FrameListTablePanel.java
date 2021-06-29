package org.cytoscape.ding.debug;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.util.LinkedList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class FrameListTablePanel extends JPanel {
	
	private static final int MAX_ITEMS = 300;
	
	private final JTable table;
	private final FramePanelTableModel model;
	
	
	public FrameListTablePanel(String title) {
		model = new FramePanelTableModel(MAX_ITEMS);
		table = new JTable(model);
		JLabel label = new JLabel(title);
		LookAndFeelUtil.makeSmall(label);
		JScrollPane scrollPane = new JScrollPane(table);
		
		setOpaque(false);
		
		setLayout(new BorderLayout());
		add(label, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
	}
	
	public void addFrame(DebugRootFrameInfo frame) {
		model.add(frame);
		int lastRow = model.getRowCount() - 1;
		Rectangle cellRect = table.getCellRect(lastRow, 0, true);
		table.scrollRectToVisible(cellRect);
	}
	
	public void clear() {
		model.clear();
	}
	
	
	private class FramePanelTableModel extends AbstractTableModel {
		
		private static final int NODE_COL = 0;
		private static final int EDGE_COL = 1;
		private static final int TIME_COL = 2;
		private static final int OPT_COL  = 3;

		private final int maxSize;
		private LinkedList<DebugRootFrameInfo> list = new LinkedList<>();
		
		
		public FramePanelTableModel(int maxSize) {
			this.maxSize = maxSize;
		}
		
		public void add(DebugRootFrameInfo entry) {
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
			return 4;
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
				case EDGE_COL: return "Edges";
				case OPT_COL:  return "Opt";
				
			}
			return null;
		}

		@Override
		public Object getValueAt(int row, int col) {
			var entry = list.get(row);
			switch(col) {
				case TIME_COL: return getTimeMessage(entry);
				case NODE_COL: return entry.getRenderDetailFlags().getVisibleNodeCount();
				case EDGE_COL: return entry.getRenderDetailFlags().getEstimatedEdgeCount();
				case OPT_COL:  return getOptimizationMessage(entry);
			}
			return null;
		}
		
		public String getTimeMessage(DebugRootFrameInfo entry) {
			var time = entry.getTime();
			var type = entry.getType();
			var cancelled = entry.isCancelled();
			
			if(type == DebugFrameType.MAIN_ANNOTAITONS || type == DebugFrameType.BEV_ANNOTAITONS)
				return time + " (annotations)";
			else if(type == DebugFrameType.MAIN_EDGES)
				return time + " (edges)";
			else if(type == DebugFrameType.MAIN_SELECTED)
				return time + " (selected)";
			else if(cancelled)
				return time + " (cancelled)";
			else
				return "" + time;
		}
		
		public String getOptimizationMessage(DebugRootFrameInfo entry) {
			var params = entry.getPaintParameters();
			if(params.isPan()) {
				return "bpan " + params.getPanCanvasName();
			}
			return "";
		}

	}

	
}
