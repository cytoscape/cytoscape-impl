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
import org.cytoscape.io.internal.write.AbstractCyWriterFactory;

public class SessionWriterFactoryImpl extends AbstractCyWriterFactory implements CySessionWriterFactory {
	
	private final CyFileFilter xgmmlFilter;
	private final CyFileFilter bookmarksFilter;
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
	                                final CyFileFilter propertiesFilter,
	                                final CyFileFilter tableFilter,
	                                final CyFileFilter vizmapFilter,
	                                final CyNetworkViewWriterManager networkViewWriterMgr,
	                                final CyRootNetworkManager rootNetworkManager,
	                                final CyPropertyWriterManager propertyWriterMgr,
	                                final CyTableWriterManager tableWriterMgr,
	                                final VizmapWriterManager vizmapWriterMgr) {
		super(thisFilter);
		this.xgmmlFilter = xgmmlFilter;
		this.bookmarksFilter = bookmarksFilter;
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
	public CyWriter createWriter(OutputStream outputStream, CySession session) {
		return new SessionWriterImpl(outputStream, session, networkViewWriterMgr, rootNetworkManager,
		                             propertyWriterMgr, tableWriterMgr, vizmapWriterMgr, xgmmlFilter,
		                             bookmarksFilter, propertiesFilter, tableFilter, vizmapFilter);
	}

}
