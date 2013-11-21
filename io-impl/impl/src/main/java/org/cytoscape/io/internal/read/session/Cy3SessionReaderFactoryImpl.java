package org.cytoscape.io.internal.read.session;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import java.io.InputStream;

import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.read.datatable.CSVCyReaderFactory;
import org.cytoscape.io.internal.util.GroupUtil;
import org.cytoscape.io.internal.util.ReadCache;
import org.cytoscape.io.internal.util.SUIDUpdater;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.io.read.CyPropertyReaderManager;
import org.cytoscape.io.read.AbstractInputStreamTaskFactory;
import org.cytoscape.io.read.VizmapReaderManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.work.TaskIterator;

public class Cy3SessionReaderFactoryImpl extends AbstractInputStreamTaskFactory {

	private final ReadCache cache;
	private final GroupUtil groupUtil;
	private final SUIDUpdater suidUpdater;
	private final CyNetworkReaderManager networkReaderMgr;
	private final CyPropertyReaderManager propertyReaderMgr;
	private final VizmapReaderManager vizmapReaderMgr;
	private final CSVCyReaderFactory csvCyReaderFactory;
	private final CyNetworkTableManager networkTableMgr;
	private final CyRootNetworkManager rootNetworkMgr;
	private final EquationCompiler compiler;

	public Cy3SessionReaderFactoryImpl(final CyFileFilter filter,
									   final ReadCache cache,
									   final GroupUtil groupUtil,
									   final SUIDUpdater suidUpdater,
									   final CyNetworkReaderManager networkReaderMgr,
									   final CyPropertyReaderManager propertyReaderMgr,
									   final VizmapReaderManager vizmapReaderMgr,
									   final CSVCyReaderFactory csvCyReaderFactory,
									   final CyNetworkTableManager networkTableMgr,
									   final CyRootNetworkManager rootNetworkMgr,
									   final EquationCompiler compiler) {
		super(filter);
		this.cache = cache;
		this.groupUtil = groupUtil;
		this.suidUpdater = suidUpdater;
		this.networkReaderMgr = networkReaderMgr;
		this.propertyReaderMgr = propertyReaderMgr;
		this.vizmapReaderMgr = vizmapReaderMgr;
		this.csvCyReaderFactory = csvCyReaderFactory;
		this.networkTableMgr = networkTableMgr;
		this.rootNetworkMgr = rootNetworkMgr;
		this.compiler = compiler;
	}

	@Override
	public TaskIterator createTaskIterator(InputStream inputStream, String inputName) {
		return new TaskIterator(new Cy3SessionReaderImpl(inputStream, cache, groupUtil, suidUpdater, networkReaderMgr,
				propertyReaderMgr, vizmapReaderMgr, csvCyReaderFactory, networkTableMgr, rootNetworkMgr, compiler));
	}
}
