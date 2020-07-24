package org.cytoscape.browser.internal.task;

import org.cytoscape.browser.internal.util.TableBrowserUtil;

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

import org.cytoscape.equations.Equation;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractTableColumnTaskFactory;
import org.cytoscape.work.TaskIterator;


public final class ClearAllErrorsTaskFactory extends AbstractTableColumnTaskFactory {
	
	private final CyServiceRegistrar serviceRegistrar;

	public ClearAllErrorsTaskFactory(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public TaskIterator createTaskIterator(CyColumn column) {
		if (column == null)
			throw new IllegalStateException("you forgot to set the CyColumn on this task factory.");
		return new TaskIterator(new ClearAllErrorsTask(column, serviceRegistrar));
	}

	@Override
	public boolean isReady(CyColumn column) {
		CyTable table = column.getTable();
		EquationCompiler compiler = serviceRegistrar.getService(EquationCompiler.class);
				
		for (CyRow row : table.getAllRows()) {
			Object raw = row.getRaw(column.getName());
			
			if (raw instanceof Equation) {
				Equation eq = (Equation) raw;
				boolean success = compiler.compile(eq.toString(), TableBrowserUtil.getAttNameToTypeMap(table, null));
				//TODO: success is incorrectly set to yes on broken equations [=ABS(String)]
				if (!success || row.get(column.getName(), column.getType()) == null)
					return true;
			}
		}
		
		return false;
	}
}
