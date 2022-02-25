package org.cytoscape.search.internal.ui;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.cytoscape.search.internal.progress.ProgressMonitor;
import org.cytoscape.search.internal.progress.ProgressViewer;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class ProgressPanel extends JPanel implements ProgressViewer {
	
	
	private static final Object[] HEADER_NAMES = { "Network", "Progress" };
	
	private final boolean popup;
	
	private JButton clearButton;
	private JTable table;
	private JScrollPane tableScrollPane;
	
	private List<ProgressPopupMonitor> progressInfo = new ArrayList<>();
	
	
	public ProgressPanel(boolean popup) {
		this.popup = popup;
		initComponents();
	}
	
	private class ProgressPopupMonitor implements ProgressMonitor {
		
		private final String name;
		private double progess;
		
		public ProgressPopupMonitor(String name) {
			this.name = name;
		}

		@Override
		public void addProgress(double progress) {
			this.progess = Math.max(0.0, Math.min(1.0, this.progess + progress));
			((DefaultTableModel)getTable().getModel()).fireTableDataChanged();
		}
		
		@Override
		public void done() {
			if(popup) {
				progressInfo.remove(this);
				updateTable();
			}
		}
		
		public String toString() {
			return String.format("%.0f%%", progess * 100.0);
		}
	}
	
	@Override
	public ProgressMonitor addProgress(String name) {
		var pm = new ProgressPopupMonitor(name);
		progressInfo.add(pm);
		updateTable();
		return pm;
	}
	
	
	public void initComponents() {
		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		JLabel title = new JLabel("Network Indexing Status");
		
		if(popup) {
			LookAndFeelUtil.makeSmall(title);
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addComponent(title,  DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getTableScrollPane(), 300, 300, 300)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(title, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getTableScrollPane(), 100, 100, 100)
			);
		} else {
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addComponent(title,  DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getTableScrollPane(), 300, 300, Short.MAX_VALUE)
				.addComponent(getClearButton())
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(title, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getTableScrollPane(), 300, PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(getClearButton())
			);
		}
	}
	
	private JButton getClearButton() {
		if(clearButton == null) {
			clearButton = new JButton("Clear");
			clearButton.addActionListener(e -> {
				progressInfo.clear();
				updateTable();
			});
		}
		return clearButton;
	}
	
	private JTable getTable() {
		if (table == null) {
			var tableModel = new DefaultTableModel(HEADER_NAMES, 0);
			table = new JTable(tableModel);
			table.setTableHeader(null);
			table.setShowGrid(false);
			table.setRowSelectionAllowed(false);
			
			if(popup) {
				JTextField tmpField = new JTextField();
				LookAndFeelUtil.makeSmall(tmpField);
				table.setRowHeight(Math.max(table.getRowHeight(), tmpField.getPreferredSize().height - 4));
				table.setIntercellSpacing(new Dimension(0, 1));
			}
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
		for(var pm : progressInfo) {
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
		colModel.getColumn(1).setMaxWidth(40);
		colModel.getColumn(0).setResizable(false);
		colModel.getColumn(1).setResizable(false);
	}
	
}
