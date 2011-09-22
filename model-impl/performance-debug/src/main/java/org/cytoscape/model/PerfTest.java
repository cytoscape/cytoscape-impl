package org.cytoscape.model; 



import org.cytoscape.event.CyEvent;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.equations.Interpreter;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.service.util.CyServiceRegistrar;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class PerfTest {

	private final CyNetworkFactory netFactory;

	public static void main(String[] args) {
		new PerfTest().runTestLoop();
	}

	public PerfTest() {
		NetworkTestSupport testSupport = new NetworkTestSupport();
		netFactory = testSupport.getNetworkFactory();
	}

	public void runTestLoop() {
		final int EFFECTIVE_LOOP_COUNT = 5;
		for (int i = 0; i <= EFFECTIVE_LOOP_COUNT; ++i) {
			final long startTime = System.currentTimeMillis();
			testMiscNodeAndEdgeOps();
			final long endTime = System.currentTimeMillis();

			final long startTime2 = System.currentTimeMillis();
			testMiscNodeAndEdgeOpsBuilder();
			final long endTime2 = System.currentTimeMillis();
		
			long noBuilder = (endTime-startTime);
			long withBuilder = (endTime2-startTime2);

			long diff = noBuilder - withBuilder; 
			double per = ((double)diff)/((double)(endTime-startTime));
			System.out.println("difference: " + diff + "   no builder:  " + noBuilder + "   builder: " + withBuilder);
		}
	}

	private void testMiscNodeAndEdgeOps() {
		final CyNetwork network = netFactory.getInstance(); 

		final int NODE_COUNT = 50000;
		final List<CyNode> nodes = new ArrayList<CyNode>(NODE_COUNT);
		for (int i = 0; i < NODE_COUNT; ++i) {
			nodes.add(network.addNode());
		}

		boolean isDirected = true;
		final Random rand = new Random(1234L);

		final int EDGE_COUNT = 100000;
		final List<CyEdge> edges = new ArrayList<CyEdge>(EDGE_COUNT);
		for (int i = 0; i < EDGE_COUNT; ++i) {
			final CyNode source = nodes.get(rand.nextInt(NODE_COUNT));
			final CyNode target = nodes.get(rand.nextInt(NODE_COUNT));
			edges.add(network.addEdge(source, target, isDirected));
			isDirected = !isDirected;
		}
	}

	private void testMiscNodeAndEdgeOpsBuilder() {
		CyNetworkBuilder netBuilder = new CyNetworkBuilder();

		final int NODE_COUNT = 50000;
		final List<CyNodeBuilder> nodes = new ArrayList<CyNodeBuilder>(NODE_COUNT);
		for (int i = 0; i < NODE_COUNT; ++i) {
			nodes.add(netBuilder.addNode());
		}

		boolean isDirected = true;
		final Random rand = new Random(1234L);

		final int EDGE_COUNT = 100000;
		final List<CyEdgeBuilder> edges = new ArrayList<CyEdgeBuilder>(EDGE_COUNT);
		for (int i = 0; i < EDGE_COUNT; ++i) {
			final CyNodeBuilder source = nodes.get(rand.nextInt(NODE_COUNT));
			final CyNodeBuilder target = nodes.get(rand.nextInt(NODE_COUNT));
			edges.add(netBuilder.addEdge(source, target, isDirected));
			isDirected = !isDirected;
		}
		final CyNetwork network = netFactory.getInstance(); 
	}
}
