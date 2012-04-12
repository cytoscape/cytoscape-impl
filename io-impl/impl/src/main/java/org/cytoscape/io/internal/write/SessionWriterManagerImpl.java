package org.cytoscape.io.internal.write;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.CySessionWriterFactory;
import org.cytoscape.io.write.CySessionWriterManager;
import org.cytoscape.session.CySession;

public class SessionWriterManagerImpl extends
		AbstractWriterManager<CySessionWriterFactory> implements
		CySessionWriterManager {

	public SessionWriterManagerImpl() {
		super(DataCategory.SESSION);
	}

	@Override
	public CyWriter getWriter(CySession session, CyFileFilter filter, File file) throws Exception {
		return getWriter(session,filter,new FileOutputStream(file));
	}

	@Override
	public CyWriter getWriter(CySession session, CyFileFilter filter, OutputStream os) throws Exception {
		CySessionWriterFactory factory = getMatchingFactory(filter);
		if (factory == null) {
			throw new NullPointerException("Couldn't find matching factory for filter: " + filter);
		}
		return factory.getWriterTask(os,session);
	}

}
