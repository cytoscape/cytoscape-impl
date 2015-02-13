package org.cytoscape.tableimport.internal.ui;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

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
		if (this.getColumnCount() < col || this.getRowCount() == 0)
			return null;
		
		return getValueAt(0, col).getClass();
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return (column == 0);
	}
}