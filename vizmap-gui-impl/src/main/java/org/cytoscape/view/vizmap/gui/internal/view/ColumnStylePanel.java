package org.cytoscape.view.vizmap.gui.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.Dimension;
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
	
	private OptionsButton optionsBtn;
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
			
			// TODO: For some reason, the Styles button is naturally taller than the Options one on Nimbus and Windows.
			//       Let's force it to have the same height.
			getColumnComboBox().setPreferredSize(
					new Dimension(getColumnComboBox().getPreferredSize().width, getOptionsBtn().getOptionsBtn().getPreferredSize().height));
			
			var layout = new GroupLayout(columnPanel);
			columnPanel.setLayout(layout);
			layout.setAutoCreateGaps(!isAquaLAF());
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addComponent(getColumnComboBox(), 0, 146, Short.MAX_VALUE)
					.addComponent(getOptionsBtn().getOptionsBtn(), PREFERRED_SIZE, 64, PREFERRED_SIZE)
			);
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING, false)
					.addComponent(getColumnComboBox(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getOptionsBtn().getOptionsBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
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
	
	OptionsButton getOptionsBtn() {
		if (optionsBtn == null) {
			optionsBtn = new OptionsButton(servicesUtil);
		}
		return optionsBtn;
	}
	
	public void updateColumns(Collection<CyColumn> columns, CyColumn selected) {
		CyColumnComboBox columnComboBox = getColumnComboBox();
		columnComboBox.removeAllItems();
		if(columns != null)
			columns.forEach(columnComboBox::addItem);
		if(selected != null)
			columnComboBox.setSelectedItem(selected);
	}
	
	
}