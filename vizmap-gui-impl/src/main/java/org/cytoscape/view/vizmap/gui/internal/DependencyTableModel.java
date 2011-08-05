package org.cytoscape.view.vizmap.gui.internal;

import javax.swing.table.DefaultTableModel;

public class DependencyTableModel extends DefaultTableModel {

	private static final long serialVersionUID = 1014856844094238247L;

	private static final Class<?>[] types = new Class[] { java.lang.Boolean.class, java.lang.String.class };
	private static final String[] colNames = new String[] { "Enable", "Description" };
	
	private static final boolean[] canEdit = new boolean[] { true, false };

	protected DependencyTableModel() {
		super();
		this.addColumn(colNames[0]);
		this.addColumn(colNames[1]);
	}

	public Class<?> getColumnClass(int columnIndex) {
		return types[columnIndex];
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return canEdit[columnIndex];
	}
}
