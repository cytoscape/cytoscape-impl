package org.cytoscape.task.internal.view;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;
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
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class CreateNetworkViewTaskTest {
	
	private final NetworkTestSupport support = new NetworkTestSupport();
	private final NetworkViewTestSupport viewSupport = new NetworkViewTestSupport();
	private CyNetworkViewFactory viewFactory = viewSupport.getNetworkViewFactory();
	private final CyRootNetworkManager rootNetManager = support.getRootNetworkFactory();

	@Mock private CyNetworkViewManager netViewManager;
	@Mock private CyNetworkManager netManager;
	@Mock private RenderingEngineManager renderingEngineManager;
	@Mock private UndoSupport undoSupport;
	@Mock private TaskMonitor tm;
	@Mock private CyEventHelper eventHelper;
	@Mock private VisualMappingManager vmManager;
	@Mock private CyApplicationManager appManager;
	@Mock private VisualStyle defStyle;
	@Mock private CyServiceRegistrar serviceRegistrar;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		when(vmManager.getDefaultVisualStyle()).thenReturn(defStyle);
		when(vmManager.getVisualStyle(any(CyNetworkView.class))).thenReturn(defStyle);
		
		when(renderingEngineManager.getRenderingEngines(any(View.class))).thenReturn(Collections.EMPTY_LIST);
		
		when(serviceRegistrar.getService(UndoSupport.class)).thenReturn(undoSupport);
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
		when(serviceRegistrar.getService(RenderingEngineManager.class)).thenReturn(renderingEngineManager);
		when(serviceRegistrar.getService(VisualMappingManager.class)).thenReturn(vmManager);
		when(serviceRegistrar.getService(CyApplicationManager.class)).thenReturn(appManager);
		when(serviceRegistrar.getService(CyNetworkViewManager.class)).thenReturn(netViewManager);
		when(serviceRegistrar.getService(CyRootNetworkManager.class)).thenReturn(rootNetManager);
	}
	
	@Test
	public void testCreateNetworkViewTask() throws Exception {
		Set<CyNetwork> networks = new HashSet<>();
		networks.add(support.getNetwork());
		var task = new CreateNetworkViewTask(networks, viewFactory, netManager, null, appManager, serviceRegistrar);

		task.setTaskIterator(new TaskIterator(task));
		task.run(tm);
		verify(netViewManager, times(1)).addNetworkView(any(CyNetworkView.class), eq(false));
	}

	@Test
	public void testShouldCreateMultipleViewsPerNetwork() throws Exception {
		Set<CyNetwork> networks = new HashSet<>();
		CyNetworkView view = viewSupport.getNetworkView();
		networks.add(support.getNetwork());
		networks.add(view.getModel());
		when(netViewManager.getNetworkViews(view.getModel())).thenReturn(Arrays.asList(new CyNetworkView[]{ view }));
		
		var task = new CreateNetworkViewTask(networks, viewFactory, netManager, null, appManager, serviceRegistrar);

		task.setTaskIterator(new TaskIterator(task));
		task.run(tm);
		verify(netViewManager, times(2)).addNetworkView(any(CyNetworkView.class), eq(false));
		verify(vmManager, times(2)).setVisualStyle(eq(defStyle), any(CyNetworkView.class));
	}
}
