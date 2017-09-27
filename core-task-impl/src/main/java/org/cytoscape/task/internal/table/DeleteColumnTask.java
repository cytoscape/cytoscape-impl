package org.cytoscape.task.internal.table;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2013 The Cytoscape Consortium
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
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractTableColumnTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.TunableValidator.ValidationState;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.work.undo.UndoSupport;

import java.util.Arrays;
import java.util.List;

import javax.swing.SwingUtilities;


public final class DeleteColumnTask extends AbstractTableColumnTask implements TunableValidator {
	private final UndoSupport undoSupport;
	private final CyServiceRegistrar serviceRegistrar;

	DeleteColumnTask(final UndoSupport undoSupport, final CyColumn column, CyServiceRegistrar reg) {
		super(column);
		serviceRegistrar = reg;
		this.undoSupport = undoSupport;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(0.0);
		undoSupport.postEdit( new DeleteColumnEdit(column));
		taskMonitor.setProgress(0.3);
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {    	column.getTable().deleteColumn(column.getName());  }    
         });
		taskMonitor.setProgress(1.0);
	}

	@Override
	public ValidationState getValidationState(final Appendable errMsg) {
		if (column.isImmutable()) {
			try {
				errMsg.append("Cannot delete an immutable column.");
			} catch (Exception e) {	}
			return ValidationState.INVALID;
		}
		return ValidationState.OK;
	}
	public List<Class<?>> getResultClasses() {	return Arrays.asList(CyColumn.class, String.class, JSONResult.class);	}
	public Object getResults(Class requestedType) {
		if (requestedType.equals(String.class)) 		return column.getName();
		if (requestedType.equals(JSONResult.class)) {
			JSONResult res = () -> {		return column.getName();	};	
			return res;
			}
		return null;
	}
}