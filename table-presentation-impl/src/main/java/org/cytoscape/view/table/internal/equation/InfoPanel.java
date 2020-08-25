package org.cytoscape.view.table.internal.equation;

import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.Color;
import java.awt.Font;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLDocument;

import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class InfoPanel extends JPanel {

	private JEditorPane textArea;
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
		getTextArea().setCaretPosition(0); // scroll to top
	}
	
	public JEditorPane getTextArea() {
		if(textArea == null) {
			textArea = new JEditorPane("text/html", "");
			textArea.setEditable(false);
			
			Color color = UIManager.getColor("Panel.background");
			textArea.setBackground(color);
			
			JLabel label = new JLabel();
			LookAndFeelUtil.makeSmall(label);
			Font font = label.getFont();
			String bodyRule = "body { font-family: " + font.getFamily() + "; " + "font-size: " + font.getSize() + "pt; }";
		    ((HTMLDocument)textArea.getDocument()).getStyleSheet().addRule(bodyRule);
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
