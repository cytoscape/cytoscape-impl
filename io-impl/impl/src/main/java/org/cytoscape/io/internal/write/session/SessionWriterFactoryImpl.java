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
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySession;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

public class SessionWriterFactoryImpl extends AbstractCyWriterFactory implements CySessionWriterFactory {
	
	private final CyFileFilter bookmarksFilter;
	private final CyFileFilter propertiesFilter;
	private final CyFileFilter tableFilter;
	private final CyFileFilter vizmapFilter;
	private final CyNetworkViewWriterFactory networkViewWriterFactory;
	private final CyPropertyWriterManager propertyWriterMgr;
	private final CyTableWriterManager tableWriterMgr;
	private final VizmapWriterManager vizmapWriterMgr;
	private final GroupUtil groupUtil;
	private final CyServiceRegistrar serviceRegistrar;

	public SessionWriterFactoryImpl(final CyFileFilter thisFilter, 
	                                final CyFileFilter bookmarksFilter, 
	                                final CyFileFilter propertiesFilter,
	                                final CyFileFilter tableFilter,
	                                final CyFileFilter vizmapFilter,
	                                final CyNetworkViewWriterFactory networkViewWriterFactory,
	                                final CyPropertyWriterManager propertyWriterMgr,
	                                final CyTableWriterManager tableWriterMgr,
	                                final VizmapWriterManager vizmapWriterMgr,
	                                final GroupUtil groupUtil,
	                                final CyServiceRegistrar serviceRegistrar) {
		super(thisFilter);
		this.bookmarksFilter = bookmarksFilter;
		this.propertiesFilter = propertiesFilter;
		this.tableFilter = tableFilter;
		this.vizmapFilter = vizmapFilter;
		this.networkViewWriterFactory = networkViewWriterFactory;
		this.propertyWriterMgr = propertyWriterMgr;
		this.tableWriterMgr = tableWriterMgr;
		this.vizmapWriterMgr = vizmapWriterMgr;
		this.groupUtil = groupUtil;
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public CyWriter createWriter(OutputStream outputStream, CySession session) {
		return new SessionWriterImpl(outputStream, session, propertyWriterMgr, tableWriterMgr,
				vizmapWriterMgr, networkViewWriterFactory, bookmarksFilter, propertiesFilter, tableFilter, vizmapFilter,
				groupUtil, serviceRegistrar);
	}
}
