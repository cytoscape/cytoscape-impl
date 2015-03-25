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

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.tableimport.internal.reader.ontology.GeneAssociationReader;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class ImportOntologyAndAnnotationTask extends AbstractTask {
	
	private final InputStreamTaskFactory factory;
	private final String ontologyDagName;
	private final InputStream gaStream;
	private final String gaTableName;
	private final CyServiceRegistrar serviceRegistrar;
	private final InputStream is;

	ImportOntologyAndAnnotationTask(
			final InputStreamTaskFactory factory,
			final InputStream is,
			final String ontologyDagName,
			final InputStream gaStream,
			final String tableName,
			final CyServiceRegistrar serviceRegistrar) {
		this.factory = factory;
		this.ontologyDagName = ontologyDagName;
		this.gaStream = gaStream;
		this.gaTableName = tableName;
		this.serviceRegistrar = serviceRegistrar;
		this.is = is;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Importing ontology and annotations");
		tm.setStatusMessage("Loading Ontology...");
		tm.setProgress(-1d);
		
		final CyNetworkReader loadOBOTask = (CyNetworkReader) factory.createTaskIterator(is, ontologyDagName).next();
		final RegisterOntologyTask registerOntologyTask = new RegisterOntologyTask((CyNetworkReader) loadOBOTask,
				serviceRegistrar, ontologyDagName);
		final GeneAssociationReader gaReader = new GeneAssociationReader(ontologyDagName, gaStream, gaTableName,
				serviceRegistrar);
		final MapGeneAssociationTask mapAnnotationTask = new MapGeneAssociationTask(gaReader, serviceRegistrar);
		
		final TaskIterator taskChain = new TaskIterator(loadOBOTask,registerOntologyTask, gaReader, mapAnnotationTask);
		insertTasksAfterCurrentTask(taskChain);
	}
}
