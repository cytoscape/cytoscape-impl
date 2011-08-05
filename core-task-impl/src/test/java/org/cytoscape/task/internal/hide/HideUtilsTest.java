package org.cytoscape.task.internal.hide;

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

import static org.mockito.Mockito.*;


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
		edges = new ArrayList<CyEdge>();
		edges.add(edge1);
		edges.add(edge2);
		edges.add(edge3);
		
		nodes = new ArrayList<CyNode>();
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
