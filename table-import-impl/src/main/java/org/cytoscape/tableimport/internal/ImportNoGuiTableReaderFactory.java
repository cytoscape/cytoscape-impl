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



import java.io.InputStream;

import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.task.edit.*;



public class ImportNoGuiTableReaderFactory extends AbstractTaskFactory {
	private final static long serialVersionUID = 12023139869460898L;
	
	protected final StreamUtil streamUtil;
	private boolean fromURL;
	private ImportDataTableTaskFactory importFactory;

	/**
	 * Creates a new ImportAttributeTableReaderFactory object.
	 */
	public ImportNoGuiTableReaderFactory(final StreamUtil streamUtil,ImportDataTableTaskFactory importFactory, boolean fromURL)
	{
		this.streamUtil = streamUtil;
		this.fromURL = fromURL;
		this.importFactory = importFactory;
	}

	public TaskIterator createTaskIterator() {
		
		LoadTableReaderTask readerTask = new LoadTableReaderTask();
		TaskIterator importTaskIterator = importFactory.createTaskIterator(readerTask);
		
		if(fromURL)
		{
			return new TaskIterator(new SelectURLTableTask(readerTask,streamUtil ),readerTask, importTaskIterator.next());
		}
		else
		{
			return new TaskIterator(new SelectFileTableTask(readerTask,streamUtil ),readerTask,importTaskIterator.next());
		}
		
	}
	
	
}
