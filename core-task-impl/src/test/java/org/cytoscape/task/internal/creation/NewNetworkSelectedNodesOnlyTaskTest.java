package org.cytoscape.task.internal.creation;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.internal.CyNetworkManagerImpl;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Before;
import org.junit.Test;


public class NewNetworkSelectedNodesOnlyTaskTest {
	private final NetworkTestSupport support = new NetworkTestSupport();
	private final NetworkViewTestSupport viewSupport = new NetworkViewTestSupport();

	private CyNetwork net = support.getNetwork();
	private CyRootNetworkManager cyroot = support.getRootNetworkFactory();
	private CyNetworkViewFactory cnvf = viewSupport.getNetworkViewFactory();
	CyEventHelper eventHelper = new DummyCyEventHelper();
	private CyNetworkManager netmgr = new CyNetworkManagerImpl(eventHelper);
	private CyNetworkViewManager networkViewManager = mock(CyNetworkViewManager.class);
	private CyNetworkNaming cyNetworkNaming = mock(CyNetworkNaming.class);
	private VisualMappingManager vmm = mock(VisualMappingManager.class);
	private CyApplicationManager appManager = mock(CyApplicationManager.class);

	@Before
	public void setUp() throws Exception {
	}
	
	@Test
	public void testNewNetworkSelectedNodesOnlyTask() throws Exception {
		final UndoSupport undoSupport = mock(UndoSupport.class);
		
		netmgr.addNetwork(net);
		final CyNode node1 = net.addNode();
		final CyNode node2 = net.addNode();
		final CyNode node3 = net.addNode();
	
		final CyEdge edge1 = net.addEdge(node1, node2, true);
		//final CyEdge edge2 = net.addEdge(node2, node3, true);
		
		net.getRow(node1).set(CyNetwork.SELECTED, true);
		//net.getRow(node2).set(CyNetwork.SELECTED, true);
		net.getRow(edge1).set(CyNetwork.SELECTED, true);
		
		int numberOfNetsBeforeTask = netmgr.getNetworkSet().size();
		List<CyNetwork> netListbeforeTask = new ArrayList<CyNetwork>(netmgr.getNetworkSet());

		final NewNetworkSelectedNodesOnlyTask task =
			new NewNetworkSelectedNodesOnlyTask(undoSupport, net, cyroot, cnvf, netmgr,
			                                     networkViewManager, cyNetworkNaming,
			                                     vmm, appManager, eventHelper);
		
		assertNotNull("task is null!" , task);
		task.setTaskIterator(new TaskIterator(task));
		task.run(mock(TaskMonitor.class));
		
		int numberOfNetsAfterTask = netmgr.getNetworkSet().size();
		assertEquals(1, numberOfNetsAfterTask - numberOfNetsBeforeTask);
		
		List<CyNetwork> networkList = new ArrayList<CyNetwork>(netmgr.getNetworkSet());
		networkList.removeAll(netListbeforeTask);
		assertEquals(1, networkList.get(0).getNodeList().size());
		assertEquals(0, networkList.get(0).getEdgeList().size());
	}
}
