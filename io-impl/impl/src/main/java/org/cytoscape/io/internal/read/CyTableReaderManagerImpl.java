package org.cytoscape.io.internal.read;

import org.cytoscape.io.DataCategory;
import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.io.read.CyTableReaderManager;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.util.StreamUtil;

public class CyTableReaderManagerImpl extends GenericReaderManager<InputStreamTaskFactory<?>, CyTableReader> implements
		CyTableReaderManager {

	public CyTableReaderManagerImpl(final StreamUtil streamUtil) {
		super(DataCategory.TABLE, streamUtil);
	}
}
