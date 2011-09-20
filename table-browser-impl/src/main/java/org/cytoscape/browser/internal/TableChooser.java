package org.cytoscape.browser.internal;


import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.CyTableManager;


public class TableChooser extends JComboBox {
	
	private static final long serialVersionUID = 5141179419574773453L;
	
	private final Map<CyTable, String> tableToStringMap;

	TableChooser(final CyTableManager tableManager, final CyNetworkManager networkManager) {
		tableToStringMap = new HashMap<CyTable, String>();
		setModel(new MyComboBoxModel(tableManager, networkManager, tableToStringMap));
		setRenderer(new MyCellRenderer(tableToStringMap));
	}
}


final class MyComboBoxModel extends DefaultComboBoxModel {
	
	private static final long serialVersionUID = -5435833047656563358L;
	
	private final static Comparator<CyTable> tableComparator = new TableComparator();
	
	private final CyTableManager tableManager;
	private final CyNetworkManager networkManager;
	private final Map<CyTable, String> tableToStringMap;
	
	private List<CyTable> tables;
	private Set<CyTable> oldSet;

	MyComboBoxModel(final CyTableManager tableManager, final CyNetworkManager networkManager,
			final Map<CyTable, String> tableToStringMap)
	{
		this.tableManager     = tableManager;
		this.networkManager   = networkManager;
		this.tableToStringMap = tableToStringMap;

		oldSet = tableManager.getAllTables(/* includePrivate = */ false);
		tables = new ArrayList<CyTable>(oldSet);
		Collections.sort(tables, tableComparator);
		updateTableToStringMap();
	}

	private void updateTableToStringMap() {
		tableToStringMap.clear();

		final Set<CyNetwork> networks = networkManager.getNetworkSet();
		for (final CyNetwork network : networks) {
			final CyTable networkTable = network.getDefaultNetworkTable();
			final String networkName =
				networkTable.getAllRows().get(0).get(CyTableEntry.NAME, String.class);
			tableToStringMap.put(networkTable, networkName + " (network)");
			tableToStringMap.put(network.getDefaultNodeTable(), networkName + " (nodes)");
			tableToStringMap.put(network.getDefaultEdgeTable(), networkName + " (edges)");
		}
	}

	public int getSize() {
		final Set<CyTable> tableSet = tableManager.getAllTables(/* includePrivate = */ false);
		if (!tableSet.equals(oldSet)) {
			oldSet = tableSet;
			fireContentsChanged(this, 0, tableSet.size() - 1);
			tables = new ArrayList<CyTable>(tableSet.size());
			for (final CyTable table : tableSet)
				tables.add(table);
			Collections.sort(tables, tableComparator);
			updateTableToStringMap();
		}

		return tables.size();
	}

	public Object getElementAt(int index) {
		return tables.get(index);
	}

	public void addAndSetSelectedItem(final CyTable newTable) {
		if (!tables.contains(newTable)) {
			tables.add(newTable);
			Collections.sort(tables, tableComparator);
			oldSet.clear();
			for (final CyTable table : tables)
				oldSet.add(table);
			updateTableToStringMap();
			fireContentsChanged(this, 0, oldSet.size() - 1);
		}
		
		// This is necessary to avoid deadlock!
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setSelectedItem(newTable);
			}
		});		
	}
	
	public void removeItem(CyTable deletedTable){
		
		if (tables.contains(deletedTable)) {
			tables.remove(deletedTable);
			
			oldSet.clear();
			if (tables.size() != 0){
				Collections.sort(tables, tableComparator);				
				for (final CyTable table : tables)
					oldSet.add(table);
			}
		}

		if (tables.size() == 0){
			setSelectedItem(null);			
		}
		else {
			setSelectedItem(tables.get(0));
		}
	}
}

final class TableComparator implements Comparator<CyTable> {
	public int compare(final CyTable table1, final CyTable table2) {
		return table1.getTitle().compareTo(table2.getTitle());
	}
}


class MyCellRenderer extends JLabel implements ListCellRenderer {
	private final Map<CyTable, String> tableToStringMap;

	MyCellRenderer(final Map<CyTable, String> tableToStringMap) {
		this.tableToStringMap = tableToStringMap;
	}

	public Component getListCellRendererComponent(final JList list,              // the list
						      final Object value,            // value to display
						      final int index,               // cell index
						      final boolean isSelected,      // is the cell selected
						      final boolean cellHasFocus)    // does the cell have focus
	{
		final CyTable table = (CyTable)value;
		if (tableToStringMap.containsKey(table))
			setText(tableToStringMap.get(table));
		else
			setText(table == null ? "" : table.getTitle());

		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		setEnabled(list.isEnabled());
		setFont(list.getFont());
		setOpaque(true);

		return this;
	}
}