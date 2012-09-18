package org.cytoscape.model; 

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;


public class PerfTest {

	private final CyNetworkFactory netFactory;
	private final CyRootNetworkManager rootMgr;

	public static void main(String[] args) {
		new PerfTest().runTestLoop();
	}

	public PerfTest() {
		NetworkTestSupport testSupport = new NetworkTestSupport();
		netFactory = testSupport.getNetworkFactory();
		rootMgr = testSupport.getRootNetworkFactory();
	}

	public void runTestLoop() {
		final int EFFECTIVE_LOOP_COUNT = 1;
		for (int i = 0; i < EFFECTIVE_LOOP_COUNT; i++) {
			final long startTime = System.currentTimeMillis();
			testMiscNodeAndEdgeOps();
			final long endTime = System.currentTimeMillis();

			long createNet = (endTime-startTime);

			System.out.println("createNetwork && subs: " + createNet);
		}
	}

	private void testMiscNodeAndEdgeOps() {
		final CyNetwork network = netFactory.createNetwork(); 

		// create nodes
		final int NODE_COUNT = 50000;
		final List<CyNode> nodes = new ArrayList<CyNode>(NODE_COUNT);
		for (int i = 0; i < NODE_COUNT; i++) {
			nodes.add(network.addNode());
		}

		boolean isDirected = true;
		final Random rand = new Random(1234L);

		// create edges
		final int EDGE_COUNT = 100000;
		final List<CyEdge> edges = new ArrayList<CyEdge>(EDGE_COUNT);
		for (int i = 0; i < EDGE_COUNT; i++) {
			final CyNode source = nodes.get(rand.nextInt(NODE_COUNT));
			final CyNode target = nodes.get(rand.nextInt(NODE_COUNT));
			edges.add(network.addEdge(source, target, isDirected));
			isDirected = !isDirected;
		}

		// create subnetworks
		CyRootNetwork root = rootMgr.getRootNetwork(network);
		int i = 0;
		for ( CyNode n : network.getNodeList() ) {
			if ( i++ > 1000 ) break;
			List<CyNode> nl = network.getNeighborList(n,CyEdge.Type.ANY);
			Set<CyEdge> es = new HashSet<CyEdge>();
			for ( CyNode nn : nl ) {
				List<CyEdge> ee = network.getConnectingEdgeList(n,nn,CyEdge.Type.ANY);
				es.addAll(ee);
			}
			root.addSubNetwork(nl,es);
		}
	}
}
