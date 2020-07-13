package org.cytoscape.view.vizmap.gui.internal.view;

import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.util.Collection;
import java.util.Collections;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.application.swing.CyColumnComboBox;
import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;

public class ColumnStylePanel {

	private final ServicesUtil servicesUtil;
	
	private JLabel tableNameLabel;
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
			
			layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getTableNameLabel(), PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
				.addComponent(getColumnComboBox(), PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
			);
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, false)
				.addComponent(getTableNameLabel(), PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
				.addComponent(getColumnComboBox(), PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
			);
		}
		return columnPanel;
	}
	
	public CyColumnComboBox getColumnComboBox() {
		if(columnCombo == null) {
			var columnPresentationManager = servicesUtil.get(CyColumnPresentationManager.class);
			columnCombo = new CyColumnComboBox(columnPresentationManager, Collections.emptyList());
		}
		return columnCombo;
	}
	
	private JLabel getTableNameLabel() {
		if(tableNameLabel == null) {
			tableNameLabel = new JLabel();
		}
		return tableNameLabel;
	}
	
	public void updateColumns(String tableName, Collection<CyColumn> columns, CyColumn selected) {
		// MKTODO may need to abbreviate
		getTableNameLabel().setText(tableName);
		CyColumnComboBox columnComboBox = getColumnComboBox();
		columnComboBox.removeAllItems();
		columns.forEach(columnComboBox::addItem);
		if(selected != null)
			columnComboBox.setSelectedItem(selected);
	}
	
	
}