package org.cytoscape.view.vizmap.gui.internal.view;

import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.view.vizmap.gui.internal.ColumnSpec;
import org.cytoscape.view.vizmap.gui.internal.GraphObjectType;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.view.util.IconUtil;

@SuppressWarnings("serial")
public class ColumnStylePicker {
	
	public static enum Action {
		UPDATE,
		CREATE,
		DELETE
	}
	
	public static final float ICON_FONT_SIZE = 22.0f;

	private final ServicesUtil servicesUtil;
	
	private JPanel columnPanel;
	private JTable jtable;
	private JButton addButton;
	private JButton deleteButton;
	
	private List<BiConsumer<ColumnSpec,Action>> columnSelectionListeners = new ArrayList<>(2);

	
	public ColumnStylePicker(ServicesUtil servicesUtil) {
		this.servicesUtil = servicesUtil;
	}
	
	
	public void addColumnSelectionListener(BiConsumer<ColumnSpec,Action> listener) {
		columnSelectionListeners.add(listener);
	}
	
	public void removeColumnSelectionListener(BiConsumer<ColumnSpec,Action> listener) {
		columnSelectionListeners.remove(listener);
	}
	
	
	public JComponent getComponent() {
		return getColumnPanel();
	}
	
	
	public JPanel getColumnPanel() {
		if (columnPanel == null) {
			columnPanel = new JPanel();
			
			JLabel title = new JLabel("Styled Columns");
			JTable table = getJTable();
			JScrollPane scrollPane = new JScrollPane(table);
			
			var layout = new GroupLayout(columnPanel);
			columnPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(!isAquaLAF());
			
			layout.setHorizontalGroup(layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup()
					.addComponent(title)
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getAddButton())
					.addGap(5)
					.addComponent(getDeleteButton())
				)
				.addComponent(scrollPane)
			);
			
			layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
					.addComponent(title)
					.addComponent(getAddButton())
					.addComponent(getDeleteButton())
				)
				.addComponent(scrollPane, 300, 300, 300)
			);
		}
		return columnPanel;
	}
	
	
	private JButton getAddButton() {
		if (addButton == null) {
			var iconManager = servicesUtil.get(IconManager.class);
			addButton = new JButton(IconManager.ICON_PLUS);
			addButton.setFont(iconManager.getIconFont(ICON_FONT_SIZE * 4/5));
			addButton.setToolTipText("Add Column Style...");
			addButton.setBorderPainted(false);
			addButton.setContentAreaFilled(false);
			addButton.setFocusPainted(false);
			addButton.setBorder(BorderFactory.createEmptyBorder());
			
			addButton.addActionListener(e -> {
				// MKTODO what if there are no unstyled columns???
				var dialog = new ColumnStyleAddColumnPopup(servicesUtil);
				var location = addButton.getLocationOnScreen();
				dialog.setLocation(location.x + 15, location.y + 5 + addButton.getHeight());
				dialog.setVisible(true);
				
				dialog.getAddButton().addActionListener(ev -> {
					var colName = dialog.getColumnName();
					var tableType = dialog.getTableType();
					dialog.setVisible(false);
					fireColumnStyleEvent(Action.CREATE, new ColumnSpec(tableType, colName));
				});
			});
		}
		
		return addButton;
	}
	
	private JButton getDeleteButton() {
		if (deleteButton == null) {
			var iconManager = servicesUtil.get(IconManager.class);
			deleteButton = new JButton(IconManager.ICON_TRASH);
			deleteButton.setFont(iconManager.getIconFont(ICON_FONT_SIZE * 4/5));
			deleteButton.setToolTipText("Delete Selected Column Style...");
			deleteButton.setBorderPainted(false);
			deleteButton.setContentAreaFilled(false);
			deleteButton.setFocusPainted(false);
			deleteButton.setBorder(BorderFactory.createEmptyBorder());
			
			deleteButton.addActionListener(e -> {
				fireColumnStyleEvent(Action.DELETE);
			});
		}
		
		return deleteButton;
	}
	
	
	private JTable getJTable() {
		if(jtable == null) {
			var model = new ColumnStyleTableModel();
			var renderer = new ColumnStyleColumnRenderer();
			
			jtable = new JTable(model);
			jtable.setDefaultRenderer(String.class, renderer);
			jtable.setDefaultRenderer(GraphObjectType.class, renderer);
			
			JTableHeader header = jtable.getTableHeader();
			header.setReorderingAllowed(false);
			jtable.getColumnModel().getColumn(0).setPreferredWidth(30);
			
			jtable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			jtable.getSelectionModel().addListSelectionListener(e -> {
				if(e.getValueIsAdjusting())
					return;
				fireColumnStyleEvent(Action.UPDATE);
			});
		}
		return jtable;
	}
	
	
	private void fireColumnStyleEvent(Action action) {
		int row = jtable.getSelectedRow();
		if(row < 0)
			return;
		ColumnSpec column = ((ColumnStyleTableModel) jtable.getModel()).getColumnSpec(row);
		fireColumnStyleEvent(action, column);
	}
	
	private void fireColumnStyleEvent(Action action, ColumnSpec column) {
		for(var listener : columnSelectionListeners) {
			listener.accept(column, action);
		}
	}
	
	
	public void updateColumns(List<ColumnSpec> columns, ColumnSpec selectedCol) {
		var model = new ColumnStyleTableModel(columns);
		getJTable().setModel(model);
		int row = model.getRowFor(selectedCol);
		getJTable().getSelectionModel().setSelectionInterval(row, row);
	}
	
	
	public ColumnSpec getSelectedColumn() {
		int row = getJTable().getSelectedRow();
		if(row >= 0) {
			return (ColumnSpec) getJTable().getModel().getValueAt(row, 1);
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
			if (value instanceof GraphObjectType type) {
				var t = type.type();
				if(t == CyNode.class) {
					setText("Node");
					setIcon(nodeTableIcon);
				} else if(t == CyEdge.class) {
					setText("Edge");
					setIcon(edgeTableIcon);
				} else if(t == CyNetwork.class) {
					setText("Network");
					setIcon(netTableIcon);
				}
			} else if (value instanceof String column) {
				columnPresentationManager.setLabel(column, this);
			} else  {
				setText("-- None --");
				setIcon(null);
			}
			return this;
		}
	}
	
	
	private class ColumnStyleTableModel extends AbstractTableModel {
		
		private final List<ColumnSpec> columns;
		
		private ColumnStyleTableModel(List<ColumnSpec> columns) {
			this.columns = columns;
		}

		private ColumnStyleTableModel() {
			this(List.of());
		}
		
		public int getRowFor(ColumnSpec column) {
			return columns.indexOf(column);
		}
		
		public ColumnSpec getColumnSpec(int row) {
			return columns.get(row);
		}
		
		@Override
		public int getRowCount() {
			return columns.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}
		
		@Override
		public Object getValueAt(int row, int col) {
			return switch(col) {
				case 0 -> columns.get(row).tableType();
				case 1 -> columns.get(row).columnName();
				default -> null;
			};
		}
		
		@Override
		public Class<?> getColumnClass(int col) {
			return switch(col) {
				case 0 -> GraphObjectType.class;
				case 1 -> String.class;
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
