package org.cytoscape.task.internal.creation;


import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.undo.UndoableEditSupport;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetworkFactory;
import org.cytoscape.session.CyApplicationManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.test.support.NetworkTestSupport;
import org.cytoscape.test.support.NetworkViewTestSupport;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.undo.UndoSupport;

import org.junit.Before;
import org.junit.Test;


public class NewNetworkSelectedNodesOnlyTaskTest {
	private final NetworkTestSupport support = new NetworkTestSupport();
	private final NetworkViewTestSupport viewSupport = new NetworkViewTestSupport();
	private CyNetwork net = support.getNetwork();
	private CyRootNetworkFactory cyroot = mock(CyRootNetworkFactory.class);
	private CyNetworkViewFactory cnvf = viewSupport.getNetworkViewFactory();
	private CyNetworkManager netmgr = mock(CyNetworkManager.class);
	private CyNetworkViewManager networkViewManager = mock(CyNetworkViewManager.class);
	private CyNetworkNaming cyNetworkNaming = mock(CyNetworkNaming.class);
	private VisualMappingManager vmm = mock(VisualMappingManager.class);
	private CyApplicationManager appManager = mock(CyApplicationManager.class);

	@Before
	public void setUp() throws Exception {
	}
	
	@Test
	public void testNewNetworkSelectedNodesEdgesTask() throws Exception {
		UndoableEditSupport undoableEditSupport = mock(UndoableEditSupport.class);
		UndoSupport undoSupport = mock(UndoSupport.class);
		when(undoSupport.getUndoableEditSupport()).thenReturn(undoableEditSupport);

		CyEventHelper eventHelper = mock(CyEventHelper.class);

		NewNetworkSelectedNodesOnlyTask task =
			new NewNetworkSelectedNodesOnlyTask(undoSupport, net, cyroot, cnvf, netmgr,
			                                    networkViewManager, cyNetworkNaming, vmm,
			                                    appManager, eventHelper);
		
		final CyNode node1 = net.addNode();
		final CyNode node2 = net.addNode();
		final CyNode node3 = net.addNode();
		
		final CyEdge edge1 = net.addEdge(node1, node2, true);
		
		final List<CyNode> selectedNodes = new ArrayList<CyNode>();
		
		node1.getCyRow().set(CyNetwork.SELECTED, true);
		node2.getCyRow().set(CyNetwork.SELECTED, true);
		
		selectedNodes.add(node1);
		selectedNodes.add(node2);
		
		final Collection<CyEdge> edges = task.getEdges(net, selectedNodes);
		
		assertNotNull(edges);
		assertEquals(1, edges.size());
		
		final CyEdge testEdge = edges.iterator().next();
		
		assertEquals(edge1, testEdge);
		
		// TODO: fix this
		//task.run(null);
	}

}
