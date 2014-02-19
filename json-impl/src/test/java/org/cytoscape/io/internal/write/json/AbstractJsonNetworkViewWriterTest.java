package org.cytoscape.io.internal.write.json;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.TaskMonitor;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractJsonNetworkViewWriterTest {
	private final NetworkViewTestSupport support = new NetworkViewTestSupport();

	protected TaskMonitor tm;

	protected Map<Long, CyNode> suid2nodeMap;
	protected Map<Long, CyEdge> suid2edgeMap;
	
	protected CyNetworkView view;

	@Before
	public void setUp() throws Exception {
		this.tm = mock(TaskMonitor.class);
		suid2nodeMap = new HashMap<Long, CyNode>();
		suid2edgeMap = new HashMap<Long, CyEdge>();
		
		this.view = generateNetworkView();
	}


	@After
	public void tearDown() throws Exception {
	}

	protected CyNetworkView generateNetworkView() throws Exception {
		final CyNetwork network1 = support.getNetwork();

		network1.getRow(network1).set(CyNetwork.NAME, "Sample Network1");
		network1.getDefaultNetworkTable().createColumn("description", String.class, false);
		network1.getDefaultNetworkTable().createColumn("score", Double.class, false);
		network1.getDefaultNetworkTable().createColumn("Network Column 1", String.class, false);
		
		network1.getRow(network1).set("description", "Sample network for testing.");
		network1.getRow(network1).set("score", 0.123d);
		network1.getRow(network1).set("Network Column 1", "sample1");
		
		CyNode n1 = network1.addNode();
		CyNode n2 = network1.addNode();
		CyNode n3 = network1.addNode();

		// Not connected
		CyNode n4 = network1.addNode();
		CyNode n5 = network1.addNode();

		suid2nodeMap.put(n1.getSUID(), n1);
		suid2nodeMap.put(n2.getSUID(), n2);
		suid2nodeMap.put(n3.getSUID(), n3);
		suid2nodeMap.put(n4.getSUID(), n4);
		suid2nodeMap.put(n5.getSUID(), n5);

		CyEdge e1 = network1.addEdge(n1, n2, true);
		CyEdge e2 = network1.addEdge(n2, n3, true);
		CyEdge e3 = network1.addEdge(n1, n3, true);
		CyEdge e1self = network1.addEdge(n1, n1, true);

		suid2edgeMap.put(e1.getSUID(), e1);
		suid2edgeMap.put(e2.getSUID(), e2);
		suid2edgeMap.put(e3.getSUID(), e3);
		suid2edgeMap.put(e1self.getSUID(), e1self);

		network1.getRow(n1).set(CyNetwork.NAME, "n1");
		network1.getRow(n2).set(CyNetwork.NAME, "n2: 日本語テスト");
		network1.getRow(n3).set(CyNetwork.NAME, "n3");
		network1.getRow(n4).set(CyNetwork.NAME, "n4: Alone");
		network1.getRow(n5).set(CyNetwork.NAME, "n5");

		network1.getRow(e1).set(CyNetwork.NAME, "e1");
		network1.getRow(e2).set(CyNetwork.NAME, "エッジ2");
		network1.getRow(e3).set(CyNetwork.NAME, "e3");
		network1.getRow(e1self).set(CyNetwork.NAME, "e1self");

		final CyNetworkView view = support.getNetworkViewFactory().createNetworkView(network1);
		
		// Create some visual properties
		final View<CyNode> view1 = view.getNodeView(n1);
		view1.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, 50d);
		view1.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, 50d);
		
		assertEquals(5, view.getModel().getNodeCount());
		assertEquals(4, view.getModel().getEdgeCount());

		return view;
	}
}