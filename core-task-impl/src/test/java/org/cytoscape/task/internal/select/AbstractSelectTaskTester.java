package org.cytoscape.task.internal.select;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2013 The Cytoscape Consortium
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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskMonitor;

public class AbstractSelectTaskTester {

    CyEventHelper eventHelper;
    CyNetworkViewManager networkViewManager;
    TaskMonitor tm;
    CyTable table;
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
	table = mock(CyTable.class);
	net = mock(CyNetwork.class);
	when(net.getDefaultNodeTable()).thenReturn(table);

	CyNetworkView view = mock(CyNetworkView.class);
	when(view.getModel()).thenReturn(net);

	networkViewManager = mock(CyNetworkViewManager.class);
	Collection<CyNetworkView> views = new HashSet<>();
	views.add(view);
	when(networkViewManager.getNetworkViews(any(CyNetwork.class))).thenReturn(views);

	tm = mock(TaskMonitor.class);

	r1 = mock(CyRow.class);
	when(r1.getTable()).thenReturn(table);
	e1 = mock(CyEdge.class);
	when(net.getRow(e1)).thenReturn(r1);

	r2 = mock(CyRow.class);
	when(r2.getTable()).thenReturn(table);
	e2 = mock(CyEdge.class);
	when(net.getRow(e2)).thenReturn(r2);

	List<CyEdge> el = new ArrayList<>();
	el.add(e1);
	el.add(e2);
	when(net.getEdgeList()).thenReturn(el);

	r3 = mock(CyRow.class);
	when(r3.getTable()).thenReturn(table);
	e3 = mock(CyNode.class);
	when(net.getRow(e3)).thenReturn(r3);

	r4 = mock(CyRow.class);
	when(r4.getTable()).thenReturn(table);
	e4 = mock(CyNode.class);
	when(net.getRow(e4)).thenReturn(r4);

	List<CyNode> nl = new ArrayList<>();
	nl.add(e3);
	nl.add(e4);
	when(net.getNodeList()).thenReturn(nl);
    }
}
