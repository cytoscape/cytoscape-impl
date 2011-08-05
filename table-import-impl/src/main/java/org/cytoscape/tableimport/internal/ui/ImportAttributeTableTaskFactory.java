package org.cytoscape.tableimport.internal.ui;

import org.cytoscape.tableimport.internal.reader.TextTableReader;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class ImportAttributeTableTaskFactory implements TaskFactory {

	private final TextTableReader reader;
	
	ImportAttributeTableTaskFactory(final TextTableReader reader) {
		this.reader = reader;
	}
	
	@Override
	public TaskIterator getTaskIterator() {
		return new TaskIterator(new ImportAttributeTableTask(reader));
	}

}
