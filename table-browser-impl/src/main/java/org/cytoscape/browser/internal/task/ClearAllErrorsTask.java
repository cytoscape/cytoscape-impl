package org.cytoscape.browser.internal.task;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.browser.internal.util.TableBrowserUtil;
import org.cytoscape.equations.Equation;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractTableColumnTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.AbstractCyEdit;
import org.cytoscape.work.undo.UndoSupport;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class ClearAllErrorsTask extends AbstractTableColumnTask {

	private final List<ErrorEquation> deletedEquations = new ArrayList<>();
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public ClearAllErrorsTask(CyColumn column, CyServiceRegistrar serviceRegistrar) {
		super(column);
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor tm) {
		final CyTable table = column.getTable();
		final EquationCompiler compiler = serviceRegistrar.getService(EquationCompiler.class);
		final List<ErrorEquation> errorEquations = new ArrayList<>();
				
		for (var row : table.getAllRows()) {
			if (cancelled)
				return;
			
			var raw = row.getRaw(column.getName());

			if (raw instanceof Equation) {
				var eq = (Equation) raw;
				boolean success = compiler.compile(eq.toString(), TableBrowserUtil.getAttNameToTypeMap(table, null));
				
				//TODO: success is incorrectly set to yes on broken equations [=ABS(String)]
				if (!success || row.get(column.getName(), column.getType()) == null)
					errorEquations.add(new ErrorEquation(row, column.getName(), eq));
			}
		}
		
		for (var err : errorEquations) {
			if (cancelled) {
				restoreDeletedEquations();
				return;
			}
			
			deletedEquations.add(err);
			err.clear();
		}
		
		if (!deletedEquations.isEmpty()) {
			var undoSupport = serviceRegistrar.getService(UndoSupport.class);
			undoSupport.postEdit(new ClearErrorsEdit(column.getName(), deletedEquations));
		}
	}

	private void restoreDeletedEquations() {
		for (var err : deletedEquations)
			err.restore();
	}
}

class ErrorEquation {
	
	private CyRow row;
	private String columnName;
	private Equation equation;
	
	ErrorEquation(CyRow row, String columnName, Equation equation) {
		this.row = row;
		this.columnName = columnName;
		this.equation = equation;
	}
	
	void clear() {
		row.set(columnName, null);
	}
	
	void restore() {
		row.set(columnName, equation);
	}
}

class ClearErrorsEdit extends AbstractCyEdit {

	private List<ErrorEquation> errEquations;

	public ClearErrorsEdit(String columnName, List<ErrorEquation> errEquations) {
		super("Clear all errors in column \"" + columnName + "\"");
		this.errEquations = errEquations;
	}

	@Override
	public void undo() {
		for (var err : errEquations)
			err.restore();
	}

	@Override
	public void redo() {
		for (var err : errEquations)
			err.clear();
	}
}
