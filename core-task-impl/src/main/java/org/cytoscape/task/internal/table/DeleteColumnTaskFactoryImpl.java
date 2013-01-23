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
import org.cytoscape.task.AbstractTableColumnTaskFactory;
import org.cytoscape.task.destroy.DeleteColumnTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;


public final class DeleteColumnTaskFactoryImpl extends AbstractTableColumnTaskFactory implements DeleteColumnTaskFactory{
	private final UndoSupport undoSupport;

	public DeleteColumnTaskFactoryImpl(final UndoSupport undoSupport) {
		this.undoSupport = undoSupport;
	}

	@Override
	public TaskIterator createTaskIterator(CyColumn column) {
		if (column == null)
			throw new IllegalStateException("you forgot to set the CyColumn on this task factory.");
		return new TaskIterator(new DeleteColumnTask(undoSupport, column));
	}

	@Override
	public boolean isReady(CyColumn column) {
		return !column.isImmutable();
	}
}