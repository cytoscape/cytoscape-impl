package org.cytoscape.task.internal.hide;


import javax.swing.undo.UndoableEditSupport;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNetworkViewTaskTest;
import org.cytoscape.test.support.NetworkViewTestSupport;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


public class HideSelectedEdgesTaskTest extends AbstractNetworkViewTaskTest {
	private final NetworkViewTestSupport viewSupport = new NetworkViewTestSupport();
	private CyNetworkView view = viewSupport.getNetworkView();
	@Mock TaskMonitor tm;
	@Mock CyEventHelper eventHelper;
	@Mock UndoSupport undoSupport;
	
	CyEdge edge1;
	CyEdge edge2;
	CyEdge edge3;
	
	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
		
		final CyNetwork network = view.getModel();
		final CyNode n1 = network.addNode();
		final CyNode n2 = network.addNode();
		final CyNode n3 = network.addNode();
		
		edge1 = view.getModel().addEdge(n1, n2, true);
		edge2 = network.addEdge(n1, n3, true);
		edge3 = network.addEdge(n2, n3, true);
		
		edge1.getCyRow().set(CyNetwork.SELECTED, true);
		edge3.getCyRow().set(CyNetwork.SELECTED, true);
	}
	
	@Test(expected=NullPointerException.class)
	public void testHideSelectedEdgesTask() throws Exception {
		final HideSelectedEdgesTask task =
			new HideSelectedEdgesTask(undoSupport, eventHelper, view);
		
		task.run(tm);
	}

}
