package org.cytoscape.io.internal.read;

import org.cytoscape.io.read.CySessionReader;
import org.cytoscape.io.read.CySessionReaderManager;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.DataCategory;

public class CySessionReaderManagerImpl extends
		GenericReaderManager<InputStreamTaskFactory, CySessionReader> implements
		CySessionReaderManager {

	public CySessionReaderManagerImpl() {
			super(DataCategory.SESSION);
	}
}
