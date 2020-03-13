package org.cytoscape.ding.debug;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.cytoscape.util.swing.BasicCollapsiblePanel;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class FrameRatePanel extends BasicCollapsiblePanel {

	private JTable table;
	private FrameRateTableModel model;
	
	public FrameRatePanel() {
		super("Frame Rate");
		createContents();
	}

	
	private void createContents() {
		JLabel frameRateLabel = new JLabel("Frame Rate: ");
		LookAndFeelUtil.makeSmall(frameRateLabel);
		model = new FrameRateTableModel();
		table = new JTable(model);
		table.setShowGrid(true);
		
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(300, 200));
		
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(frameRateLabel)
			.addComponent(scrollPane)
		);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(frameRateLabel)
			.addComponent(scrollPane)
		);
		
		
		JPanel content = getContentPane();
		content.setLayout(new BorderLayout());
		content.add(BorderLayout.WEST, panel);
	}
	
	
	private class FrameRateTableModel extends AbstractTableModel {
		
		@Override
		public int getRowCount() {
			return 2;
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int col) {
			switch(col) {
				case 0:  return "Render";
				case 1:  return "%";
				default: return null;
			}
		}
		
		@Override
		public Class<?> getColumnClass(int col) {
			return String.class;
		}
		
		@Override
		public Object getValueAt(int row, int col) {
			if(col == 0) {
				if(row == 0) {
					return "Nodes:";
				} else if(row == 1) {
					return "Edges";
				}
				
			} else if(col == 1) {
				return "99%";
			}
			return null;
		}
	}
	

}
