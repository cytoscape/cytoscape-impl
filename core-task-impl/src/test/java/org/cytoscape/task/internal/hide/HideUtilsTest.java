package org.cytoscape.task.internal.hide;

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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


public class HideUtilsTest {
	
	@Mock private CyNetworkView view;
	@Mock private CyNetwork network;
	
	private Collection<CyEdge> edges;
	private Collection<CyNode> nodes;
	
	@Mock CyEdge edge1;
	@Mock CyEdge edge2;
	@Mock CyEdge edge3;
	@Mock CyNode node1;
	@Mock CyNode node2;
	@Mock CyNode node3;
	@Mock private View<CyEdge> ev1;
	@Mock private View<CyEdge> ev2;
	@Mock private View<CyEdge> ev3;
	@Mock private View<CyNode> nv1;
	@Mock private View<CyNode> nv2;
	@Mock private View<CyNode> nv3;
	
	@Before
	public void initMocks() {
		edges = new ArrayList<>();
		edges.add(edge1);
		edges.add(edge2);
		edges.add(edge3);
		
		nodes = new ArrayList<>();
		nodes.add(node1);
		nodes.add(node2);
		nodes.add(node3);
		
		MockitoAnnotations.initMocks(this);
		when(view.getEdgeView(any(CyEdge.class))).thenReturn(ev1, ev2, ev3);
		when(view.getNodeView(any(CyNode.class))).thenReturn(nv1, nv2, nv3);
		when(view.getModel()).thenReturn(network);
	}
	
	@Test
	public void testsetVisibleEdges() throws Exception {

		HideUtils.setVisibleEdges(edges, true, view);
		verify(view, times(3)).getEdgeView(any(CyEdge.class));
		
	}
	
	@Test
	public void testsetVisibleNodes() throws Exception {

		HideUtils.setVisibleNodes(nodes, true, view);
		verify(view, times(3)).getNodeView(any(CyNode.class));
		
	}
}
