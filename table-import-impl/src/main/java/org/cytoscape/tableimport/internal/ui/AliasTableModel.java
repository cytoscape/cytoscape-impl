package org.cytoscape.tableimport.internal.ui;

import javax.swing.table.DefaultTableModel;

class AliasTableModel extends DefaultTableModel {
	
	private static final long serialVersionUID = -4934304431569268487L;

	AliasTableModel(String[] columnNames, int rowNum) {
		super(columnNames, rowNum);
	}

	AliasTableModel(Object[][] data, Object[] colNames) {
		super(data, colNames);
	}

	AliasTableModel() {
		super();
	}

	@Override
	public Class<?> getColumnClass(int col) {
		if(this.getColumnCount()<col || this.getRowCount() == 0)
			return null;
		
		return getValueAt(0, col).getClass();
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		if (column == 0) {
			return true;
		} else {
			return false;
		}
	}
}