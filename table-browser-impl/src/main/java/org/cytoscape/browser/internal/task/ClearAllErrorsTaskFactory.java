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

import org.cytoscape.browser.internal.util.TableBrowserUtil;
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

	public ClearAllErrorsTaskFactory(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public TaskIterator createTaskIterator(final CyColumn column) {
		if (column == null)
			throw new IllegalStateException("you forgot to set the CyColumn on this task factory.");
		
		return new TaskIterator(new ClearAllErrorsTask(column, serviceRegistrar));
	}

	@Override
	public boolean isReady(final CyColumn column) {
		final CyTable table = column.getTable();
		final EquationCompiler compiler = serviceRegistrar.getService(EquationCompiler.class);
				
		for (CyRow row : table.getAllRows()) {
			final Object raw = row.getRaw(column.getName());

			if (raw instanceof Equation) {
				final Equation eq = (Equation) raw;
				final boolean success =
						compiler.compile(eq.toString(), TableBrowserUtil.getAttNameToTypeMap(table, null));

				if (!success || table.getLastInternalError() != null)
					return true;
			}
		}
		
		return false;
	}
}
