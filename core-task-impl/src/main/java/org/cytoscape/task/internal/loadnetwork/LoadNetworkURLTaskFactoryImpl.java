package org.cytoscape.task.internal.loadnetwork;

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


import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.read.LoadNetworkURLTaskFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.TunableSetter;


/**
 * Task to load a new network.
 */
public class LoadNetworkURLTaskFactoryImpl extends AbstractTaskFactory implements LoadNetworkURLTaskFactory {

	private CyNetworkReaderManager mgr;
	private CyNetworkManager netmgr;
	private final CyNetworkViewManager networkViewManager;
	private Properties props;
	private StreamUtil streamUtil;

	private CyNetworkNaming cyNetworkNaming;
	
	private final SynchronousTaskManager<?> syncTaskManager;
	
	private final TunableSetter tunableSetter;
	
	private final VisualMappingManager vmm;
	private final CyNetworkViewFactory nullNetworkViewFactory;

	public LoadNetworkURLTaskFactoryImpl(CyNetworkReaderManager mgr,
					     CyNetworkManager netmgr,
					     final CyNetworkViewManager networkViewManager,
					     CyProperty<Properties> cyProps, CyNetworkNaming cyNetworkNaming,
					     StreamUtil streamUtil, final SynchronousTaskManager<?> syncTaskManager,
						 TunableSetter tunableSetter, final VisualMappingManager vmm,
						 final CyNetworkViewFactory nullNetworkViewFactory)
	{
		this.mgr = mgr;
		this.netmgr = netmgr;
		this.networkViewManager = networkViewManager;
		this.props = cyProps.getProperties();
		this.cyNetworkNaming = cyNetworkNaming;
		this.streamUtil = streamUtil;
		this.tunableSetter = tunableSetter;
		this.syncTaskManager = syncTaskManager;
		this.vmm = vmm;
		this.nullNetworkViewFactory = nullNetworkViewFactory;
	}

	@Override
	public TaskIterator createTaskIterator() {
		// Usually we need to create view, so expected number is 2.
		return new TaskIterator(2, new LoadNetworkURLTask(mgr, netmgr, networkViewManager, props, cyNetworkNaming, streamUtil, vmm, nullNetworkViewFactory));
	}

	public TaskIterator createTaskIterator(final URL url) {
		return loadCyNetworks(url);
	}

	@Override
	public TaskIterator createTaskIterator(final URL url, TaskObserver observer) {
		return loadCyNetworks(url);
	}
	
	@Override
	public TaskIterator loadCyNetworks(final URL url) {
		
		String name = "";
		CyNetworkReader reader = null;
		
		try {
			name = url.toURI().toString();
			reader = mgr.getReader(url.toURI(), url.toURI().toString());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new TaskIterator(2,new LoadNetworkTask(mgr, netmgr, reader, name,networkViewManager, props, cyNetworkNaming, vmm, nullNetworkViewFactory));
	}
}
