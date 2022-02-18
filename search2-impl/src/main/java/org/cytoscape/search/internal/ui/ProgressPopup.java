package org.cytoscape.search.internal.ui;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.Color;
import java.awt.Dimension;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.cytoscape.search.internal.progress.ProgressMonitor;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class ProgressPopup extends JPanel {
	
	
	private static final Object[] HEADER_NAMES = { "Network", "Progress" };
	
	private JTable table;
	private JScrollPane tableScrollPane;
	
	private Map<Long,InternalProgressMonitor> progressInfo = new LinkedHashMap<>(); // Want to maintain insertion order
	
	
	public ProgressPopup() {
		initComponents();
	}
	
	private class InternalProgressMonitor implements ProgressMonitor {
		
		private final Long suid;
		private final String name;
		private double progess;
		
		public InternalProgressMonitor(Long suid, String name) {
			this.suid = suid;
			this.name = name;
		}

		@Override
		public void addProgress(double progress) {
			this.progess = Math.max(0.0, Math.min(1.0, this.progess + progress));
			((DefaultTableModel)getTable().getModel()).fireTableDataChanged();
		}
		
		@Override
		public void done() {
			progressInfo.remove(suid);
			updateTable();
		}
		
		public String toString() {
			return String.format("%.0f%%", progess * 100.0);
		}
	}
	
	
	public ProgressMonitor addProgress(Long suid, String name) {
		System.out.println("ProgressPopup.addProgress() " + suid);
		var pm = new InternalProgressMonitor(suid, name);
		progressInfo.put(suid, pm);
		updateTable();
		return pm;
	}
	
	
	public void initComponents() {
		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		JLabel title = new JLabel("Network Indexing Status");
		LookAndFeelUtil.makeSmall(title);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
			.addComponent(title,  DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(getTableScrollPane(), 300, 300, 300)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(title, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			.addComponent(getTableScrollPane(), 100, 100, 100)
		);
	}
	
	private JTable getTable() {
		if (table == null) {
			var tableModel = new DefaultTableModel(HEADER_NAMES, 0);
			
			table = new JTable(tableModel);
			table.setTableHeader(null);
			table.setShowGrid(false);
			
			JTextField tmpField = new JTextField();
			LookAndFeelUtil.makeSmall(tmpField);
			table.setRowHeight(Math.max(table.getRowHeight(), tmpField.getPreferredSize().height - 4));
			table.setIntercellSpacing(new Dimension(0, 1));
			
			table.setRowSelectionAllowed(false);
		}
		return table;
	}
	
	private JScrollPane getTableScrollPane() {
		if (tableScrollPane == null) {
			tableScrollPane = new JScrollPane();
			tableScrollPane.setViewportView(getTable());
			
			final Color bg = UIManager.getColor("Table.background");
			tableScrollPane.setBackground(bg);
			tableScrollPane.getViewport().setBackground(bg);
		}

		return tableScrollPane;
	}
	
	
	private void updateTable() {
		Object[][] data = new Object[progressInfo.size()][2];
		int i = 0;
		for(var pm : progressInfo.values()) {
			data[i][0] = pm.name;
			data[i][1] = pm;
			i++;
		}
		
		var model = new DefaultTableModel(data, HEADER_NAMES) {
			@Override public boolean isCellEditable(int r, int c) {
				return false;
			}
		};
		getTable().setModel(model);
		
		
		TableColumnModel colModel = getTable().getColumnModel();
		colModel.getColumn(1).setMaxWidth(30);
		colModel.getColumn(0).setResizable(false);
		colModel.getColumn(1).setResizable(false);
	}
	
}
