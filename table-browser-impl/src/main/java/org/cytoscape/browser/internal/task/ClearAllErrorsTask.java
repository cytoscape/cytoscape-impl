package org.cytoscape.browser.internal.task;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
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

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.browser.internal.util.TableBrowserUtil;
import org.cytoscape.browser.internal.view.AbstractTableBrowser;
import org.cytoscape.browser.internal.view.BrowserTable;
import org.cytoscape.equations.Equation;
import org.cytoscape.equations.EquationCompiler;/*

 * Cytoscape Table Browser Impl (table-browser-impl)
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
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractTableColumnTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.AbstractCyEdit;
import org.cytoscape.work.undo.UndoSupport;

public class ClearAllErrorsTask extends AbstractTableColumnTask {

	private final List<ErrorEquation> deletedEquations = new ArrayList<>();
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public ClearAllErrorsTask(final CyColumn column, final CyServiceRegistrar serviceRegistrar) {
		super(column);
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(final TaskMonitor tm) throws Exception {
		final CyTable table = column.getTable();
		final EquationCompiler compiler = serviceRegistrar.getService(EquationCompiler.class);
		final List<ErrorEquation> errorEquations = new ArrayList<>();
				
		for (CyRow row : table.getAllRows()) {
			if (cancelled)
				return;
			
			final Object raw = row.getRaw(column.getName());

			if (raw instanceof Equation) {
				final Equation eq = (Equation) raw;
				final boolean success =
						compiler.compile(eq.toString(), TableBrowserUtil.getAttNameToTypeMap(table, null));
				
				if (!success || table.getLastInternalError() != null)
					errorEquations.add(new ErrorEquation(row, column.getName(), eq));
			}
		}
		
		for (ErrorEquation err : errorEquations) {
			if (cancelled) {
				restoreDeletedEquations();
				return;
			}
			
			deletedEquations.add(err);
			err.clear();
		}
		
		if (!deletedEquations.isEmpty()) {
			final UndoSupport undoSupport = serviceRegistrar.getService(UndoSupport.class);
			undoSupport.postEdit(new ClearErrorsEdit(column.getName(), deletedEquations));
		}
	}

	public static BrowserTable getBrowserTable(final CyTable table, final CyServiceRegistrar serviceRegistrar) {
		final CySwingApplication swingAppManager = serviceRegistrar.getService(CySwingApplication.class);
		final CytoPanel cytoPanel = swingAppManager.getCytoPanel(CytoPanelName.SOUTH);
		
		if (cytoPanel != null) {
			final int count = cytoPanel.getCytoPanelComponentCount();
			
			for (int i = 0; i < count; i++) {
				final Component c = cytoPanel.getComponentAt(i);
				
				if (c instanceof AbstractTableBrowser) {
					final AbstractTableBrowser tableBrowser = (AbstractTableBrowser) c;
					final BrowserTable browserTable = tableBrowser.getBrowserTable(table);
					
					if (browserTable != null)
						return browserTable;
				}
			}
		}
		
		return null;
	}
	
	private void restoreDeletedEquations() {
		for (ErrorEquation err : deletedEquations)
			err.restore();
	}
}

class ErrorEquation {
	
	private CyRow row;
	private String columnName;
	private Equation equation;
	
	ErrorEquation(final CyRow row, final String columnName, final Equation equation) {
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

	public ClearErrorsEdit(final String columnName, final List<ErrorEquation> errEquations) {
		super("Clear all errors in column \"" + columnName + "\"");
		this.errEquations = errEquations;
	}

	@Override
	public void undo() {
		for (ErrorEquation err : errEquations)
			err.restore();
	}

	@Override
	public void redo() {
		for (ErrorEquation err : errEquations)
			err.clear();
	}
}
