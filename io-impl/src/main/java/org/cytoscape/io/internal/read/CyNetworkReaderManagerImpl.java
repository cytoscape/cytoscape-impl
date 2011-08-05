package org.cytoscape.io.internal.read;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.DataCategory;

public class CyNetworkReaderManagerImpl extends GenericReaderManager<InputStreamTaskFactory, CyNetworkReader> implements
		CyNetworkReaderManager {

	public CyNetworkReaderManagerImpl() {
		super(DataCategory.NETWORK);
	}
}
