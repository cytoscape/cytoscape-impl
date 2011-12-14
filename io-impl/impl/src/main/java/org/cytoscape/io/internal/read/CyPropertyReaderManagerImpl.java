package org.cytoscape.io.internal.read;

import org.cytoscape.io.DataCategory;
import org.cytoscape.io.read.CyPropertyReader;
import org.cytoscape.io.read.CyPropertyReaderManager;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.util.StreamUtil;

public class CyPropertyReaderManagerImpl extends GenericReaderManager<InputStreamTaskFactory, CyPropertyReader>
		implements CyPropertyReaderManager {

	public CyPropertyReaderManagerImpl(final StreamUtil streamUtil) {
		super(DataCategory.PROPERTIES, streamUtil);
	}
}
