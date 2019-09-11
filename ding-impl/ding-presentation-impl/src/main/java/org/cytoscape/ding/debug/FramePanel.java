package org.cytoscape.ding.debug;

import java.awt.BorderLayout;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class FramePanel extends JPanel {

	private JList<String> list;
	private DefaultListModel<String> model;
	
	public FramePanel(String title) {
		createContents(title);
	}
	
	private void createContents(String title) {
		JLabel label = new JLabel(title);
		model = new DefaultListModel<>();
		list = new JList<>(model);
		var renderer = (DefaultListCellRenderer)list.getCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.RIGHT);
		JScrollPane scrollPane = new JScrollPane(list);
		
		setLayout(new BorderLayout());
		add(label, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
	}
	
	public void addMessage(String message) {
		int i = model.size();
		model.add(i, message);
		list.ensureIndexIsVisible(i);
	}
	
	public void clear() {
		model.clear();
	}
	
}
