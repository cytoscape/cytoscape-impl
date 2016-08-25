package org.cytoscape.io.internal.read.session;

import java.io.InputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.util.GroupUtil;
import org.cytoscape.io.internal.util.ReadCache;
import org.cytoscape.io.read.AbstractInputStreamTaskFactory;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.io.read.CyPropertyReaderManager;
import org.cytoscape.io.read.VizmapReaderManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskIterator;

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

public class Cy2SessionReaderFactoryImpl extends AbstractInputStreamTaskFactory {

	private final CyNetworkReaderManager networkReaderMgr;
	private final CyPropertyReaderManager propertyReaderMgr;
	private final VizmapReaderManager vizmapReaderMgr;
	private final ReadCache cache;
	private final GroupUtil groupUtil;
	private final CyServiceRegistrar serviceRegistrar;

	public Cy2SessionReaderFactoryImpl(final CyFileFilter filter,
									   final ReadCache cache,
									   final GroupUtil groupUtil,
									   final CyNetworkReaderManager networkReaderMgr,
									   final CyPropertyReaderManager propertyReaderMgr,
									   final VizmapReaderManager vizmapReaderMgr,
									   final CyServiceRegistrar serviceRegistrar) {
		super(filter);
		this.cache = cache;
		this.groupUtil = groupUtil;
		this.networkReaderMgr = networkReaderMgr;
		this.propertyReaderMgr = propertyReaderMgr;
		this.vizmapReaderMgr = vizmapReaderMgr;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public TaskIterator createTaskIterator(InputStream inputStream, String inputName) {
		return new TaskIterator(new Cy2SessionReaderImpl(inputStream, cache, groupUtil, networkReaderMgr,
				propertyReaderMgr, vizmapReaderMgr, serviceRegistrar));
	}
}
