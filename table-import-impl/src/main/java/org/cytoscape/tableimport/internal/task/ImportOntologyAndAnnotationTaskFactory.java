package org.cytoscape.tableimport.internal.task;


import java.io.InputStream;

import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;


public class ImportOntologyAndAnnotationTaskFactory implements TaskFactory {
	private final InputStreamTaskFactory factory;
	private final InputStream is;
	private final String ontologyDAGName;
	private final CyNetworkManager manager;
	private final CyTableFactory tableFactory;
	private final InputStream gaStream;
	private final String gaGlobalTableName;
	private final CyTableManager tableManager;

	public ImportOntologyAndAnnotationTaskFactory(final CyNetworkManager manager,
						      final InputStreamTaskFactory factory,
						      final InputStream is, String ontologyDAGName,
						      final CyTableFactory tableFactory,
						      final InputStream gaStream,
						      final String gaGlobalTableName,
						      final CyTableManager tableManager)
	{
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
		return new TaskIterator(
			new ImportOntologyAndAnnotationTask(manager, factory, is, ontologyDAGName,
			                                    tableFactory, gaStream,
			                                    gaGlobalTableName, tableManager));
	}
}
