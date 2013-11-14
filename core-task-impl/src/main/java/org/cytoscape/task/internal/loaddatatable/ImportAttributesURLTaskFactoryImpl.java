package org.cytoscape.task.internal.loaddatatable;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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



import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.io.read.CyTableReaderManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.task.read.*;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;


public class ImportAttributesURLTaskFactoryImpl extends AbstractTaskFactory implements LoadTableURLTaskFactory {
	
	private CyTableReaderManager mgr;
	
	private final TunableSetter tunableSetter; 
	private  final CyNetworkManager netMgr;
	private final CyTableManager tableMgr;
	private final CyRootNetworkManager rootNetMgr;
	
	public ImportAttributesURLTaskFactoryImpl(CyTableReaderManager mgr, TunableSetter tunableSetter,  final CyNetworkManager netMgr,
			final CyTableManager tabelMgr, final CyRootNetworkManager rootNetMgr) {
		this.mgr = mgr;
		this.tunableSetter = tunableSetter;
		this.netMgr = netMgr;
		this.tableMgr = tabelMgr;
		this.rootNetMgr = rootNetMgr;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(2, new ImportAttributesURLTask(mgr, netMgr, tableMgr, rootNetMgr));
	}

	@Override
	public TaskIterator createTaskIterator(URL url) {
		//final Map<String, Object> m = new HashMap<String, Object>();
		//m.put("url", url);
	    URI uri = null;
		try {
			uri = url.toURI();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		CyTableReader reader = mgr.getReader(uri, uri.toString());
		
		return new TaskIterator(new CombineReaderAndMappingTask( reader,tableMgr, netMgr, rootNetMgr));

		//return tunableSetter.createTaskIterator(this.createTaskIterator(), m); 
	}
}
