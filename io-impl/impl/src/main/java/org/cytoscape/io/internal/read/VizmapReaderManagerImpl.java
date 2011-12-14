package org.cytoscape.io.internal.read;

import org.cytoscape.io.DataCategory;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.read.VizmapReader;
import org.cytoscape.io.read.VizmapReaderManager;
import org.cytoscape.io.util.StreamUtil;

public class VizmapReaderManagerImpl extends GenericReaderManager<InputStreamTaskFactory, VizmapReader> implements
		VizmapReaderManager {

	public VizmapReaderManagerImpl(final StreamUtil streamUtil) {
		super(DataCategory.VIZMAP, streamUtil);
	}
}