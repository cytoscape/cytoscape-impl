package org.cytoscape.view.vizmap.gui.internal.view;

import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyColumnComboBox;
import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.vizmap.TableVisualMappingManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.internal.GraphObjectType;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;

@SuppressWarnings("serial")
public class ColumnStyleAddColumnPopup extends JDialog {

	private final ServicesUtil servicesUtil;
	
	private JButton addButton;
	private JButton cancelButton;
	private JComboBox<String> tableCombo;
	private JComboBox<CyColumn> colCombo;
	
	public ColumnStyleAddColumnPopup(ServicesUtil servicesUtil) {
		this.servicesUtil = servicesUtil;
		setTitle("Add Column Style");
		initComponents();
		pack();
		
		addWindowFocusListener(new WindowFocusListener() {
			@Override public void windowGainedFocus(WindowEvent e) { }
			@Override public void windowLostFocus(WindowEvent e) {
				setVisible(false);
			}
		});
	}
	
	
	public JButton getAddButton() {
		if(addButton == null) {
			addButton = new JButton("Add");
		}
		return addButton;
	}
	
	public JButton getCancelButton() {
		if(cancelButton == null) {
			cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(e -> setVisible(false));
		}
		return cancelButton;
	}
	
	
	public GraphObjectType getTableType() {
		var tabName = tableCombo.getItemAt(tableCombo.getSelectedIndex());
		return "Node".equals(tabName) ? GraphObjectType.node() : GraphObjectType.edge();
	}
	
	public String getColumnName() {
		return colCombo.getItemAt(colCombo.getSelectedIndex()).getName();
	}
	
	
	private void initComponents() {
		JLabel tableLabel = new JLabel("Table:");
		JLabel colLabel = new JLabel("Column:");
		JButton addButton = getAddButton();
		JButton cancelButton = getCancelButton();
		JPanel buttonPanel = LookAndFeelUtil.createOkCancelPanel(addButton, cancelButton);
		
		var presentationManager = servicesUtil.get(CyColumnPresentationManager.class);
		
		tableCombo = new JComboBox<>(new String[] {"Node", "Edge"});
		colCombo = new CyColumnComboBox(presentationManager, List.of());

		final int comboWidth = 300;
		
		updateColumns();
		tableCombo.addActionListener(e -> updateColumns());
		
		addButton.addActionListener(e -> {
			var col = colCombo.getItemAt(colCombo.getSelectedIndex());
			//addColumnStyle(col);
		});
		
		var layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(!isAquaLAF());
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(tableLabel)
				.addComponent(tableCombo)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(colLabel)
				.addComponent(colCombo)
			)
			.addGap(5)
			.addComponent(buttonPanel) 
		);
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addGroup(layout.createSequentialGroup()
				.addComponent(tableLabel)
				.addComponent(tableCombo, comboWidth, comboWidth, comboWidth)
			)
			.addGroup(layout.createSequentialGroup()
				.addComponent(colLabel)
				.addComponent(colCombo, comboWidth, comboWidth, comboWidth)
			)
			.addComponent(buttonPanel)
		);
		
		layout.linkSize(SwingConstants.HORIZONTAL, tableLabel, colLabel);
		layout.linkSize(SwingConstants.HORIZONTAL, tableCombo, colCombo);
	}
	
	
	private void updateColumns() {
		var appManager = servicesUtil.get(CyApplicationManager.class);
		var tableVMM = servicesUtil.get(TableVisualMappingManager.class);
		var vmm = servicesUtil.get(VisualMappingManager.class);
		
		var netStyle = vmm.getCurrentVisualStyle();
		var network = appManager.getCurrentNetwork();
		
		var tableName = tableCombo.getItemAt(tableCombo.getSelectedIndex());
		
		CyTable table = null;
		Class<? extends CyIdentifiable> tableType = null;
		if("Node".equals(tableName)) {
			table = network.getDefaultNodeTable();
			tableType = CyNode.class;
		} else if("Edge".equals(tableName)) {
			table = network.getDefaultEdgeTable();
			tableType = CyEdge.class;
		}
		
		colCombo.removeAllItems();
		
		if(table != null) {
			List<CyColumn> columns = new ArrayList<>(table.getColumns());
			columns.sort(comparingName());
			
			for(var col : columns) {
				String name = col.getName();
				if(CyNetwork.SUID.equals(name) || CyNetwork.SELECTED.equals(name))
					continue;
				
				var existingStyle = tableVMM.getAssociatedColumnVisualStyle(netStyle, tableType, name);
				if(existingStyle != null)
					continue;
				
				colCombo.addItem(col);
			};
		}
	}
	
	// Sorts the same as ColumnSpec
	private static Comparator<CyColumn> comparingName() {
		return (a, b) -> a.getName().compareToIgnoreCase(b.getName());
	}
	
	
}
