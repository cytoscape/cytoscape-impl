package org.cytoscape.io.internal.read;


import org.cytoscape.io.read.CyPropertyReader;
import org.cytoscape.io.read.CyPropertyReaderManager;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.DataCategory;


public class CyPropertyReaderManagerImpl extends GenericReaderManager<InputStreamTaskFactory, CyPropertyReader> 
	implements CyPropertyReaderManager {

	public CyPropertyReaderManagerImpl() {
		super(DataCategory.PROPERTIES);
	}
}
