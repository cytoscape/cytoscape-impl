package org.cytoscape.tableimport.internal;


import java.io.InputStream;


import org.cytoscape.tableimport.internal.util.CytoscapeServices;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.io.CyFileFilter;


public class ImportNetworkTableReaderFactory extends AbstractNetworkReaderFactory {
	private final static long serialVersionUID = 12023139869460154L;
	
	/**
	 * Creates a new ImportNetworkTableReaderFactory object.
	 */
	public ImportNetworkTableReaderFactory(final CyFileFilter filter){
		super(filter, CytoscapeServices.cyNetworkViewFactory, CytoscapeServices.cyNetworkFactory);
		
	}

public TaskIterator createTaskIterator(InputStream inputStream, String inputName) {
		String fileFormat = inputName.substring(inputName.lastIndexOf('.'));
		return new TaskIterator(new CombineReaderAndMappingTask(inputStream, fileFormat,
		                                                         inputName));
	}
}
