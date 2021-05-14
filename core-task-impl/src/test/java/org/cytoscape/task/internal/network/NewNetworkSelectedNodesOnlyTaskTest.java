package org.cytoscape.task.internal.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.internal.CyNetworkManagerImpl;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Before;
import org.junit.Test;

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

public class NewNetworkSelectedNodesOnlyTaskTest {
	
	private final NetworkTestSupport support = new NetworkTestSupport();
	private final NetworkViewTestSupport viewSupport = new NetworkViewTestSupport();

	private CyNetwork net = support.getNetwork();
	
	private CyApplicationManager appMgr = mock(CyApplicationManager.class);
	private CyRootNetworkManager rootNetMgr = support.getRootNetworkFactory();
	private CyNetworkViewFactory netViewFactory = viewSupport.getNetworkViewFactory();
	private UndoSupport undoSupport = mock(UndoSupport.class);
	private CyEventHelper eventHelper = new DummyCyEventHelper();
	private CyNetworkNaming namingUtil = mock(CyNetworkNaming.class);
    private CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
	private CyNetworkManager netMgr = new CyNetworkManagerImpl(serviceRegistrar);
	private CyNetworkViewManager netViewMgr = mock(CyNetworkViewManager.class);
	private VisualMappingManager visMapMgr = mock(VisualMappingManager.class);
	private RenderingEngineManager renderingEngineMgr = mock(RenderingEngineManager.class);
	private CyGroupManager groupMgr = mock(CyGroupManager.class);
	private CyLayoutAlgorithmManager layoutMgr = mock(CyLayoutAlgorithmManager.class);
	
	@Before
	public void setUp() throws Exception {
		when(serviceRegistrar.getService(CyApplicationManager.class)).thenReturn(appMgr);
		when(serviceRegistrar.getService(CyRootNetworkManager.class)).thenReturn(rootNetMgr);
		when(serviceRegistrar.getService(CyNetworkManager.class)).thenReturn(netMgr);
		when(serviceRegistrar.getService(CyNetworkFactory.class)).thenReturn(support.getNetworkFactory());
		when(serviceRegistrar.getService(CyNetworkViewFactory.class)).thenReturn(netViewFactory);
		when(serviceRegistrar.getService(CyNetworkViewManager.class)).thenReturn(netViewMgr);
		when(serviceRegistrar.getService(RenderingEngineManager.class)).thenReturn(renderingEngineMgr);
		when(serviceRegistrar.getService(CyGroupManager.class)).thenReturn(groupMgr);
		when(serviceRegistrar.getService(VisualMappingManager.class)).thenReturn(visMapMgr);
		when(serviceRegistrar.getService(UndoSupport.class)).thenReturn(undoSupport);
		when(serviceRegistrar.getService(CyLayoutAlgorithmManager.class)).thenReturn(layoutMgr);
		when(serviceRegistrar.getService(UndoSupport.class)).thenReturn(undoSupport);
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
        when(serviceRegistrar.getService(CyNetworkNaming.class)).thenReturn(namingUtil);
        when(serviceRegistrar.getService(CyLayoutAlgorithmManager.class)).thenReturn(layoutMgr);
		
		when(renderingEngineMgr.getRenderingEngines(any(View.class))).thenReturn(Collections.EMPTY_LIST);
	}
	
	@Test
	public void testNewNetworkSelectedNodesOnlyTask() throws Exception {
		netMgr.addNetwork(net);
		final CyNode node1 = net.addNode();
		final CyNode node2 = net.addNode();
		final CyNode node3 = net.addNode();
	
		final CyEdge edge1 = net.addEdge(node1, node2, true);
		//final CyEdge edge2 = net.addEdge(node2, node3, true);
		
		net.getRow(node1).set(CyNetwork.SELECTED, true);
		//net.getRow(node2).set(CyNetwork.SELECTED, true);
		net.getRow(edge1).set(CyNetwork.SELECTED, true);
		
		int numberOfNetsBeforeTask = netMgr.getNetworkSet().size();
		List<CyNetwork> netListbeforeTask = new ArrayList<>(netMgr.getNetworkSet());

		var task = new NewNetworkSelectedNodesOnlyTask(net, serviceRegistrar);

		assertNotNull("task is null!", task);
		task.setTaskIterator(new TaskIterator(task));
		task.run(mock(TaskMonitor.class));
		
		int numberOfNetsAfterTask = netMgr.getNetworkSet().size();
		assertEquals(1, numberOfNetsAfterTask - numberOfNetsBeforeTask);
		
		List<CyNetwork> networkList = new ArrayList<>(netMgr.getNetworkSet());
		networkList.removeAll(netListbeforeTask);
		assertEquals(1, networkList.get(0).getNodeList().size());
		assertEquals(0, networkList.get(0).getEdgeList().size());
	}
}
