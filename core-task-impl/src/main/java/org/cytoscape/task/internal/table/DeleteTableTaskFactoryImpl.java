package org.cytoscape.task.internal.table;

import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTable.Mutability;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractTableTaskFactory;
import org.cytoscape.task.destroy.DeleteTableTaskFactory;
import org.cytoscape.work.TaskIterator;

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

public final class DeleteTableTaskFactoryImpl extends AbstractTableTaskFactory implements DeleteTableTaskFactory {

	private final CyServiceRegistrar serviceRegistrar;
	
	public DeleteTableTaskFactoryImpl(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public TaskIterator createTaskIterator(CyTable table) {
		if (table == null)
			throw new IllegalStateException("You forgot to set the CyTable on this task factory.");
		
		return new TaskIterator(new DeleteTableTask(table, serviceRegistrar));
	}
	
	@Override
	public boolean isReady(CyTable table) {
		return table != null && table.getMutability() == Mutability.MUTABLE;
	}
	
	@Override
	public boolean isApplicable(CyTable table) {
		return isReady(table);
	}
}
