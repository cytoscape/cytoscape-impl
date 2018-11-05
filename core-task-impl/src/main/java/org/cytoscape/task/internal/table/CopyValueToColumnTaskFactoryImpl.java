package org.cytoscape.task.internal.table;

import org.cytoscape.model.CyColumn;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractTableCellTaskFactory;
import org.cytoscape.work.TaskIterator;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2018 The Cytoscape Consortium
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

public final class CopyValueToColumnTaskFactoryImpl extends AbstractTableCellTaskFactory {
	
	private final boolean selectedOnly;
	private final String taskFactoryName;
	private final CyServiceRegistrar serviceRegistrar;

	public CopyValueToColumnTaskFactoryImpl(boolean selectedOnly, String taskFactoryName,
			CyServiceRegistrar serviceRegistrar) {
		this.selectedOnly = selectedOnly;
		this.taskFactoryName = taskFactoryName;
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public TaskIterator createTaskIterator(CyColumn column, Object primaryKeyValue) {
		if (column == null)
			throw new IllegalStateException("'column' was not set.");
		if (primaryKeyValue == null)
			throw new IllegalStateException("'primaryKeyValue' was not set.");
		
		return new TaskIterator(
				new CopyValueToColumnTask(column, primaryKeyValue, selectedOnly, taskFactoryName, serviceRegistrar));
	}
	
	public String getTaskFactoryName(){
		return taskFactoryName;
	}
}
