package org.cytoscape.task.internal.creation;


import static org.mockito.Mockito.mock;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetworkFactory;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Before;
import org.junit.Test;


public class NewNetworkSelectedNodesEdgesTaskTest {
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
		final UndoSupport undoSupport = mock(UndoSupport.class);

		final CyNode node1 = net.addNode();
		final CyNode node2 = net.addNode();
		final CyNode node3 = net.addNode();
		
		final CyTable nodeTable = net.getDefaultNodeTable();
		node1.getCyRow().set(CyNetwork.SELECTED, true);

		CyEventHelper eventHelper = mock(CyEventHelper.class);

		final NewNetworkSelectedNodesEdgesTask task =
			new NewNetworkSelectedNodesEdgesTask(undoSupport, net, cyroot, cnvf, netmgr,
			                                     networkViewManager, cyNetworkNaming,
			                                     vmm, appManager, eventHelper);
		
		TaskMonitor tm = mock(TaskMonitor.class);
		
		// TODO how can we test this?
		//task.run(tm);
	}

}
