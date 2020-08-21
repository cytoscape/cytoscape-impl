package org.cytoscape.view.table.internal.equation;

import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.Color;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class InfoPanel extends JPanel {

	private JTextArea textArea;
	private JScrollPane scrollPane;
	private JButton insertButton;
	
	
	public InfoPanel() {
		init();
	}
	
	private void init() {
		setOpaque(!isAquaLAF());
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(getScrollPane())
			.addComponent(getInsertButton())
		);
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(getScrollPane())
			.addComponent(getInsertButton(), Alignment.TRAILING)
		);
	}
	
	public void setText(String s) {
		getTextArea().setText(s);
	}
	
	public JTextArea getTextArea() {
		if(textArea == null) {
			textArea = new JTextArea();
			textArea.setEditable(false);
			Color color = UIManager.getColor("Panel.background");
			textArea.setBackground(color);
		}
		return textArea;
	}
	
	
	private JScrollPane getScrollPane() {
		if(scrollPane == null) {
			scrollPane = new JScrollPane(getTextArea());
			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		}
		return scrollPane;
	}
	
	public JButton getInsertButton() {
		if(insertButton == null) {
			insertButton = new JButton("Insert");
			LookAndFeelUtil.makeSmall(insertButton);
		}
		return insertButton;
	}
	
	
}
