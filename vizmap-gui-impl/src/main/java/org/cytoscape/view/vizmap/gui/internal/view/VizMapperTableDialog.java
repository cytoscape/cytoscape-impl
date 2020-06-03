package org.cytoscape.view.vizmap.gui.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.Dimension;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;

@SuppressWarnings("serial")
public class VizMapperTableDialog extends JDialog implements VisualPropertySheetContainer {

	private final ServicesUtil servicesUtil;
	private PropertySheetPanel propertySheetPanel;
	
	public VizMapperTableDialog(ServicesUtil servicesUtil) {
		this.servicesUtil = servicesUtil;
		init();
	}
	
	PropertySheetPanel getPropertiesPnl() {
		if(propertySheetPanel == null) {
			propertySheetPanel = new PropertySheetPanel();
		}
		return propertySheetPanel;
	}
	
	@Override
	public JComponent getComponent() {
		return getRootPane();
	}
	
	@Override
	public RenderingEngine<CyNetwork> getRenderingEngine() {
		return null;
	}
	
	@Override
	public VisualPropertySheet getVisualPropertySheet(final Class<? extends CyIdentifiable> targetDataType) {
		return getPropertiesPnl().getVisualPropertySheet(targetDataType);
	}
	
	@Override
	public VisualPropertySheet getSelectedVisualPropertySheet() {
		return getPropertiesPnl().getSelectedVisualPropertySheet();
	}
	
	@Override
	public JPopupMenu getContextMenu() {
		return getPropertiesPnl().getContextMenu();
	}
	
	@Override
	public Set<VisualPropertySheet> getVisualPropertySheets() {
		return getPropertiesPnl().getVisualPropertySheets();
	}
	
	@Override
	public void addVisualPropertySheet(final VisualPropertySheet sheet) {
		getPropertiesPnl().addVisualPropertySheet(sheet);
	}
	
	@Override
	public void setSelectedVisualPropertySheet(final VisualPropertySheet sheet) {
		getPropertiesPnl().setSelectedVisualPropertySheet(sheet);
	}
			
	@Override
	public JMenu getMapValueGeneratorsSubMenu() {
		return getPropertiesPnl().getMapValueGeneratorsSubMenu();
	}
	
	private void init() {
		setMinimumSize(new Dimension(420, 240));
		setPreferredSize(new Dimension(420, 385));
		
		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!isAquaLAF());
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
//				.addComponent(getStylesPnl().getComponent(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getPropertiesPnl().getComponent(), DEFAULT_SIZE, 280, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
//						.addComponent(getStylesPnl().getComponent(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getPropertiesPnl().getComponent(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				)
		);
	}
}
