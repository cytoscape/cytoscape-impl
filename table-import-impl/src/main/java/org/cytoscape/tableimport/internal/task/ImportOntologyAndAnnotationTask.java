package org.cytoscape.tableimport.internal.task;

import java.io.InputStream;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.session.CyApplicationManager;
import org.cytoscape.tableimport.internal.reader.ontology.GeneAssociationReader;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportOntologyAndAnnotationTask extends AbstractTask {
	
	private static final Logger logger = LoggerFactory.getLogger(ImportOntologyAndAnnotationTask.class);
	
	private final InputStreamTaskFactory factory;
	private final CyNetworkManager manager;
	private final String ontologyDagName;
	final CyTableFactory tableFactory;
	
	private final InputStream gaStream;
	
	private final String gaTableName;
	
	ImportOntologyAndAnnotationTask(final CyNetworkManager manager, final InputStreamTaskFactory factory, InputStream is, String ontologyDagName,
			final CyTableFactory tableFactory, final InputStream gaStream, final String tableName) {
		this.factory = factory;
		this.manager = manager;
		this.ontologyDagName = ontologyDagName;
		this.tableFactory = tableFactory;
		
		this.gaStream = gaStream;
		this.gaTableName = tableName;
		
		this.factory.setInputStream(is, ontologyDagName);
	}
	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		logger.debug("Start");
		Task loadOBOTask = factory.getTaskIterator().next();
		
		final GeneAssociationReader gaReader = new GeneAssociationReader(tableFactory, ontologyDagName, gaStream, gaTableName);
		
		insertTasksAfterCurrentTask(new MapGeneAssociationTask(gaReader, manager));
		insertTasksAfterCurrentTask(gaReader);
		insertTasksAfterCurrentTask(new RegisterOntologyTask((CyNetworkReader) loadOBOTask, manager, ontologyDagName));
		insertTasksAfterCurrentTask(loadOBOTask);
		
	}
}
