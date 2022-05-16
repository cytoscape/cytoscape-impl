package org.cytoscape.view.vizmap.gui.internal.view;

import static org.cytoscape.util.swing.LookAndFeelUtil.createTitledBorder;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.view.util.IconUtil;

@SuppressWarnings("serial")
public class ColumnStylePicker {

	private final ServicesUtil servicesUtil;
	
	private JPanel columnPanel;
	private JTable jtable;
	
	private List<Consumer<CyColumn>> columnSelectionListeners = new ArrayList<>(2);
	
	
	public ColumnStylePicker(ServicesUtil servicesUtil) {
		this.servicesUtil = servicesUtil;
	}
	
	
	public void addColumnSelectionListener(Consumer<CyColumn> listener) {
		columnSelectionListeners.add(listener);
	}
	
	public void removeColumnSelectionListener(Consumer<CyColumn> listener) {
		columnSelectionListeners.remove(listener);
	}
	
	
	public JComponent getComponent() {
		return getColumnPanel();
	}
	
	
	public JPanel getColumnPanel() {
		if (columnPanel == null) {
			columnPanel = new JPanel();
			columnPanel.setOpaque(!isAquaLAF());
			columnPanel.setBorder(createTitledBorder("Styled Columns"));
			
			JTable table = getJTable();
			JScrollPane scrollPane = new JScrollPane(table);
			
			var layout = new GroupLayout(columnPanel);
			columnPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(!isAquaLAF());
			
			layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(scrollPane)
			);
			
			layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(scrollPane, 300, 300, 300)
			);
		}
		return columnPanel;
	}
	
	
	
	private JTable getJTable() {
		if(jtable == null) {
			var model = new ColumnStyleTableModel();
			var renderer = new ColumnStyleColumnRenderer();
			
			jtable = new JTable(model);
			jtable.setDefaultRenderer(CyTable.class, renderer);
			jtable.setDefaultRenderer(CyColumn.class, renderer);

			JTableHeader header = jtable.getTableHeader();
			header.setReorderingAllowed(false);
			jtable.getColumnModel().getColumn(0).setPreferredWidth(30);
			
			jtable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jtable.getSelectionModel().addListSelectionListener(e -> {
				if(columnSelectionListeners.isEmpty())
					return;
				int row = e.getFirstIndex();
				CyColumn column = (CyColumn) jtable.getModel().getValueAt(row, 1);
				for(var listener : columnSelectionListeners) {
					listener.accept(column);
				}
			});
		}
		return jtable;
	}
	
	
	public void updateColumns(Map<CyTable,List<CyColumn>> columnMap, CyColumn selectedCol) {
		int width = getJTable().getColumnModel().getColumn(0).getWidth();
		var model = new ColumnStyleTableModel(columnMap);
		int row = model.getRowFor(selectedCol);
		getJTable().setModel(model);
		getJTable().getSelectionModel().setSelectionInterval(row, row);
		jtable.getColumnModel().getColumn(0).setPreferredWidth(width);
	}
	
	
	public CyColumn getSelectedColumn() {
		int row = getJTable().getSelectedRow();
		if(row >= 0) {
			return (CyColumn) getJTable().getModel().getValueAt(row, 1);
		}
		return null;
	}
	
	
	public class ColumnStyleColumnRenderer extends DefaultTableCellRenderer {

		final CyNetworkTableManager netTableManager;
		final CyColumnPresentationManager columnPresentationManager;
		final Icon nodeTableIcon, edgeTableIcon, netTableIcon, globalTableIcon;
		
		private ColumnStyleColumnRenderer() {
			netTableManager = servicesUtil.get(CyNetworkTableManager.class);
			columnPresentationManager = servicesUtil.get(CyColumnPresentationManager.class);
			
			var iconManager = servicesUtil.get(IconManager.class);
			globalTableIcon = new TextIcon(IconManager.ICON_TABLE, iconManager.getIconFont(14.0f), 16, 16);
			var iconFont = iconManager.getIconFont(IconUtil.CY_FONT_NAME, 14.0f);
			nodeTableIcon = new TextIcon(IconUtil.NODE_TABLE, iconFont, 16, 16);
			edgeTableIcon = new TextIcon(IconUtil.EDGE_TABLE, iconFont, 16, 16);
			netTableIcon  = new TextIcon(IconUtil.NETWORK_TABLE, iconFont, 16, 16);
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable jtable, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
			super.getTableCellRendererComponent(jtable, value, isSelected, hasFocus, row, col);
			
			if (value instanceof CyTable table) {
				var text = table.getTitle();
				var namespace = netTableManager.getTableNamespace(table);
				var type = netTableManager.getTableType(table);
				
				var icon = globalTableIcon;
				
				if (type != null && CyNetwork.DEFAULT_ATTRS.equals(namespace))
					text = type.getSimpleName().replace("Cy", "");
				
				if (type == CyNode.class)
					icon = nodeTableIcon;
				else if (type == CyEdge.class)
					icon = edgeTableIcon;
				else if (type == CyNetwork.class)
					icon = netTableIcon;
				
				setText(text);
				setIcon(icon);
			} else if (value instanceof CyColumn column) {
				columnPresentationManager.setLabel(column.getName(), this);
			} else  {
				setText("-- None --");
				setIcon(null);
			}
			return this;
		}
	}
	
	
	private class ColumnStyleTableModel extends AbstractTableModel {
		
		private final Map<Integer,CyTable> tables = new HashMap<>();
		private final Map<Integer,CyColumn> columns = new HashMap<>();
		private final int rowCount;
		
		private ColumnStyleTableModel(Map<CyTable,List<CyColumn>> columnMap) {
			int i = 0;
			for(var entry : columnMap.entrySet()) {
				var table = entry.getKey();
				for(var column : entry.getValue()) {
					tables.put(i, table);
					columns.put(i, column);
					i++;
				}
			}
			rowCount = i;
		}
		
		
		public int getRowFor(CyColumn column) {
			for(var entry : columns.entrySet()) {
				if(entry.getValue() == column) {
					return entry.getKey();
				}
			}
			return -1;
		}
		
		private ColumnStyleTableModel() {
			this(Map.of());
		}
		
		@Override
		public int getRowCount() {
			return rowCount;
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Object getValueAt(int row, int col) {
			return switch(col) {
				case 0 -> tables.get(row);
				case 1 -> columns.get(row);
				default -> null;
			};
		}
		
		@Override
		public Class<?> getColumnClass(int col) {
			return switch(col) {
				case 0 -> CyTable.class;
				case 1 -> CyColumn.class;
				default -> null;
			};
		}
		
		@Override
		public String getColumnName(int col) {
			return switch(col) {
				case 0 -> "Table";
				case 1 -> "Column";
				default -> null;
			};
		}
	}
	
}
