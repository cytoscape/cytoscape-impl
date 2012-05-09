package org.cytoscape.tableimport.internal;


import java.io.InputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.tableimport.internal.util.CytoscapeServices;
import org.cytoscape.work.TaskIterator;


public class ImportAttributeTableReaderFactory extends AbstractTableReaderFactory {
	private final static long serialVersionUID = 12023139869460898L;

	/**
	 * Creates a new ImportAttributeTableReaderFactory object.
	 */
	public ImportAttributeTableReaderFactory(CyFileFilter filter)
	{
		super(filter, CytoscapeServices.cyTableFactory);
		
	}

	public TaskIterator createTaskIterator(InputStream inputStream, String inputName) {
		String fileFormat = inputName.substring(inputName.lastIndexOf('.'));
		return new TaskIterator(
			new ImportAttributeTableReaderTask(inputStream, fileFormat, inputName));
	}
}
