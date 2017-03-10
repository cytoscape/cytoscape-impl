package org.cytoscape.task.internal.export.table;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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


import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.write.CyTableWriterManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.task.write.ExportSelectedTableTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class ExportSelectedTableTaskFactoryImpl extends AbstractTaskFactory implements ExportSelectedTableTaskFactory{

	private final CyTableWriterManager writerManager;
	private final CyTableManager cyTableManagerServiceRef;
	private final CyNetworkManager cyNetworkManagerServiceRef;
	private final CyApplicationManager cyApplicationManagerServiceRef;

	
	public ExportSelectedTableTaskFactoryImpl(CyTableWriterManager writerManager, CyTableManager cyTableManagerServiceRef, 
			CyNetworkManager cyNetworkManagerServiceRef, CyApplicationManager cyApplicationManagerServiceRef) {
		this.writerManager = writerManager;
		this.cyTableManagerServiceRef = cyTableManagerServiceRef;
		this.cyNetworkManagerServiceRef = cyNetworkManagerServiceRef;
		this.cyApplicationManagerServiceRef = cyApplicationManagerServiceRef;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new SelectExportTableTask(writerManager, cyTableManagerServiceRef, 
				cyNetworkManagerServiceRef, cyApplicationManagerServiceRef));
	}
}
