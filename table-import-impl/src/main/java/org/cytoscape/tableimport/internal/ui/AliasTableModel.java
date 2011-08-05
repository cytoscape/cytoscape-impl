package org.cytoscape.tableimport.internal.ui;

import javax.swing.table.DefaultTableModel;

class AliasTableModel extends DefaultTableModel {
	AliasTableModel(String[] columnNames, int rowNum) {
		super(columnNames, rowNum);
	}

	AliasTableModel(Object[][] data, Object[] colNames) {
		super(data, colNames);
	}

	AliasTableModel() {
		super();
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param col DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Class getColumnClass(int col) {
		return getValueAt(0, col).getClass();
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param row DOCUMENT ME!
	 * @param column DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public boolean isCellEditable(int row, int column) {
		if (column == 0) {
			return true;
		} else {
			return false;
		}
	}
}