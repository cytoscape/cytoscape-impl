package org.cytoscape.task.internal.loadnetwork;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyRow;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskManager;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2017 The Cytoscape Consortium
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

public class AbstractLoadNetworkTaskTester {

	URI uri;

	CyNetworkReaderManager netReaderManager;
	CyNetworkManager netManager;
	CyNetworkViewManager netViewManager;
	CyProperty<Properties> props;
	CyNetworkNaming namingUtil;
	SynchronousTaskManager<?> syncTaskManager;
	CyApplicationManager applicationManager;
	StreamUtil streamUtil;
	CyServiceRegistrar serviceRegistrar;

	CyNetwork net;
	CyNetworkView view;

	CyNetwork[] networks;
	CyNetworkReader reader;
	VisualMappingManager vmm;
	VisualStyle style;

	public void setUp() throws Exception {
		CyRow attrs = mock(CyRow.class);
		vmm = mock(VisualMappingManager.class);
		style = mock(VisualStyle.class);
		when(vmm.getCurrentVisualStyle()).thenReturn(style);

		net = mock(CyNetwork.class);
		when(net.getNodeCount()).thenReturn(2);
		when(net.getEdgeCount()).thenReturn(1);
		when(net.getRow(net)).thenReturn(attrs);

		view = mock(CyNetworkView.class);
		when(view.getModel()).thenReturn(net);

		networks = new CyNetwork[] { net };

		reader = mock(CyNetworkReader.class);
		when(reader.getNetworks()).thenReturn(networks);
		when(reader.buildCyNetworkView(net)).thenReturn(view);

		netReaderManager = mock(CyNetworkReaderManager.class);
		when(netReaderManager.getReader(eq(uri), anyString())).thenReturn(reader);

		netManager = mock(CyNetworkManager.class);
		netViewManager = mock(CyNetworkViewManager.class);

		Properties p = new Properties();
		p.setProperty("viewThreshold", "1000");

		props = mock(CyProperty.class);
		when(props.getProperties()).thenReturn(p);

		namingUtil = mock(CyNetworkNaming.class);
		syncTaskManager = mock(SynchronousTaskManager.class);
		
		applicationManager = mock(CyApplicationManager.class);
		
		streamUtil = mock(StreamUtil.class);
		
		serviceRegistrar = mock(CyServiceRegistrar.class);
		when(serviceRegistrar.getService(CyApplicationManager.class)).thenReturn(applicationManager);
		when(serviceRegistrar.getService(CyNetworkManager.class)).thenReturn(netManager);
		when(serviceRegistrar.getService(CyNetworkViewManager.class)).thenReturn(netViewManager);
		when(serviceRegistrar.getService(CyNetworkReaderManager.class)).thenReturn(netReaderManager);
		when(serviceRegistrar.getService(VisualMappingManager.class)).thenReturn(vmm);
		when(serviceRegistrar.getService(TaskManager.class)).thenReturn(syncTaskManager);
		when(serviceRegistrar.getService(SynchronousTaskManager.class)).thenReturn(syncTaskManager);
		when(serviceRegistrar.getService(CyNetworkNaming.class)).thenReturn(namingUtil);
		when(serviceRegistrar.getService(StreamUtil.class)).thenReturn(streamUtil);
		when(serviceRegistrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)")).thenReturn(props);
	}
}
