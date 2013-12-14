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


import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.internal.utils.SessionUtils;
import org.cytoscape.task.read.LoadNetworkFileTaskFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.TunableSetter;

/**
 * Task to load a new network.
 */
public class LoadNetworkFileTaskFactoryImpl extends AbstractTaskFactory implements LoadNetworkFileTaskFactory {

	private CyNetworkReaderManager mgr;
	private CyNetworkManager netmgr;
	private final CyNetworkViewManager networkViewManager;
	private Properties props;
	
	private CyNetworkNaming cyNetworkNaming;
	private final TunableSetter tunableSetter;
	private final VisualMappingManager vmm;
	private final CyNetworkViewFactory nullNetworkViewFactory;
	private final SessionUtils sessionUtils;


	public LoadNetworkFileTaskFactoryImpl(CyNetworkReaderManager mgr, CyNetworkManager netmgr,
			final CyNetworkViewManager networkViewManager, CyProperty<Properties> cyProp,
			CyNetworkNaming cyNetworkNaming, TunableSetter tunableSetter, final VisualMappingManager vmm,
			final CyNetworkViewFactory nullNetworkViewFactory, final SessionUtils sessionUtils) {
		
		this.mgr = mgr;
		this.netmgr = netmgr;
		this.networkViewManager = networkViewManager;
		this.props = cyProp.getProperties();
		this.cyNetworkNaming = cyNetworkNaming;
		this.tunableSetter = tunableSetter;
		this.vmm = vmm;
		this.nullNetworkViewFactory = nullNetworkViewFactory;
		this.sessionUtils = sessionUtils;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		// Load, visualize, and layout.
		return new TaskIterator(3, new LoadNetworkFileTask(mgr, netmgr, networkViewManager, props, cyNetworkNaming, vmm, nullNetworkViewFactory));
	}

	@Override
	public TaskIterator createTaskIterator(File file) {
		final Map<String, Object> m = new HashMap<String, Object>();
		m.put("file", file);

		return tunableSetter.createTaskIterator(this.createTaskIterator(), m); 
	}

	@Override
	public TaskIterator createTaskIterator(File file, TaskObserver observer) {
		final Map<String, Object> m = new HashMap<String, Object>();
		m.put("file", file);

		return tunableSetter.createTaskIterator(this.createTaskIterator(), m, observer); 
	}
	
	@Override
	public boolean isReady() {
		return sessionUtils.isSessionReady();
	}
}
