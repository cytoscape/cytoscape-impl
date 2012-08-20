package org.cytoscape.io.internal.write.session;

import java.io.OutputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.util.GroupUtil;
import org.cytoscape.io.internal.write.AbstractCyWriterFactory;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyPropertyWriterManager;
import org.cytoscape.io.write.CySessionWriterFactory;
import org.cytoscape.io.write.CyTableWriterManager;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.VizmapWriterManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.session.CySession;

public class SessionWriterFactoryImpl extends AbstractCyWriterFactory implements CySessionWriterFactory {
	
	private final CyFileFilter bookmarksFilter;
	private final CyFileFilter propertiesFilter;
	private final CyFileFilter tableFilter;
	private final CyFileFilter vizmapFilter;
	private final CyNetworkViewWriterFactory networkViewWriterFactory;
	private final CyRootNetworkManager rootNetworkManager;
	private final CyPropertyWriterManager propertyWriterMgr;
	private final CyTableWriterManager tableWriterMgr;
	private final VizmapWriterManager vizmapWriterMgr;
	private final GroupUtil groupUtil;

	private OutputStream outputStream;
	private CySession session;


	public SessionWriterFactoryImpl(final CyFileFilter thisFilter, 
	                                final CyFileFilter bookmarksFilter, 
	                                final CyFileFilter propertiesFilter,
	                                final CyFileFilter tableFilter,
	                                final CyFileFilter vizmapFilter,
	                                final CyNetworkViewWriterFactory networkViewWriterFactory,
	                                final CyRootNetworkManager rootNetworkManager,
	                                final CyPropertyWriterManager propertyWriterMgr,
	                                final CyTableWriterManager tableWriterMgr,
	                                final VizmapWriterManager vizmapWriterMgr,
	                                final GroupUtil groupUtil) {
		super(thisFilter);
		this.bookmarksFilter = bookmarksFilter;
		this.propertiesFilter = propertiesFilter;
		this.tableFilter = tableFilter;
		this.vizmapFilter = vizmapFilter;
		this.networkViewWriterFactory = networkViewWriterFactory;
		this.rootNetworkManager = rootNetworkManager;
		this.propertyWriterMgr = propertyWriterMgr;
		this.tableWriterMgr = tableWriterMgr;
		this.vizmapWriterMgr = vizmapWriterMgr;
		this.groupUtil = groupUtil;
	}
	
	@Override
	public CyWriter createWriter(OutputStream outputStream, CySession session) {
		return new SessionWriterImpl(outputStream, session, rootNetworkManager, propertyWriterMgr, tableWriterMgr,
				vizmapWriterMgr, networkViewWriterFactory, bookmarksFilter, propertiesFilter, tableFilter, vizmapFilter,
				groupUtil);
	}

}
