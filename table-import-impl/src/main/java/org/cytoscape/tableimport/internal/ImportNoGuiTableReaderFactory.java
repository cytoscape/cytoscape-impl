package org.cytoscape.tableimport.internal;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
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

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.edit.ImportDataTableTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;


public class ImportNoGuiTableReaderFactory extends AbstractTaskFactory {
	
	private final boolean fromURL;
	private final CyServiceRegistrar serviceRegistrar;

	/**
	 * Creates a new ImportAttributeTableReaderFactory object.
	 */
	public ImportNoGuiTableReaderFactory(boolean fromURL, final CyServiceRegistrar serviceRegistrar) {
		this.fromURL = fromURL;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public TaskIterator createTaskIterator() {
		final LoadTableReaderTask readerTask = new LoadTableReaderTask(serviceRegistrar);
		final ImportDataTableTaskFactory importFactory = serviceRegistrar.getService(ImportDataTableTaskFactory.class);
		final TaskIterator importTaskIterator = importFactory.createTaskIterator(readerTask);
		
		if (fromURL) {
			return new TaskIterator(new SelectURLTableTask(readerTask, serviceRegistrar), readerTask, importTaskIterator.next());
		} else {
			return new TaskIterator(new SelectFileTableTask(readerTask, serviceRegistrar), readerTask,importTaskIterator.next());
		}
	}
}
