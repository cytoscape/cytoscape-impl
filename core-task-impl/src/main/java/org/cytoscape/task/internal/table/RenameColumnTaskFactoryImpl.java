package org.cytoscape.task.internal.table;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyColumn;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractTableColumnTaskFactory;
import org.cytoscape.task.edit.RenameColumnTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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

public final class RenameColumnTaskFactoryImpl extends AbstractTableColumnTaskFactory 
                                               implements RenameColumnTaskFactory, TaskFactory {

	private final CyServiceRegistrar serviceRegistrar;

	public RenameColumnTaskFactoryImpl(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new RenameColumnCommandTask(serviceRegistrar));
	}

	@Override
	public TaskIterator createTaskIterator(CyColumn column) {
		if (column == null)
			throw new IllegalStateException("you forgot to set the CyColumn on this task factory.");

		return new TaskIterator(new RenameColumnTask(column, serviceRegistrar));
	}

	@Override
	public TaskIterator createTaskIterator(CyColumn column, String newColumnName) {
		final Map<String, Object> m = new HashMap<>();
		m.put("newColumnName", newColumnName);

		return serviceRegistrar.getService(TunableSetter.class).createTaskIterator(this.createTaskIterator(column), m);
	}

	@Override
	public boolean isReady(CyColumn column) {
		return !column.isImmutable();
	}

	@Override
	public boolean isReady() {
		return true;
	}
}
