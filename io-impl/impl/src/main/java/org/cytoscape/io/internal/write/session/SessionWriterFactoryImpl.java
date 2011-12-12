package org.cytoscape.io.internal.write.session;

import java.io.OutputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyNetworkViewWriterManager;
import org.cytoscape.io.write.CyPropertyWriterManager;
import org.cytoscape.io.write.CySessionWriterFactory;
import org.cytoscape.io.write.CyTableWriterManager;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.VizmapWriterManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.session.CySession;

public class SessionWriterFactoryImpl implements CySessionWriterFactory {
	
	private final CyFileFilter thisFilter;
	private final CyFileFilter xgmmlFilter;
	private final CyFileFilter bookmarksFilter;
	private final CyFileFilter cysessionFilter;
	private final CyFileFilter propertiesFilter;
	private final CyFileFilter tableFilter;
	private final CyFileFilter vizmapFilter;
	private final CyNetworkViewWriterManager networkViewWriterMgr;
	private final CyRootNetworkManager rootNetworkManager;
	private final CyPropertyWriterManager propertyWriterMgr;
	private final CyTableWriterManager tableWriterMgr;
	private final VizmapWriterManager vizmapWriterMgr;

	private OutputStream outputStream;
	private CySession session;



	public SessionWriterFactoryImpl(final CyFileFilter thisFilter, 
	                                final CyFileFilter xgmmlFilter, 
	                                final CyFileFilter bookmarksFilter, 
	                                final CyFileFilter cysessionFilter, 
	                                final CyFileFilter propertiesFilter,
	                                final CyFileFilter tableFilter,
	                                final CyFileFilter vizmapFilter,
	                                final CyNetworkViewWriterManager networkViewWriterMgr,
	                                final CyRootNetworkManager rootNetworkManager,
	                                final CyPropertyWriterManager propertyWriterMgr,
	                                final CyTableWriterManager tableWriterMgr,
	                                final VizmapWriterManager vizmapWriterMgr) {
		this.thisFilter = thisFilter;
		this.xgmmlFilter = xgmmlFilter;
		this.bookmarksFilter = bookmarksFilter;
		this.cysessionFilter = cysessionFilter;
		this.propertiesFilter = propertiesFilter;
		this.tableFilter = tableFilter;
		this.vizmapFilter = vizmapFilter;
		this.networkViewWriterMgr = networkViewWriterMgr;
		this.rootNetworkManager = rootNetworkManager;
		this.propertyWriterMgr = propertyWriterMgr;
		this.tableWriterMgr = tableWriterMgr;
		this.vizmapWriterMgr = vizmapWriterMgr;
	}
	
	@Override
	public CyWriter getWriterTask() {
		return new SessionWriterImpl(outputStream, session, networkViewWriterMgr, rootNetworkManager,
		                             propertyWriterMgr, tableWriterMgr, vizmapWriterMgr, xgmmlFilter,
		                             bookmarksFilter, cysessionFilter, propertiesFilter,
		                             tableFilter, vizmapFilter);
	}

	@Override
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	@Override
	public CyFileFilter getFileFilter() {
		return thisFilter;
	}

	@Override
	public void setSession(CySession session) {
		this.session = session;
	}
}
