package org.cytoscape.view.vizmap.gui.internal;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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
