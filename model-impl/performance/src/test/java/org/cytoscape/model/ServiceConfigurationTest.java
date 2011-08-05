package org.cytoscape.model; 


import org.easymock.EasyMock;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.MavenConfiguredJUnit4TestRunner;
import org.osgi.util.tracker.ServiceTracker;

import org.cytoscape.event.CyEvent;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.equations.Interpreter;
import org.cytoscape.integration.ServiceTestSupport;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@RunWith(MavenConfiguredJUnit4TestRunner.class)
public class ServiceConfigurationTest extends ServiceTestSupport {
	private CyNetworkFactory networkFactory;

	@Before 
	public void setup() {
		registerMockService(Interpreter.class);
		registerMockService(CyServiceRegistrar.class);

		// Obtain a CyNetworkFactory service:
		final ServiceTracker networkFactoryTracker =
			new ServiceTracker(bundleContext, CyNetworkFactory.class.getName(), null);
		networkFactoryTracker.open();
		networkFactory = null;
		try {
			final int WAIT_TIME = 10000; // seconds
			networkFactory = (CyNetworkFactory)networkFactoryTracker.waitForService(WAIT_TIME);
		} catch (final InterruptedException ie) {
			fail("Did not get an instance of a CyNetworkFactory service within the specified amount of time!");
		}
		assertNotNull(networkFactory);
	}

	@Test
	public void runTestLoop() {
		final int EFFECTIVE_LOOP_COUNT = 2;
		for (int i = 0; i <= EFFECTIVE_LOOP_COUNT; ++i) {
			final long startTime = System.nanoTime();
			testMiscNodeAndEdgeOps();
			final long endTime = System.nanoTime();
			if (i > 0) // We throw the first value away.
				System.err.println("*** CTRT: " + getClass().getName() + ".testMiscNodeAndEdgeOps "
						   + (endTime - startTime));
		}
	}

	private void testMiscNodeAndEdgeOps() {
		final CyNetwork network = networkFactory.getInstance();

		final int NODE_COUNT = 50000;
		final List<CyNode> nodes = new ArrayList<CyNode>(NODE_COUNT);
		for (int i = 0; i < NODE_COUNT; ++i)
			nodes.add(network.addNode());

/*
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

		for (int i = 0; i < 1000; ++i)
			network.getNeighborList(nodes.get(i), CyEdge.Type.ANY);
		for (int i = 0; i < 1000; ++i)
			network.getAdjacentEdgeList(nodes.get(i), CyEdge.Type.ANY);
		for (int i = 0; i < 1000; ++i) {
			final CyNode source = nodes.get(i);
			final CyNode target = nodes.get(i + 1000);
			network.getConnectingEdgeList(source, target, CyEdge.Type.ANY);
		}
*/
	}
}
