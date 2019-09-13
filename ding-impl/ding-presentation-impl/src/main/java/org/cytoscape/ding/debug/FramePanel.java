package org.cytoscape.ding.debug;

import java.awt.BorderLayout;
import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

@SuppressWarnings("serial")
public class FramePanel extends JPanel {
	
	private static final int MAX_ITEMS = 300;
	
	private final JTable table;
	private final DebugTableModel model;
	
	
	public FramePanel(String title) {
		model = new DebugTableModel(MAX_ITEMS);
		table = new JTable(model);
		JLabel label = new JLabel(title);
		JScrollPane scrollPane = new JScrollPane(table);
		
		setLayout(new BorderLayout());
		add(label, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
	}
	
	public void addEntry(DebugEntry entry) {
		model.add(entry);
		int lastRow = model.getRowCount() - 1;
		Rectangle cellRect = table.getCellRect(lastRow, 0, true);
		table.scrollRectToVisible(cellRect);
	}
	
	public void clear() {
		model.clear();
	}
	
}
