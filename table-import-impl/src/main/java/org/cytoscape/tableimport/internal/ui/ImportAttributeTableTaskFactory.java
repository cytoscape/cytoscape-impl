package org.cytoscape.tableimport.internal.ui;


import org.cytoscape.model.CyTableManager;
import org.cytoscape.tableimport.internal.reader.TextTableReader;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;


public class ImportAttributeTableTaskFactory implements TaskFactory {
	private final TextTableReader reader;
	private final CyTableManager tableManager;

	ImportAttributeTableTaskFactory(final TextTableReader reader,
					final CyTableManager tableManager)
	{
		this.reader       = reader;
		this.tableManager = tableManager;
	}

	@Override
	public TaskIterator getTaskIterator() {
		return new TaskIterator(new ImportAttributeTableTask(reader, tableManager));
	}

}
