package org.cytoscape.tableimport.internal.task;

import java.io.InputStream;

import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class ImportOntologyAndAnnotationTaskFactory implements TaskFactory {

	private final InputStreamTaskFactory factory;
	private InputStream is;
	private String ontologyDAGName;
	private final CyNetworkManager manager;
	
	final CyTableFactory tableFactory;
	final InputStream gaStream;
	private String gaGlobalTableName;

	public ImportOntologyAndAnnotationTaskFactory(final CyNetworkManager manager, final InputStreamTaskFactory factory,
			InputStream is, String ontologyDAGName, final CyTableFactory tableFactory,
			final InputStream gaStream, final String gaGlobalTableName) {
		this.factory = factory;
		this.is = is;
		this.ontologyDAGName = ontologyDAGName;
		this.manager = manager;
		
		this.tableFactory = tableFactory;
		this.gaStream = gaStream;
		this.gaGlobalTableName = gaGlobalTableName;
	}

	@Override
	public TaskIterator getTaskIterator() {
		return new TaskIterator(new ImportOntologyAndAnnotationTask(manager, factory, is, ontologyDAGName, tableFactory, gaStream, gaGlobalTableName));
	}
}
