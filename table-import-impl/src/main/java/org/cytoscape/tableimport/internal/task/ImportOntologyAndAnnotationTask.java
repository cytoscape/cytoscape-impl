package org.cytoscape.tableimport.internal.task;


import java.io.InputStream;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.tableimport.internal.reader.ontology.GeneAssociationReader;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ImportOntologyAndAnnotationTask extends AbstractTask {
	private static final Logger logger = LoggerFactory.getLogger(ImportOntologyAndAnnotationTask.class);
	
	private final InputStreamTaskFactory factory;
	private final CyNetworkManager networkManager;
	private final String ontologyDagName;
	private final CyTableFactory tableFactory;
	private final InputStream gaStream;
	private final String gaTableName;
	private final CyTableManager tableManager;
	private final InputStream is;
	
	ImportOntologyAndAnnotationTask(final CyNetworkManager networkManager,
	                                final InputStreamTaskFactory factory, final InputStream is,
	                                final String ontologyDagName,
	                                final CyTableFactory tableFactory,
	                                final InputStream gaStream, final String tableName,
	                                final CyTableManager tableManager)
	{
		this.factory = factory;
		this.networkManager = networkManager;
		this.ontologyDagName = ontologyDagName;
		this.tableFactory = tableFactory;
		
		this.gaStream = gaStream;
		this.gaTableName = tableName;
		this.tableManager = tableManager;
		
		this.is = is;
	}
	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		logger.debug("Start");
		Task loadOBOTask = factory.createTaskIterator(is, ontologyDagName).next();
		
		final GeneAssociationReader gaReader =
			new GeneAssociationReader(tableFactory, ontologyDagName, gaStream, gaTableName, tableManager);
		
		insertTasksAfterCurrentTask(new MapGeneAssociationTask(gaReader, tableManager, networkManager));
		insertTasksAfterCurrentTask(gaReader);
		insertTasksAfterCurrentTask(new RegisterOntologyTask((CyNetworkReader) loadOBOTask, networkManager, ontologyDagName));
		insertTasksAfterCurrentTask(loadOBOTask);
		
	}
}
