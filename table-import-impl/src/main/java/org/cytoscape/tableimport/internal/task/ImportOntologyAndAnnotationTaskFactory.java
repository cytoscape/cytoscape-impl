package org.cytoscape.tableimport.internal.task;

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

import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class ImportOntologyAndAnnotationTaskFactory extends AbstractTaskFactory {
	private final InputStreamTaskFactory factory;
	private final InputStream is;
	private final String ontologyDAGName;
	private final CyNetworkManager manager;
	private final CyTableFactory tableFactory;
	private final InputStream gaStream;
	private final String gaGlobalTableName;
	private final CyTableManager tableManager;

	public ImportOntologyAndAnnotationTaskFactory(final CyNetworkManager manager, final InputStreamTaskFactory factory,
			final InputStream is, String ontologyDAGName, final CyTableFactory tableFactory,
			final InputStream gaStream, final String gaGlobalTableName, final CyTableManager tableManager) {
		this.factory = factory;
		this.is = is;
		this.ontologyDAGName = ontologyDAGName;
		this.manager = manager;
		this.tableFactory = tableFactory;
		this.gaStream = gaStream;
		this.gaGlobalTableName = gaGlobalTableName;
		this.tableManager = tableManager;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ImportOntologyAndAnnotationTask(manager, factory, is, ontologyDAGName,
				tableFactory, gaStream, gaGlobalTableName, tableManager));
	}
}
