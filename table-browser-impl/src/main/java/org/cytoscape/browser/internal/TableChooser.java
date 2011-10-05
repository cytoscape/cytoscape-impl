package org.cytoscape.browser.internal;

import static org.cytoscape.browser.internal.GlobalTableBrowser.*;
import static org.cytoscape.browser.internal.AbstractTableBrowser.SELECTED_ITEM_BACKGROUND_COLOR;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;

import org.cytoscape.model.CyTable;

/**
 * Combo Box to choose tables to be displayed.
 * 
 */
public class TableChooser extends JComboBox {

	private static final long serialVersionUID = 5141179419574773453L;

	private final Map<CyTable, String> tableToStringMap;
	
	

	TableChooser() {
		tableToStringMap = new HashMap<CyTable, String>();
		setModel(new GlobalTableComboBoxModel(tableToStringMap));
		setRenderer(new TableChooserCellRenderer(tableToStringMap));
	}

	final class GlobalTableComboBoxModel extends DefaultComboBoxModel {

		private static final long serialVersionUID = -5435833047656563358L;

		private final Comparator<CyTable> tableComparator = new TableComparator();
		private final Map<CyTable, String> tableToStringMap;
		private final List<CyTable> tables;

		GlobalTableComboBoxModel(final Map<CyTable, String> tableToStringMap) {
			this.tableToStringMap = tableToStringMap;
			tables = new ArrayList<CyTable>();
		}

		private void updateTableToStringMap() {
			tableToStringMap.clear();
			for (final CyTable table : tables)
				tableToStringMap.put(table, "Global Table: " + table.getTitle());
		}

		@Override
		public int getSize() {
			return tables.size();
		}

		
		@Override
		public Object getElementAt(int index) {
			return tables.get(index);
		}

		public void addAndSetSelectedItem(final CyTable newTable) {
			if (!tables.contains(newTable)) {
				tables.add(newTable);
				Collections.sort(tables, tableComparator);
				updateTableToStringMap();
				fireContentsChanged(this, 0, tables.size() - 1);
			}

			// This is necessary to avoid deadlock!
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setSelectedItem(newTable);
				}
			});
		}


		public void removeItem(CyTable deletedTable) {

			if (tables.contains(deletedTable)) {
				tables.remove(deletedTable);
				if (tables.size() != 0)
					Collections.sort(tables, tableComparator);
			}

			if (tables.size() == 0) {
				setSelectedItem(null);
			} else {
				setSelectedItem(tables.get(0));
			}
		}
	}

	private static final class TableComparator implements Comparator<CyTable> {
		public int compare(final CyTable table1, final CyTable table2) {
			return table1.getTitle().compareTo(table2.getTitle());
		}
	}

	private final class TableChooserCellRenderer extends JLabel implements ListCellRenderer {

		private static final long serialVersionUID = 8732696308031936737L;

		private final Map<CyTable, String> tableToStringMap;

		TableChooserCellRenderer(final Map<CyTable, String> tableToStringMap) {
			this.tableToStringMap = tableToStringMap;
		}

		public Component getListCellRendererComponent(final JList list, // the
																		// list
				final Object value, // value to display
				final int index, // cell index
				final boolean isSelected, // is the cell selected
				final boolean cellHasFocus) // does the cell have focus
		{
			final CyTable table = (CyTable) value;
			this.setFont(GLOBAL_FONT);
			
			if (tableToStringMap.containsKey(table))
				setText(tableToStringMap.get(table));
			else
				setText(table == null ? "" : table.getTitle());

			if (isSelected) {
				setBackground(SELECTED_ITEM_BACKGROUND_COLOR);
				setForeground(GLOBAL_TABLE_COLOR);
			} else {
				setBackground(Color.WHITE);
				setForeground(GLOBAL_TABLE_COLOR);
			}

			setEnabled(list.isEnabled());
			setFont(list.getFont());
			setOpaque(true);

			return this;
		}
	}
}