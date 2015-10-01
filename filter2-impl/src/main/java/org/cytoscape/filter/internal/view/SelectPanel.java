package org.cytoscape.filter.internal.view;

import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class SelectPanel extends JPanel {
	
	public SelectPanel(FilterPanel filterPanel, TransformerPanel transformerPanel) {
		JTabbedPane tabPane = new JTabbedPane(JTabbedPane.BOTTOM);
		tabPane.addTab("Filter", null, filterPanel, "Define filters");
		tabPane.addTab("Chain", null, transformerPanel, "Chain together filtering operations");
		
		setLayout(new BorderLayout());
		add(tabPane, BorderLayout.CENTER);
		
		setOpaque(!isAquaLAF());
	}
}
