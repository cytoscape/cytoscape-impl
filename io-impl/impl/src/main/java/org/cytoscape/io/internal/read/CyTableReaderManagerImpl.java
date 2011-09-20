package org.cytoscape.io.internal.read;


import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.io.read.CyTableReaderManager;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.DataCategory;


public class CyTableReaderManagerImpl extends GenericReaderManager<InputStreamTaskFactory, CyTableReader> 
	implements CyTableReaderManager {

	public CyTableReaderManagerImpl() {
		super(DataCategory.TABLE);
	}
}
