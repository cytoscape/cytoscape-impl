package org.cytoscape.task.internal.loadnetwork;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Collections;
import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskManager;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

	@Mock CyNetworkReaderManager netReaderManager;
	@Mock CyRootNetworkManager rootNetManager;
	@Mock CyNetworkManager netManager;
	@Mock CyNetworkViewManager netViewManager;
	@Mock CyProperty<Properties> props;
	@Mock CyNetworkNaming namingUtil;
	@Mock SynchronousTaskManager<?> syncTaskManager;
	@Mock CyApplicationManager applicationManager;
	@Mock StreamUtil streamUtil;
	@Mock VisualMappingManager vmm;
	@Mock CyNetworkReader reader;
	@Mock CyServiceRegistrar serviceRegistrar;

	@Mock CyRootNetwork rootNet;
	@Mock CySubNetwork net;
	@Mock CyNetworkView view;
	@Mock VisualStyle style;

	CyNetwork[] networks;

	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		CyRow attrs = mock(CyRow.class);
		when(vmm.getDefaultVisualStyle()).thenReturn(style);
		when(vmm.getVisualStyle(any(CyNetworkView.class))).thenReturn(style);
		
		when(rootNetManager.getRootNetwork(any(CyNetwork.class))).thenReturn(rootNet);

		when(rootNet.getBaseNetwork()).thenReturn(net);
		when(rootNet.getSubNetworkList()).thenReturn(Collections.singletonList(net));
		when(rootNet.getRow(rootNet)).thenReturn(attrs);
		
		when(net.getRootNetwork()).thenReturn(rootNet);
		when(net.getNodeCount()).thenReturn(2);
		when(net.getEdgeCount()).thenReturn(1);
		when(net.getRow(net)).thenReturn(attrs);

		when(view.getModel()).thenReturn(net);

		networks = new CyNetwork[] { net };

		when(reader.getNetworks()).thenReturn(networks);
		when(reader.buildCyNetworkView(net)).thenReturn(view);

		when(netReaderManager.getReader(eq(uri), anyString())).thenReturn(reader);

		Properties p = new Properties();
		p.setProperty("viewThreshold", "1000");

		when(props.getProperties()).thenReturn(p);
		
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
		when(serviceRegistrar.getService(CyRootNetworkManager.class)).thenReturn(rootNetManager);
	}
}
