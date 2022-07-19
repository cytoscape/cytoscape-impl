package org.cytoscape.view.table.internal.equation;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.equations.Equation;
import org.cytoscape.model.CyColumn;
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
	
	private final Map<Long,String> storedEquations = new HashMap<>(); // Key is column SUID.
	
	
	public EquationEditorDialogFactory(CyServiceRegistrar registrar) {
		this.registrar = registrar;
	}
	
	public void openEquationEditorDialog(BrowserTable browserTable) {
		long colSUID = getColSUID(browserTable);
		
		String initialEquation = storedEquations.get(colSUID);
		if(initialEquation == null) {
			initialEquation = getEquationInCell(browserTable);
		}
		
		String equationNotApplied = 
				EquationEditorMediator.openEquationEditorDialog(browserTable, initialEquation, registrar);
		
		if(equationNotApplied == null || equationNotApplied.isBlank())
			storedEquations.remove(colSUID);
		else
			storedEquations.put(colSUID, equationNotApplied.trim());
	}
	
	
	private static long getColSUID(BrowserTable browserTable) {
		int modelColIndex = browserTable.convertColumnIndexToModel(browserTable.getSelectedColumn());
		CyColumn column = browserTable.getBrowserTableModel().getCyColumn(modelColIndex);
		return column.getSUID();
	}
	
	
	private static String getEquationInCell(BrowserTable browserTable) {
		int cellRow = browserTable.getSelectedRow();
		int cellCol = browserTable.getSelectedColumn();
		
		var model = browserTable.getBrowserTableModel();
		
		try {
			CyRow row = model.getCyRow(cellRow);
			
			String colName = browserTable.getColumnName(cellCol);
			Object obj = row.getRaw(colName);
			
			if(obj instanceof Equation) {
				Equation equation = (Equation) obj;
				return equation.toString().trim().substring(1); // remove '='
			}
		} catch(Exception e) { }
		
		return null;
	}
}
