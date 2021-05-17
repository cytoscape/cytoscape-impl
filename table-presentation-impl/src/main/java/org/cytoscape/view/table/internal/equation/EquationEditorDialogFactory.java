package org.cytoscape.view.table.internal.equation;

import org.cytoscape.equations.Equation;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.table.internal.impl.BrowserTable;

/*
 * #%L
 * Cytoscape Table Presentation Impl (table-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2021 The Cytoscape Consortium
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

public class EquationEditorDialogFactory {
	
	private final CyServiceRegistrar registrar;
	
	public EquationEditorDialogFactory(CyServiceRegistrar registrar) {
		this.registrar = registrar;
	}
	
	public void openEquationEditorDialog(BrowserTable browserTable) {
		String equation = getInitialEquation(browserTable);
		EquationEditorMediator.openEquationEditorDialog(browserTable, equation, registrar);
	}
	
	private static String getInitialEquation(BrowserTable browserTable) {
		int cellRow = browserTable.getSelectedRow();
		int cellCol = browserTable.getSelectedColumn();
		
		try {
			String colName = browserTable.getColumnName(cellCol);
			CyRow row = browserTable.getBrowserTableModel().getCyRow(cellRow);
			
			Object obj = row.getRaw(colName);
			if(obj instanceof Equation) {
				Equation equation = (Equation) obj;
				return equation.toString().trim().substring(1); // remove '='
			}
		} catch(Exception e) { }
		
		return null;
	}
}
