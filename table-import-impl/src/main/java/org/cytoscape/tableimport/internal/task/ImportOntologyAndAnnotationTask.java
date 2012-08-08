package org.cytoscape.tableimport.internal.task;

import java.io.InputStream;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.tableimport.internal.reader.ontology.GeneAssociationReader;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class ImportOntologyAndAnnotationTask extends AbstractTask {
	
	private final InputStreamTaskFactory factory;
	private final CyNetworkManager networkManager;
	private final String ontologyDagName;
	private final CyTableFactory tableFactory;
	private final InputStream gaStream;
	private final String gaTableName;
	private final CyTableManager tableManager;
	private final InputStream is;

	ImportOntologyAndAnnotationTask(final CyNetworkManager networkManager, final InputStreamTaskFactory factory,
			final InputStream is, final String ontologyDagName, final CyTableFactory tableFactory,
			final InputStream gaStream, final String tableName, final CyTableManager tableManager) {
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
		tm.setTitle("Importing ontology and annotations");
		tm.setStatusMessage("Loading Ontology...");
		tm.setProgress(-1d);
		
		final CyNetworkReader loadOBOTask = (CyNetworkReader) factory.createTaskIterator(is, ontologyDagName).next();
		final RegisterOntologyTask registerOntologyTask = new RegisterOntologyTask((CyNetworkReader) loadOBOTask, networkManager, ontologyDagName);
		final GeneAssociationReader gaReader = new GeneAssociationReader(tableFactory, ontologyDagName, gaStream,
				gaTableName, tableManager);
		final MapGeneAssociationTask mapAnnotationTask = new MapGeneAssociationTask(gaReader, tableManager, networkManager);
		
		final TaskIterator taskChain = new TaskIterator(loadOBOTask,registerOntologyTask, gaReader, mapAnnotationTask);
		insertTasksAfterCurrentTask(taskChain);
	}
}
