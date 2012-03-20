package org.cytoscape.io.internal.read;

import org.cytoscape.io.DataCategory;
import org.cytoscape.io.read.CySessionReader;
import org.cytoscape.io.read.CySessionReaderManager;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.util.StreamUtil;

public class CySessionReaderManagerImpl extends GenericReaderManager<InputStreamTaskFactory<?>, CySessionReader> implements
		CySessionReaderManager {

	public CySessionReaderManagerImpl(final StreamUtil streamUtil) {
		super(DataCategory.SESSION, streamUtil);
	}
}
