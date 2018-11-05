package org.cytoscape.task.internal.select;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2018 The Cytoscape Consortium
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

public class AbstractSelectTaskTester {

    protected CyEventHelper eventHelper;
    protected CyNetworkViewManager networkViewManager;
    protected UndoSupport undoSupport;
    protected CyApplicationManager applicationManager;
    protected CyServiceRegistrar serviceRegistrar;
    
	TaskMonitor tm;
	CyTable nodeTable;
	CyTable edgeTable;
	CyNetwork net;
	CyRow r1;
	CyEdge e1;
	CyRow r2;
	CyEdge e2;
	CyRow r3;
	CyNode e3;
	CyRow r4;
	CyNode e4;

	public void setUp() throws Exception {
		eventHelper = mock(CyEventHelper.class);
		undoSupport = mock(UndoSupport.class);
		applicationManager = mock(CyApplicationManager.class);
		networkViewManager = mock(CyNetworkViewManager.class);
		serviceRegistrar = mock(CyServiceRegistrar.class);
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
		when(serviceRegistrar.getService(CyNetworkViewManager.class)).thenReturn(networkViewManager);
		when(serviceRegistrar.getService(UndoSupport.class)).thenReturn(undoSupport);
		when(serviceRegistrar.getService(CyApplicationManager.class)).thenReturn(applicationManager);
		
		nodeTable = mock(CyTable.class);
		edgeTable = mock(CyTable.class);
		net = mock(CyNetwork.class);
		when(net.getDefaultNodeTable()).thenReturn(nodeTable);
		when(net.getDefaultEdgeTable()).thenReturn(edgeTable);

		CyNetworkView view = mock(CyNetworkView.class);
		when(view.getModel()).thenReturn(net);

		Collection<CyNetworkView> views = new HashSet<>();
		views.add(view);
		when(networkViewManager.getNetworkViews(any(CyNetwork.class))).thenReturn(views);

		tm = mock(TaskMonitor.class);

		r1 = mock(CyRow.class);
		when(r1.getTable()).thenReturn(edgeTable);
		e1 = mock(CyEdge.class);
		when(net.getRow(e1)).thenReturn(r1);
		when(r1.get(CyNetwork.SUID, Long.class)).thenReturn(1L);
		when(e1.getSUID()).thenReturn(1L);
		when(net.getEdge(1L)).thenReturn(e1);
		when(edgeTable.getRow(1L)).thenReturn(r1);

		r2 = mock(CyRow.class);
		when(r2.getTable()).thenReturn(edgeTable);
		e2 = mock(CyEdge.class);
		when(net.getRow(e2)).thenReturn(r2);
		when(r2.get(CyNetwork.SUID, Long.class)).thenReturn(2L);
		when(e2.getSUID()).thenReturn(2L);
		when(net.getEdge(2L)).thenReturn(e2);
		when(edgeTable.getRow(2L)).thenReturn(r2);

		List<CyEdge> el = new ArrayList<>();
		el.add(e1);
		el.add(e2);
		when(net.getEdgeList()).thenReturn(el);

		r3 = mock(CyRow.class);
		when(r3.getTable()).thenReturn(nodeTable);
		e3 = mock(CyNode.class);
		when(net.getRow(e3)).thenReturn(r3);
		when(r3.get(CyNetwork.SUID, Long.class)).thenReturn(3L);
		when(e3.getSUID()).thenReturn(3L);
		when(net.getNode(3L)).thenReturn(e3);
		when(nodeTable.getRow(3L)).thenReturn(r3);

		r4 = mock(CyRow.class);
		when(r4.getTable()).thenReturn(nodeTable);
		e4 = mock(CyNode.class);
		when(net.getRow(e4)).thenReturn(r4);
		when(r4.get(CyNetwork.SUID, Long.class)).thenReturn(4L);
		when(e4.getSUID()).thenReturn(4L);
		when(net.getNode(4L)).thenReturn(e4);
		when(nodeTable.getRow(4L)).thenReturn(r4);

		List<CyNode> nl = new ArrayList<>();
		nl.add(e3);
		nl.add(e4);
		when(net.getNodeList()).thenReturn(nl);
	}
}
