package org.cytoscape.view.vizmap.gui.internal.view;

import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
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
import org.cytoscape.model.CyTable;
import org.cytoscape.util.swing.LookAndFeelUtil;
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
		
		initComponents();
		setTitle("Add Column Style");
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
		if("Node".equals(tabName))
			return GraphObjectType.node();
		else
			return GraphObjectType.edge();
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

		Runnable setColumns = () -> {
			var network = servicesUtil.get(CyApplicationManager.class).getCurrentNetwork();
			
			colCombo.removeAllItems();
			CyTable table = null;
			
			var tableName = tableCombo.getItemAt(tableCombo.getSelectedIndex());
			if("Node".equals(tableName)) {
				table = network.getDefaultNodeTable();
			} else if("Edge".equals(tableName)) {
				table = network.getDefaultEdgeTable();
			}
			
			if(table != null) {
				for(var col : table.getColumns()) {
					// MKTODO filter out columns that already have a style.
					if(!"SUID".equals(col.getName())) {
						colCombo.addItem(col);
					}
				};
			}
		};
		
		final int comboWidth = 300;
		
		setColumns.run();
		tableCombo.addActionListener(e -> setColumns.run());
		
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
}
