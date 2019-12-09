package org.cytoscape.task.internal.network;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskMonitor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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

public class NewEmptyNetworkTaskTest {

	private final NetworkTestSupport support = new NetworkTestSupport();
	private final NetworkViewTestSupport viewSupport = new NetworkViewTestSupport();

	private CyNetworkFactory netFactory = support.getNetworkFactory();
	private CyNetworkViewFactory netViewFactory = viewSupport.getNetworkViewFactory();
	private CyRootNetworkManager rootNetMgr = support.getRootNetworkFactory();

	@Mock private CyServiceRegistrar serviceRegistrar;
	@Mock private CyNetworkManager netMgr;
	@Mock private CyNetworkViewManager netViewMgr;
	@Mock private CyNetworkNaming namingUtil;
	@Mock private VisualMappingManager vmm;
	@Mock private CyApplicationManager appMgr;
	@Mock private NetworkViewRenderer netViewRenderer;
	@Mock private VisualStyle currentStyle;

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
		when(vmm.getCurrentVisualStyle()).thenReturn(currentStyle);
		when(netViewRenderer.getNetworkViewFactory()).thenReturn(netViewFactory);
		when(appMgr.getDefaultNetworkViewRenderer()).thenReturn(netViewRenderer);
		
		when(serviceRegistrar.getService(CyApplicationManager.class)).thenReturn(appMgr);
		when(serviceRegistrar.getService(CyRootNetworkManager.class)).thenReturn(rootNetMgr);
		when(serviceRegistrar.getService(CyNetworkManager.class)).thenReturn(netMgr);
		when(serviceRegistrar.getService(CyNetworkViewManager.class)).thenReturn(netViewMgr);
		when(serviceRegistrar.getService(CyNetworkFactory.class)).thenReturn(netFactory);
		when(serviceRegistrar.getService(VisualMappingManager.class)).thenReturn(vmm);
		when(serviceRegistrar.getService(CyNetworkNaming.class)).thenReturn(namingUtil);
	}

	@Test
	public void testNewEmptyNetworkTask() throws Exception {
		var task = new NewEmptyNetworkTask(Collections.singleton(netViewRenderer), serviceRegistrar);
		task.run(mock(TaskMonitor.class));

		verify(netMgr, times(1)).addNetwork(any(CyNetwork.class), eq(false));
		verify(netViewMgr, times(1)).addNetworkView(any(CyNetworkView.class));
		verify(vmm, times(1)).setVisualStyle(eq(currentStyle), any(CyNetworkView.class));
	}
}
