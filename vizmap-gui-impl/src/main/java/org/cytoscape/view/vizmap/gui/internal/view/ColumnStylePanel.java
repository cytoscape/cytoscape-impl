package org.cytoscape.view.vizmap.gui.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.util.Collection;
import java.util.Collections;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.cytoscape.application.swing.CyColumnComboBox;
import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;

public class ColumnStylePanel {

	private final ServicesUtil servicesUtil;
	
	private CyColumnComboBox columnCombo;
	private JPanel columnPanel;
	
	
	public ColumnStylePanel(ServicesUtil servicesUtil) {
		this.servicesUtil = servicesUtil;
	}
	
	public JComponent getComponent() {
		return getColumnPanel();
	}
	
	public JPanel getColumnPanel() {
		if(columnPanel == null) {
			columnPanel = new JPanel();
			columnPanel.setOpaque(!isAquaLAF());
			
			var layout = new GroupLayout(columnPanel);
			columnPanel.setLayout(layout);
			layout.setAutoCreateGaps(!isAquaLAF());
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(getColumnComboBox(), PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
			);
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING, false)
				.addComponent(getColumnComboBox(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		return columnPanel;
	}
	
	private CyColumnComboBox getColumnComboBox() {
		if(columnCombo == null) {
			var columnPresentationManager = servicesUtil.get(CyColumnPresentationManager.class);
			columnCombo = new CyColumnComboBox(columnPresentationManager, Collections.emptyList());
		}
		return columnCombo;
	}
	
	
	public void updateColumns(Collection<CyColumn> columns, CyColumn selected) {
		CyColumnComboBox columnComboBox = getColumnComboBox();
		columnComboBox.removeAllItems();
		columns.forEach(columnComboBox::addItem);
		if(selected != null)
			columnComboBox.setSelectedItem(selected);
	}
	
	
}
