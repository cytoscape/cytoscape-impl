package org.cytoscape.view.vizmap.gui.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;

@SuppressWarnings("serial")
public class VizMapperTableDialog extends JDialog implements VisualPropertySheetContainer {

	private final ServicesUtil servicesUtil;
	private PropertySheetPanel propertySheetPanel;
	private JLabel titleLabel;
	private CyColumn targetColumn;
	
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
	
	public void setTargetColumn(CyColumn column) {
		var columnPresentation = servicesUtil.get(CyColumnPresentationManager.class);
		JLabel label = getTitleLabel();
		columnPresentation.setLabel(column.getName(), label::setIcon, text -> label.setText("Column Style for: " + text));
	}
	
	private JLabel getTitleLabel() {
		if(titleLabel == null) {
			titleLabel = new JLabel("Column Style for: ");
		}
		return titleLabel;
	}
	
	private void init() {
		setMinimumSize(new Dimension(420, 240));
		setPreferredSize(new Dimension(420, 585));
		
		JPanel panel = new JPanel();
		
		
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!isAquaLAF());
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addComponent(getTitleLabel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getPropertiesPnl().getComponent(), DEFAULT_SIZE, 280, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addComponent(getTitleLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getPropertiesPnl().getComponent(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				)
		);
		
		
		setLayout(new BorderLayout());
		add(panel, BorderLayout.CENTER);
	}
}
